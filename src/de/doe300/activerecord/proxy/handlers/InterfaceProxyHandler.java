package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Base-class for dummy-objects implementing some interfaces used by an activerecord
 * @author doe300
 * @param <T>
 */
public abstract class InterfaceProxyHandler<T extends ActiveRecord> implements ProxyHandler
{
	private final Class<T> type;

	public InterfaceProxyHandler( Class<T> type)
	{
		this.type = type;
	}

	@Override
	public <U extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<U> handler,Method method, Object[] args ) throws IllegalArgumentException
	{
		try
		{
			return method.invoke( this, record, handler, args);
		}
		catch ( IllegalAccessException | InvocationTargetException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		for(Method m:type.getMethods())
		{
			if(m.getName().equals( method.getName()) && Arrays.equals( m.getParameterTypes(), getArgumentsTypes( args)))
			{
				return true;
			}
		}
		return false;
	}
	
	private static Class<?>[] getArgumentsTypes(Object... args)
	{
		Class<?>[] array = new Class<?>[args.length+2];
		array[0] = ActiveRecord.class;
		array[1] = RecordHandler.class;
		
		for(int i=0;i<args.length;i++)
		{
			array[i+2] = args.getClass();
		}
		
		return array;
	}
}
