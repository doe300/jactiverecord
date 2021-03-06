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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.migration.constraints.Index;
import de.doe300.activerecord.migration.constraints.ReferenceRule;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.association.RecordSet;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.attributes.Attributes;
import de.doe300.activerecord.store.JDBCRecordStore;
import javax.annotation.Nonnull;

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
 *			<li>Use the {@link Attribute#type() }, map it via {@link JDBCDriver#getSQLType(Class) } and finish</li>
 *		</ol>
 *	</li>
 *	<li>Ignore method, if it is <code>static</code> or <code>default</code>.</li>
 *	<li>If the method is annotated by {@link AttributeGetter}, use {@link AttributeGetter#name() } as column-name<br>
 *		Use the methods return-type, map it via {@link JDBCDriver#getSQLType(Class) } and finish
 *	</li>
 *	<li>If the method is annotated by {@link AttributeSetter}, use {@link AttributeSetter#name() } as column-name<br>
 *		Use the methods only parameter, map it via {@link JDBCDriver#getSQLType(Class) } and finish
 *	</li>
 *	<li>Use the methods property-name as column-name and its return-type (or only parameter) as data-type, map it via {@link JDBCDriver#getSQLType(Class) } and finish</li>
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
	protected final Class<? extends ActiveRecord> recordType;
	protected final String tableName;
	protected final JDBCRecordStore store;
	protected final JDBCDriver driver;

	/**
	 * @param recordType the type to create and drop the table for
	 * @param store the SQL-store
	 * @param driver the vendor-specific driver for the vendor used (if known)
	 */
	public AutomaticMigration(@Nonnull final Class<? extends ActiveRecord> recordType, @Nullable final JDBCRecordStore store, @Nonnull final JDBCDriver driver )
	{
		this.recordType = recordType;
		this.tableName = getTableName( recordType );
		this.store = store;
		this.driver = driver;
	}

	/**
	 * @param recordType the type to create and drop the table for
	 * @param tableName the table-name to use
	 * @param store the SQL-store
	 * @param driver the vendor-specific driver for the vendor used (if known)
	 * @since 0.7
	 */
	public AutomaticMigration(@Nonnull final Class<? extends ActiveRecord> recordType, @Nonnull final String tableName, @Nullable final JDBCRecordStore store, @Nonnull final JDBCDriver driver )
	{
		this.recordType = recordType;
		this.tableName = tableName;
		this.store = store;
		this.driver = driver;
	}

	@Override
	public boolean apply() throws SQLException
	{
		//1. check if table exists
		if(store.exists( tableName))
		{
			return false;
		}
		//2. get desired columns and types
		final Map<String,String> columns = getColumnsFromModel( recordType );
		//3. execute statement
		final String sql = "CREATE TABLE "+tableName+" ("
			+columns.entrySet().stream().map( (final Map.Entry<String,String> e) -> JDBCDriver.convertIdentifier( e.getKey(), store.getConnection())+" "+e.getValue())
			.collect( Collectors.joining(", "))
			+" )";
		Logging.getLogger().info( recordType.getSimpleName(), "Executing automatic table-creation...");
		Logging.getLogger().info( recordType.getSimpleName(), sql);
		try(Statement stm = store.getConnection().createStatement())
		{
			if(stm.executeUpdate(sql)<0)
			{
				Logging.getLogger().error( recordType.getSimpleName(), "Automatic table-creation failed!");
				return false;
			}
			//4. add indices
			final Index[] indices = recordType.getAnnotationsByType( Index.class);
			if(indices.length > 0)
			{
				final String indicesSQL = Arrays.stream( indices ).map( (final Index index)-> index.type().toSQL(driver, tableName, index.name(), index.columns())).collect( Collectors.joining( "; "));
				Logging.getLogger().info( recordType.getSimpleName(), "Adding indices...");
				Logging.getLogger().info( recordType.getSimpleName(), indicesSQL);
				try(final Statement indicesStm = store.getConnection().createStatement())
				{
					if(indicesStm.executeUpdate( indicesSQL) < 0)
					{
						Logging.getLogger().error( recordType.getSimpleName(), "Adding indices failed!");
					}
				}
			}
		}
		catch(final SQLException e)
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
	 * NOTE: If <code>dropColumns</code> is set to <code>true</code>, this method will drop all columns it deems not used anymore!
	 * @param dropColumns whether to drop unused columns
	 * @return whether the table was updated
	 * @throws java.sql.SQLException
	 */
	@Override
	public boolean update(final boolean dropColumns) throws SQLException
	{
		//1. check if table exists
		if(store.exists( tableName ))
		{
			return false;
		}
		//2. get existing columns
		final Map<String,String> hasColumns = new HashMap<>(10);
		try(ResultSet set = store.getConnection().getMetaData().getColumns( store.getConnection().getCatalog(), store.getConnection().getSchema(), tableName, null))
		{
			while(set.next())
			{
				hasColumns.put( set.getString( "COLUMN_NAME").toLowerCase(), set.getString( "TYPE_NAME"));
			}
		}
		//3. get desired columns
		final Map<String,String> desiredColumns = getColumnsFromModel( recordType );
		//4. calculate difference
		final Map<String,String> removeColumns = hasColumns.entrySet().stream().
			filter( (final Map.Entry<String,String> e) -> desiredColumns.containsKey( e.getKey())).collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue));
		final Map<String,String> addColumns = desiredColumns.entrySet().stream().
			filter( (final Map.Entry<String,String> e) -> hasColumns.containsKey( e.getKey())).collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue));
		//5. execute updates
		boolean changed = false;
		//SQL needs extra statements for add and drop columns
		if (dropColumns && !removeColumns.isEmpty())
		{
			final String sql = "ALTER TABLE "+tableName+" ("+
				"DROP COLUMN "+
				removeColumns.entrySet().stream().map( (final Map.Entry<String,String> e) -> JDBCDriver.convertIdentifier( e.getKey(), store.getConnection())).collect( Collectors.joining( ", "))+
				")";
			Logging.getLogger().info(recordType.getSimpleName(), "Executing automatic table-update...");
			Logging.getLogger().info(recordType.getSimpleName(), sql);
			try(Statement stm = store.getConnection().createStatement())
			{
				changed = stm.executeUpdate(sql) >= 0;
			}
		}
		if(!addColumns.isEmpty())
		{
			final String sql = "ALTER TABLE "+tableName+" ("+
				"ADD "+
				addColumns.entrySet().stream().map( (final Map.Entry<String,String> e) -> JDBCDriver.convertIdentifier( e.getKey(), store.getConnection())+" "+e.getValue()).collect( Collectors.joining( ", "))+
				")";
			Logging.getLogger().info(recordType.getSimpleName(), "Executing automatic table-update...");
			Logging.getLogger().info(recordType.getSimpleName(), sql);
			try(Statement stm = store.getConnection().createStatement())
			{
				changed = changed || stm.executeUpdate(sql) >= 0;
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
	 * @return whether the table existed and was dropped
	 * @throws SQLException
	 */
	@Override
	public boolean revert() throws SQLException
	{
		//1. check if table exists
		if(!store.exists( tableName ))
		{
			return false;
		}
		//2. drop table
		return store.dropTable( tableName );
	}

	private static String getTableName(final Class<? extends ActiveRecord> type)
	{
		if(type.isAnnotationPresent(RecordType.class))
		{
			return type.getAnnotation(RecordType.class).typeName();
		}
		return type.getSimpleName();
	}

	protected String getPrimaryColumn(final Class<? extends ActiveRecord> type)
	{
		if(type.isAnnotationPresent(RecordType.class))
		{
			return type.getAnnotation(RecordType.class).primaryKey();
		}
		return ActiveRecord.DEFAULT_PRIMARY_COLUMN;
	}

	/**
	 * The column-names are all in lower case
	 * @param recordType
	 * @return the columns
	 */
	protected Map<String,String> getColumnsFromModel(final Class<? extends ActiveRecord> recordType) throws IllegalArgumentException
	{
		//TODO move ID to first column
		final HashMap<String,String> columns = new HashMap<>(10);
		final Method[] methods = recordType.getMethods();
		for(final Method method:methods)
		{
			//1. get attributes
			//this is priorized before skipping of default methods, so columns for associations are created correctly
			if(method.isAnnotationPresent( Attribute.class))
			{
				final Attribute att = method.getAnnotation( Attribute.class);
				final String name = att.name().toLowerCase();
				Class<?> attributeType = null;
				if(!"".equals( att.typeName() ))
				{
					attributeType = !Void.class.equals( att.type()) ? att.type() :
						Attributes.isGetter( method, false) ? method.getReturnType() : Attributes.isSetter(
							method, null, false) ? method.getParameterTypes()[0] : null;
							columns.put(name, att.typeName());
				}
				//Attribute#type is optional
				else if(!Void.class.equals( att.type() ))
				{
					attributeType = att.type();
					columns.putIfAbsent(name, driver.getSQLType( attributeType));
				}
				//fall back to check getter-type
				else if(Attributes.isGetter( method, false ))
				{
					attributeType = method.getReturnType();
					columns.putIfAbsent( name, driver.getSQLType( attributeType));
				}
				//... or setter-type
				else if(Attributes.isSetter( method, null, false))
				{
					attributeType = method.getParameterTypes()[0];
					columns.putIfAbsent( name, driver.getSQLType( attributeType));
				}
				if(attributeType == null)
				{
					throw new IllegalArgumentException("Could not deduce type for attribute: " + name);
				}
				//deduce references (with standard primary key) from class of associated record-type
				final String foreignKeyTable, foreignKeyColumn;
				if(!att.foreignKeyTable().isEmpty())
				{
					foreignKeyTable = att.foreignKeyTable();
					foreignKeyColumn = att.foreignKeyColumn();
				}
				else if(ActiveRecord.class.isAssignableFrom( attributeType )) //try to deduce foreign key table from attribute-type
				{
					//TODO always references to base table for sharded tables
					final RecordType associatedType = attributeType.getAnnotation( RecordType.class);
					foreignKeyTable = associatedType != null ? associatedType.typeName() : attributeType.getSimpleName();
					foreignKeyColumn = !att.foreignKeyColumn().isEmpty() ? att.foreignKeyColumn() : associatedType != null ? associatedType.primaryKey() : null;
				}
				else
				{
					foreignKeyTable = null;
					foreignKeyColumn = null;
				}
				final String prevValue = columns.get( name);
				if(prevValue.contains( " NULL"))
				{
					//makes sure, modifiers are not added twice
					//for annotated getter and setter
					continue;
				}
				columns.put( name, prevValue
					+(!"".equals( att.defaultValue() )?" DEFAULT "+att.defaultValue(): "")
					+(att.mayBeNull()?" NULL": " NOT NULL")
					+(att.isUnique()?" UNIQUE": "")
					+(foreignKeyTable == null ? "" : " REFERENCES "+foreignKeyTable
						+(foreignKeyColumn == null || foreignKeyColumn.isEmpty() ? "" : " ("+foreignKeyColumn+")")
						+att.onUpdate().toSQL( ReferenceRule.ACTION_UPDATE)
						+att.onDelete().toSQL( ReferenceRule.ACTION_DELETE)
						)
					+(att.checkConstraint().isEmpty() ? "" : " CHECK("+att.checkConstraint()+")"));
				continue;
			}
			//skip getClass() - for POJO records
			if(method.getDeclaringClass().equals( Object.class))
			{
				continue;
			}
			//skip default or static methods
			if(method.isDefault() || (method.getModifiers() & Modifier.STATIC) == Modifier.STATIC || (method.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC)
			{
				continue;
			}
			//skip methods from ActiveRecord (#getPrimaryKey() and #getBase()) - for interface-records
			if(method.getDeclaringClass().equals( ActiveRecord.class))
			{
				continue;
			}
			//skip methods from ActiveRecord (#getPrimaryKey() and #getBase()) - for POJO records
			if(method.getName().equals( "getPrimaryKey") && method.getReturnType().equals( Integer.TYPE) || method.getName().equals( "getBase") && RecordBase.class.isAssignableFrom( method.getReturnType()))
			{
				continue;
			}
			//skip associations - for POJO records
			if(RecordSet.class.isAssignableFrom( method.getReturnType()))
			{
				continue;
			}
			//skip all methods annotated with ExcludeAttribute
			if(method.isAnnotationPresent( ExcludeAttribute.class))
			{
				continue;
			}
			String columnName = null;
			Class<?> attType = null;
			//2. get attribute-accessors
			if(method.isAnnotationPresent( AttributeGetter.class))
			{
				final AttributeGetter acc = method.getAnnotation( AttributeGetter.class);
				columnName = acc.name().toLowerCase();
				attType = method.getReturnType();
			}
			else if(method.isAnnotationPresent( AttributeSetter.class))
			{
				final AttributeSetter acc = method.getAnnotation( AttributeSetter.class);
				columnName = acc.name().toLowerCase();
				attType = method.getParameterTypes()[0];
			}
			//3. get bean accessors
			else if(Attributes.isGetter( method, false))
			{
				columnName = Attributes.getPropertyName( method.getName() );
				attType = method.getReturnType();
			}
			else if(Attributes.isSetter( method, null, false))
			{
				columnName = Attributes.getPropertyName( method.getName() );
				attType = method.getParameterTypes()[0];
			}
			//convert type (for 2. and 3.) only if not yet registered
			if(columnName!=null && attType!=null && !columns.containsKey( columnName))
			{
				columns.put(columnName, driver.getSQLType(attType));
				try
				{
					if(driver.isReservedKeyword( columnName, store == null ? null : store.getConnection()))
					{
						throw new IllegalArgumentException("Column-name is reserved keyword: " + columnName);
					}
				}
				catch (final SQLException ex )
				{
					throw new IllegalArgumentException(ex);
				}
			}
		}
		//4. add timestamps, other features
		if(TimestampedRecord.class.isAssignableFrom( recordType))
		{
			//forces the timestamps to be overriden, because they need to be of type Timestamp
			columns.put(TimestampedRecord.COLUMN_CREATED_AT, driver.getSQLType(Timestamp.class));
			columns.put(TimestampedRecord.COLUMN_UPDATED_AT, driver.getSQLType(Timestamp.class));
		}
		//5. mark or add primary key, add constraints
		final String primaryColumn = getPrimaryColumn( recordType).toLowerCase();
		columns.putIfAbsent(primaryColumn, driver.getSQLType(Integer.class));
		columns.put(primaryColumn, driver.getPrimaryKeyKeywords( columns.get( primaryColumn)));
		return columns;
	}

}
