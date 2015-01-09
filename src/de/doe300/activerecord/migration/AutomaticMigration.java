package de.doe300.activerecord.migration;

import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.attributes.Attributes;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author doe300
 */
public class AutomaticMigration implements Migration
{
	private final Class<? extends ActiveRecord> recordType;

	public AutomaticMigration(Class<? extends ActiveRecord> recordType )
	{
		this.recordType = recordType;
	}

	@Override
	public <T extends ActiveRecord> boolean apply( Connection con ) throws Exception
	{
		String tableName = getTableName( recordType );
		//1. check if table exists
		if(structureExists( con, tableName))
		{
			return false;
		}
		//2. get desired columns and types
		Map<String,String> columns = new HashMap<>(10);
		Method[] methods = recordType.getMethods();
		for(Method method:methods)
		{
			if(method.isDefault() || (method.getModifiers() & Modifier.STATIC) == Modifier.STATIC || (method.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC)
			{
				continue;
			}
			//2.1 get attributes
			if(method.isAnnotationPresent( Attribute.class))
			{
				Attribute att = method.getAnnotation( Attribute.class);
				if(!"".equals( att.typeName() ))
				{
					columns.put( att.name(), att.typeName());
				}
				else
				{
					columns.put( att.name(), getSQLType( att.type()));
				}
			}
			//2.2 get attribute-accessors
			else if(method.isAnnotationPresent( AttributeGetter.class))
			{
				AttributeGetter acc = method.getAnnotation( AttributeGetter.class);
				Method converter = Attributes.getConverterMethod( method);
				Class<?> attType = null;
				if(converter != null)
				{
					attType = converter.getParameterTypes()[0];
				}
				else
				{
					attType = method.getReturnType();
				}
			}
			else if(method.isAnnotationPresent( AttributeSetter.class))
			{
				AttributeSetter acc = method.getAnnotation( AttributeSetter.class);
				Method converter = Attributes.getConverterMethod( method);
				Class<?> attType = null;
				if(converter != null)
				{
					attType = converter.getReturnType();
				}
				else
				{
					attType = method.getParameterTypes()[0];
				}
			}
			//2.3 get bean accessors
			else
			{
				
			}
		}
		
		
		//2.4 add timestamps, other features
		//3. mark primary key, add constraints
		//4. execute statement
		throw new Exception();
	}

	@Override
	public <T extends ActiveRecord> boolean revert( Connection con ) throws Exception
	{
		//TODO drop table
		throw new Exception();
	}

	private String getTableName(Class<? extends ActiveRecord> type)
	{
		if(type.getClass().isAnnotationPresent(RecordType.class))
		{
			return type.getClass().getAnnotation(RecordType.class).typeName();
		}
		return type.getClass().getSimpleName();
	}
	
	/**
	 * Note: The result of this method may be inaccurate
	 * @param jdbcType
	 * @return the mapped SQL-type
	 * @see http://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/jdbc/getstart/mapping.doc.html
	 * @see java.sql.Types
	 */
	public static String getSQLType(int jdbcType)
	{
		switch(jdbcType)
		{
			case Types.ARRAY:
			case Types.BIGINT:
			case Types.BINARY:
			case Types.BIT:
			case Types.BLOB
		}
		if(type.equals( String.class))
		{
			return "LONGVARCHAR";
		}
		if(type.equals( java.math.BigDecimal.class))
		{
			return "NUMERIC";
		}
		if(type.equals( Boolean.class) || type.equals( Boolean.TYPE))
		{
			return "BIT";
		}
		if(type.equals( Byte.class) || type.equals( Byte.TYPE))
		{
			return "TINYINT";
		}
		if(type.equals( Short.class) || type.equals( Short.TYPE))
		{
			return "SHORTINT";
		}
		if(type.equals( Integer.class) || type.equals( Integer.TYPE))
		{
			return "INTEGER";
		}
		if(type.equals( Long.class) || type.equals( Long.TYPE))
		{
			return "BIGINT";
		}
		if(type.equals( Float.class) || type.equals( Float.TYPE))
		{
			return "REAL";
		}
		if(type.equals( Double.class) || type.equals( Double.TYPE))
		{
			return "DOUBLE";
		}
		if(type.equals( java.sql.Date.class))
		{
			return "DATE";
		}
		if(type.equals( java.sql.Time.class))
		{
			return "TIME";
		}
		if(type.equals( java.sql.Timestamp.class))
		{
			return "TIMESTAMP";
		}
		throw new IllegalArgumentException("Type not mapped: "+type);
	}
	
	/**
	 * Note: The result of this method may be inaccurate
	 * @param javaType
	 * @return the mapped JDBC-type
	 * @see http://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/jdbc/getstart/mapping.doc.html
	 * @see java.sql.Types
	 */
	public static String getJDBCType(Class<?> javaType)
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
		throw new IllegalArgumentException("Type not mapped: "+javaType);
	}
}
