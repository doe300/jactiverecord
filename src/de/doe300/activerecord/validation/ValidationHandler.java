package de.doe300.activerecord.validation;

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.lang.reflect.Method;

/**
 *
 * @author doe300
 */
public abstract class ValidationHandler implements ProxyHandler
{

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		return method.getDeclaringClass() == ValidatedRecord.class;
	}

	@Override
	public <T extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<T> handler,
			Method method, Object[] args ) throws IllegalArgumentException
	{
		try
		{
			if(method.equals( ValidatedRecord.class.getMethod( "isValid")))			
			{
				return isValid( record );
			}
			if(method.equals( ValidatedRecord.class.getMethod( "validate")))
			{
				validate( record );
			}
			throw new IllegalArgumentException("Method is not handled by this handler");
		}
		catch ( NoSuchMethodException | SecurityException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	public abstract boolean isValid(ActiveRecord record);
	
	public abstract void validate(ActiveRecord record) throws ValidationFailed;
}
