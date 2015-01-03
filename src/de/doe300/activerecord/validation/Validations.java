package de.doe300.activerecord.validation;

import java.util.function.Predicate;

/**
 *
 * @author doe300
 */
public final class Validations
{
	public static boolean notNull(Object obj)
	{
		return obj != null;
	}
	
	public static boolean notEmpty(String s)
	{
		return !s.isEmpty();
	}
	
	public static void validate(String column, Object value, Predicate<Object> pred, String message) throws ValidationFailed
	{
		if(pred.test( value ))
		{
			return;
		}
		throw new ValidationFailed(column, value, message);
	}
	
	public static <T> boolean isValid(T value, Predicate<? super T> pred)
	{
		return pred.test( value );
	}
	
	private Validations()
	{
	}
}
