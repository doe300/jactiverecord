/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 doe300
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.doe300.activerecord.migration;

import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.attributes.Attributes;
import de.doe300.activerecord.store.RecordStore;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This migration is used to automatically create a table from a given record-type.
 * It supports {@link TimestampedRecord}.
 * 
 * This migration walks through all methods of the given type and tries to determine the columns. 
 * Column-names for the attributes are retrieved in this order:
 * <ol>
 *	<li>If the method is annotated by {@link Attribute}, the {@link Attribute#name() } is used as name:
 *		<ol>
 *			<li>Use the {@link Attribute#typeName() } as type, if set, and finish</li>
 *			<li>Use the {@link Attribute#type() }, map it via {@link #getSQLType(int) } and finish</li>
 *		</ol>
 *	</li>
 *	<li>Ignore method, if it is <code>static</code> or <code>default</code>.</li>
 *	<li>If the method is annotated by {@link AttributeGetter}, use {@link AttributeGetter#name() } as column-name<br>
 *		Use the methods return-type, map it via {@link #getSQLType(int) } and finish
 *	</li>
 *	<li>If the method is annotated by {@link AttributeSetter}, use {@link AttributeSetter#name() } as column-name<br>
 *		Use the methods only parameter, map it via {@link #getSQLType(int) } and finish
 *	</li>
 *	<li>Use the methods property-name as column-name and its return-type (or only parameter) as data-type, map it via {@link #getSQLType(int) } and finish</li>
 * </ol>
 * 
 * NOTE: The resulting columns may be inaccurate due to type discrepancies in mapping java-types to SQL-types.
 * Also, this migration is optimized for interface based records and may yield some unexpected results on POJO records.
 * 
 * 
 * @author doe300
 */
public class AutomaticMigration implements Migration
{
	private final Class<? extends ActiveRecord> recordType;
	private final boolean dropColumnsOnUpdate;

	/**
	 * @param recordType the type to create and drop the table for
	 * @param dropColumnsOnUpdate  whether to drop obsolete columns on update
	 */
	public AutomaticMigration(Class<? extends ActiveRecord> recordType, boolean dropColumnsOnUpdate )
	{
		this.recordType = recordType;
		this.dropColumnsOnUpdate = dropColumnsOnUpdate;
	}

	@Override
	public boolean apply( Connection con ) throws SQLException
	{
		String tableName = getTableName( recordType );
		//1. check if table exists
		if(structureExists( con, tableName))
		{
			return false;
		}
		//2. get desired columns and types
		Map<String,String> columns = getColumnsFromModel( recordType );
		//3. execute statement
		String sql = "CREATE TABLE "+tableName+" ("
				+columns.entrySet().stream().map( (Map.Entry<String,String> e) -> e.getKey()+" "+e.getValue())
						.collect( Collectors.joining(", "))
				+" )";
		Logging.getLogger().info( recordType.getSimpleName(), "Executing automatic table-creation...");
		Logging.getLogger().info( recordType.getSimpleName(), sql);
		try(Statement stm = con.createStatement())
		{
			if(stm.executeUpdate(sql)<0)
			{
				Logging.getLogger().error( recordType.getSimpleName(), "Automatic table-creation failed!");
				return false;
			}
		}
		catch(SQLException e)
		{
			Logging.getLogger().error( recordType.getSimpleName(), "Automatic table-creation failed with error!");
			Logging.getLogger().error( recordType.getSimpleName(), e);
			throw e;
		}
		return true;
	}
	
	/**
	 * Updates the table for the given record-type.
	 * More precisely, adds and removes columns to fit the current methods of the record-type.
	 * For more information on how columns are generated from the type's methods, see the documentation of this class.
	 * 
	 * NOTE: If <code>dropColumnsOnUpdate</code> is set to <code>true</code>, this method will drop all columns it deems not used anymore!
	 * @param con
	 * @return whether the table was updated
	 * @throws java.sql.SQLException
	 */
	@Override
	public boolean update( Connection con) throws SQLException
	{
		String tableName = getTableName( recordType );
		//1. check if table exists
		if(structureExists( con, tableName))
		{
			return false;
		}
		//2. get existing columns
		Map<String,String> hasColumns = new HashMap<>(10);
		try(ResultSet set = con.getMetaData().getColumns( con.getCatalog(), con.getSchema(), tableName, null))
		{
			while(set.next())
			{
				hasColumns.put( set.getString( "COLUMN_NAME").toLowerCase(), set.getString( "TYPE_NAME"));
			}
		}
		//3. get desired columns
		Map<String,String> desiredColumns = getColumnsFromModel( recordType );
		//4. calculate difference
		Map<String,String> removeColumns = hasColumns.entrySet().stream().
				filter( (Map.Entry<String,String> e) -> desiredColumns.containsKey( e.getKey())).collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue));
		Map<String,String> addColumns = desiredColumns.entrySet().stream().
				filter( (Map.Entry<String,String> e) -> hasColumns.containsKey( e.getKey())).collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue));
		//5. execute updates
		boolean changed = false;
		//SQL needs extra statements for add and drop columns
		if(dropColumnsOnUpdate && !removeColumns.isEmpty())
		{
			String sql = "ALTER TABLE "+tableName+" ("+
					"DROP COLUMN "+
					removeColumns.entrySet().stream().map( (Map.Entry<String,String> e) -> e.getKey()).collect( Collectors.joining( ", "))+
					")";
			Logging.getLogger().info(recordType.getSimpleName(), "Executing automatic table-update...");
			Logging.getLogger().info(recordType.getSimpleName(), sql);
			try(Statement stm = con.createStatement())
			{
				changed = (stm.executeUpdate(sql) >= 0);
			}
		}
		if(!addColumns.isEmpty())
		{
			String sql = "ALTER TABLE "+tableName+" ("+
					"ADD "+
					addColumns.entrySet().stream().map( (Map.Entry<String,String> e) -> e.getKey()+" "+e.getValue()).collect( Collectors.joining( ", "))+
					")";
			Logging.getLogger().info(recordType.getSimpleName(), "Executing automatic table-update...");
			Logging.getLogger().info(recordType.getSimpleName(), sql);
			try(Statement stm = con.createStatement())
			{
				changed = changed || (stm.executeUpdate(sql) >= 0);
			}
		}
		if(!changed)
		{
			Logging.getLogger().error(recordType.getSimpleName(), "Automatic table-update failed!");
		}
		return changed;
	}

	/**
	 * This method drops the created table, if it exists
	 * @param con
	 * @return whether the table existed and was dropped
	 * @throws SQLException 
	 */
	@Override
	public boolean revert( Connection con ) throws SQLException
	{
		String tableName = getTableName( recordType );
		//1. check if table exists
		if(!structureExists( con, tableName))
		{
			return false;
		}
		//2. drop table
		String sql = "DROP TABLE "+tableName;
		Logging.getLogger().info(recordType.getSimpleName(), "Executing automatic table-drop...");
		Logging.getLogger().info(recordType.getSimpleName(), sql);
		try(Statement stm = con.createStatement( ))
		{
			if(stm.executeUpdate(sql) < 0)
			{
				Logging.getLogger().error(recordType.getSimpleName(), "Automatic table-drop failed!");
				return false;
			}
		}
		catch(SQLException e)
		{
			Logging.getLogger().error( recordType.getSimpleName(), "Automatic table-drop failed with error!");
			Logging.getLogger().error( recordType.getSimpleName(), e);
			throw e;
		}
		return true;
	}

	private static String getTableName(Class<? extends ActiveRecord> type)
	{
		if(type.isAnnotationPresent(RecordType.class))
		{
			return type.getAnnotation(RecordType.class).typeName();
		}
		return type.getSimpleName();
	}
	
	private static String getPrimaryColumn(Class<? extends ActiveRecord> type)
	{
		if(type.isAnnotationPresent(RecordType.class))
		{
			return type.getAnnotation(RecordType.class).primaryKey();
		}
		return RecordStore.DEFAULT_COLUMN_ID;
	}
	
	/**
	 * The column-names are all in lower case
	 * @param recordType
	 * @return the columns
	 */
	private static Map<String,String> getColumnsFromModel(Class<? extends ActiveRecord> recordType) throws IllegalArgumentException
	{
		//TODO move ID to first column
		HashMap<String,String> columns = new HashMap<>(10);
		Method[] methods = recordType.getMethods();
		for(Method method:methods)
		{
			//1. get attributes
			//this is priorized before skipping of default methods, so columns for assoziations are created correctly
			if(method.isAnnotationPresent( Attribute.class))
			{
				Attribute att = method.getAnnotation( Attribute.class);
				String name = att.name().toLowerCase();
				if(!"".equals( att.typeName() ))
				{
					columns.put(name, att.typeName());
				}
				else
				{
					columns.putIfAbsent(name, getSQLType( att.type()));
				}
				columns.put( name, columns.get( name) 
						+(att.mayBeNull()?" NULL": " NOT NULL")
						+(!"".equals( att.defaultValue() )?" DEFAULT "+att.defaultValue(): ""));
				continue;
			}
			//skip default or static methods
			if(method.isDefault() || (method.getModifiers() & Modifier.STATIC) == Modifier.STATIC || (method.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC)
			{
				continue;
			}
			//skip methods from ActiveRecord (#getPrimaryKey() and #getBase())
			if(method.getDeclaringClass().equals( ActiveRecord.class))
			{
				continue;
			}
			String columnName = null;
			Class<?> attType = null;
			//2. get attribute-accessors
			if(method.isAnnotationPresent( AttributeGetter.class))
			{
				AttributeGetter acc = method.getAnnotation( AttributeGetter.class);
				columnName = acc.name().toLowerCase();
				attType = method.getReturnType();
			}
			else if(method.isAnnotationPresent( AttributeSetter.class))
			{
				AttributeSetter acc = method.getAnnotation( AttributeSetter.class);
				columnName = acc.name().toLowerCase();
				attType = method.getParameterTypes()[0];
			}
			//3. get bean accessors
			else if(Attributes.isGetter( method, false))
			{
				columnName = Attributes.getPropertyName( method ).toLowerCase();
				attType = method.getReturnType();
			}
			else if(Attributes.isSetter( method, null, false))
			{
				columnName = Attributes.getPropertyName( method ).toLowerCase();
				attType = method.getParameterTypes()[0];
			}
			//convert type (for 2. and 3.)
			if(columnName!=null && attType!=null)
			{
				columns.putIfAbsent(columnName, getSQLType( attType));
			}
		}
		//4. add timestamps, other features
		if(TimestampedRecord.class.isAssignableFrom( recordType))
		{
			//forces the timestamps to be overriden, because they need to be of type Timestamp
			columns.put(RecordStore.COLUMN_CREATED_AT, getSQLType( java.sql.Timestamp.class));
			columns.put(RecordStore.COLUMN_UPDATED_AT, getSQLType( java.sql.Timestamp.class));
		}
		//5. mark or add primary key, add constraints
		String primaryColumn = getPrimaryColumn( recordType).toLowerCase();
		columns.putIfAbsent( primaryColumn, getSQLType( Integer.class));
		columns.put( primaryColumn, columns.get( primaryColumn)+" IDENTITY PRIMARY KEY");
		return columns;
	}
	
	/**
	 * Note: The result of this method may be inaccurate
	 * @param jdbcType
	 * @return the mapped SQL-type
	 * @see java.sql.Types
	 */
	public static String getSQLType(int jdbcType) throws IllegalArgumentException
	{
		if(jdbcType == Types.SQLXML)
		{
			return "XML";
		}
		for(Field f: Types.class.getFields())
		{
			if(f.getType()==Integer.TYPE)
			{
				try
				{
					int val = f.getInt( null);
					if( val == jdbcType)
					{
						return f.getName().replaceAll( "_", " ");
					}
				}
				catch ( IllegalArgumentException | IllegalAccessException ex )
				{
				}
			}
		}
		throw new IllegalArgumentException("Unknown Type: "+jdbcType);
	}
	
	/**
	 * Note: The result of this method may be inaccurate
	 * @param javaType
	 * @return the mapped SQL-type
	 * @see http://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/jdbc/getstart/mapping.doc.html
	 * @see java.sql.Types
	 */
	public static String getSQLType(Class<?> javaType) throws IllegalArgumentException
	{
		if(javaType.equals( String.class))
		{
			return "LONGVARCHAR";
		}
		if(javaType.equals( java.math.BigDecimal.class))
		{
			return "NUMERIC";
		}
		if(javaType.equals( Boolean.class) || javaType.equals( Boolean.TYPE))
		{
			return "BIT";
		}
		if(javaType.equals( Byte.class) || javaType.equals( Byte.TYPE))
		{
			return "TINYINT";
		}
		if(javaType.equals( Short.class) || javaType.equals( Short.TYPE))
		{
			return "SHORTINT";
		}
		if(javaType.equals( Integer.class) || javaType.equals( Integer.TYPE))
		{
			return "INTEGER";
		}
		if(javaType.equals( Long.class) || javaType.equals( Long.TYPE))
		{
			return "BIGINT";
		}
		if(javaType.equals( Float.class) || javaType.equals( Float.TYPE))
		{
			return "REAL";
		}
		if(javaType.equals( Double.class) || javaType.equals( Double.TYPE))
		{
			return "DOUBLE";
		}
		if(javaType.equals( java.sql.Date.class))
		{
			return "DATE";
		}
		if(javaType.equals( java.sql.Time.class))
		{
			return "TIME";
		}
		if(javaType.equals( java.sql.Timestamp.class))
		{
			return "TIMESTAMP";
		}
		if(ActiveRecord.class.isAssignableFrom( javaType ))
		{
			//for foreign key
			return "INTEGER";
		}
		throw new IllegalArgumentException("Type not mapped: "+javaType);
	}
}
