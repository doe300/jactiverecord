package de.doe300.activerecord.migration;

import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.DataSet;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.attributes.Attributes;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
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
		Map<String,Integer> columns = new HashMap<>(10);
		Method[] methods = recordType.getMethods();
		for(Method method:methods)
		{
			if(method.isDefault() || (method.getModifiers() & Modifier.STATIC) == Modifier.STATIC || (method.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC)
			{
				continue;
			}
			//2.1 get attribute-accessors
			if(method.isAnnotationPresent( AttributeGetter.class))
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
			//2.2 get bean accessors
			else
			{
				
			}
		}
		
		
		//2.3 add timestamps, other features
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
		if(type.getClass().isAnnotationPresent( DataSet.class))
		{
			return type.getClass().getAnnotation( DataSet.class).dataSet();
		}
		return type.getClass().getSimpleName();
	}
}
