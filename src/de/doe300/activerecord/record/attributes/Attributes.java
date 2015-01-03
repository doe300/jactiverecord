package de.doe300.activerecord.record.attributes;

import de.doe300.activerecord.record.ActiveRecord;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 *
 * @author doe300
 */
public final class Attributes
{

	/**
	 * Returns the name of the property, following the bean standard
	 * @param getterOrSetter
	 * @return the name of the property, or <code>null</code>
	 */
	public static String getPropertyName( Method getterOrSetter )
	{
		String result;
		if ( getterOrSetter.getName().startsWith( "is" ) )
		{
			result = getterOrSetter.getName().substring( 2 );
		}
		else if ( getterOrSetter.getName().startsWith( "get" ) || getterOrSetter.getName().startsWith( "set" ) )
		{
			result = getterOrSetter.getName().substring( 3 );
		}
		else
		{
			//Not a valid getter or setter
			return null;
		}
		if ( getterOrSetter.getName().equals( "get" ) || getterOrSetter.getName().equals( "set" ) )
		{
			//valid getter/setter but not handled by this handler
			return null;
		}
		StringBuilder res = new StringBuilder( result.length() );
		for ( char c : result.toCharArray() )
		{
			if ( Character.isUpperCase( c ) )
			{
				res.append( '_' );
			}
			res.append( c );
		}
		return res.deleteCharAt( 0 ).toString().toLowerCase();
	}

	/**
	 *
	 * @param base the value of base
	 * @return the validator-method or <code>null</code>
	 * @throws NoSuchMethodException
	 * @see AttributeSetter
	 */
	public static Method getValidatorMethod( Method base) throws NoSuchMethodException
	{
		if(base.isAnnotationPresent( AttributeSetter.class))
		{
			AttributeSetter setter = base.getAnnotation( AttributeSetter.class);
			if(setter.validatorClass().equals( Void.class))
			{
				return null;
			}
			return setter.validatorClass().getMethod( setter.validatorMethod(), Object.class );
		}
		return null;
	}

	/**
	 *
	 * @param method the value of method
	 * @param argType the value of argType
	 * @param includeAttributeSetter whether to also check for {@link AttributeSetter}
	 * @return the boolean
	 */
	public static boolean isSetter( Method method, Class<?> argType, boolean includeAttributeSetter)
	{
		if(includeAttributeSetter && method.isAnnotationPresent( AttributeSetter.class))
		{
			return true;
		}
		if ( !method.getName().startsWith( "set" ) || method.getName().equals( "set" ) || method.getParameterCount() != 1 )
		{
			return false;
		}
		if ( argType == null || method.getParameterTypes()[0].isAssignableFrom( argType ) )
		{
			return true;
		}
		if ( argType.isPrimitive() )
		{
			try
			{
				Field f = method.getParameterTypes()[0].getField( "TYPE" );
				return f != null && f.get( null ).equals( argType );
			}
			catch ( ReflectiveOperationException rfe )
			{
				return false;
			}
		}
		if ( method.getParameterTypes()[0].isPrimitive() )
		{
			try
			{
				Field f = argType.getField( "TYPE" );
				return f != null && f.get( null ).equals( method.getParameterTypes()[0] );
			}
			catch ( ReflectiveOperationException rfe )
			{
				return false;
			}
		}
		return false;
	}

	/**
	 *
	 * @param method the value of method
	 * @param includeAttributeGetter whether to also check for {@link AttributeGetter}
	 * @return the boolean
	 */
	public static boolean isGetter( Method method, boolean includeAttributeGetter )
	{
		if(includeAttributeGetter && method.isAnnotationPresent( AttributeGetter.class))
		{
			return true;
		}
		return method.getName().startsWith( "get" ) && method.getName().length() > "get".length() &&
				method.getParameterCount() == 0 && method.getReturnType() != Void.TYPE;
	}

	/**
	 *
	 * @param base the value of base
	 * @return the converter-method or <code>null</code>
	 * @throws NoSuchMethodException
	 * @see AttributeGetter
	 * @see AttributeSetter
	 */
	public static Method getConverterMethod( Method base) throws NoSuchMethodException
	{
		if ( base.isAnnotationPresent( AttributeGetter.class))
		{
			AttributeGetter getter = base.getAnnotation( AttributeGetter.class);
			if(getter.converterClass().equals( Void.class))
			{
				return null;
			}
			return getter.converterClass().getMethod( getter.converterMethod(), Object.class);
		}
		if ( base.isAnnotationPresent( AttributeSetter.class))
		{
			AttributeSetter setter = base.getAnnotation( AttributeSetter.class);
			if(setter.converterClass().equals( Void.class))
			{
				return null;
			}
			return setter.converterClass().getMethod( setter.converterMethod(), Object.class);
		}
		return null;
	}
	
	/**
	 * @param record
	 * @param attribute
	 * @param converterFunc (optional) function to convert data before checking
	 * @return whether the attribute-value is not <code>null</code>
	 */
	public static boolean checkNotNull(ActiveRecord record, String attribute, Function<Object,Object> converterFunc)
	{
		return checkAttribute( record, attribute, (Object o) -> o!= null, converterFunc);
	}
	
	/**
	 * @param record
	 * @param attribute
	 * @param checkFunc
	 * @param converterFunc (optional) function to convert data before checking
	 * @return whether the attribute-value matches the <code>checkFunc</code>
	 */
	public static boolean checkAttribute(ActiveRecord record, String attribute, Predicate<Object> checkFunc, Function<Object,Object> converterFunc)
	{
		Object val = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), attribute);
		if(converterFunc!=null)
		{
			val = converterFunc.apply( val );
		}
		return checkFunc.test( val );
	}
	
	/**
	 * For common data-types ({@link String}, {@link Collection}, {@link Map}, {@link Array}) the length-function is provided out of the box.
	 * For any other type, the <code>checkFunc</code> must be specified.
	 * @param record
	 * @param attribute
	 * @param lengthFunc (optional) function to determine length
	 * @param converterFunc (optional) function to convert data before determining length
	 * @return the length of the attribute-value or <code>-1</code>
	 */
	public static int getLength(ActiveRecord record, String attribute, ToIntFunction<Object> lengthFunc, Function<Object,Object> converterFunc)
	{
		Object val = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), attribute);
		if(converterFunc!=null)
		{
			val = converterFunc.apply( val );
		}
		if(val == null)
		{
			return -1;
		}
		if(val instanceof String)
		{
			return ((String)val).length();
		}
		if(val.getClass().isArray())
		{
			return Array.getLength( val);
		}
		if(val instanceof Collection)	
		{
			return ((Collection)val).size();
		}
		if(val instanceof Map)	
		{
			return ((Map)val).size();
		}
		return -1;
	}
	
	private Attributes()
	{
	}

}
