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
package de.doe300.activerecord.record.attributes;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.record.ActiveRecord;

/**
 *
 * @author doe300
 */
public final class Attributes
{

	/**
	 * Returns the name of the property, following the bean standard
	 * @param methodName
	 * @return the name of the property, or <code>null</code>
	 */
	@Nullable
	public static String getPropertyName(@Nonnull final String methodName)
	{
		String result;
		if ( methodName.startsWith( "is" ) )
		{
			result = methodName.substring( 2 );
		}
		else if ( methodName.startsWith( "get" ) || methodName.startsWith( "set" ) )
		{
			result = methodName.substring( 3 );
		}
		else
		{
			//Not a valid getter or setter
			return null;
		}
		if ( methodName.equals( "get" ) || methodName.equals( "set" ) )
		{
			//valid getter/setter but not handled by this handler
			return null;
		}
		final StringBuilder res = new StringBuilder( result.length() );
		for ( final char c : result.toCharArray() )
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
	 * Converts the attribute to camel-case to be used for generating getter/setter
	 * 
	 * @param attribute
	 * @return the attribute in camel-case
	 * @since 0.3
	 */
	@Nonnull
	public static String toCamelCase(@Nonnull final String attribute)
	{
		final StringBuilder res = new StringBuilder( attribute.length() );
		boolean nextUpperCase = true;
		//skip leading and trailing whitespaces
		for ( final char c : attribute.trim().toCharArray() )
		{
			if( Character.isWhitespace( c))
			{
				//whitespaces in the attribute are errors
				throw new IllegalArgumentException("Attribute-name can't contain whitespaces!");
			}
			//convert '_' to camel-case
			if ( c == '_' )
			{
				nextUpperCase = true;
				continue;
			}
			if(nextUpperCase)
			{
				res.append( Character.toUpperCase( c));
				nextUpperCase = false;
			}
			else
			{
				res.append( c );
			}
		}
		return res.toString();
	}

	/**
	 *
	 * @param base the value of base
	 * @return the validator-method or <code>null</code>
	 * @throws NoSuchMethodException
	 * @see AttributeSetter
	 */
	@Nullable
	public static Method getValidatorMethod(@Nonnull final Method base) throws NoSuchMethodException
	{
		if(base.isAnnotationPresent( AttributeSetter.class))
		{
			final AttributeSetter setter = base.getAnnotation( AttributeSetter.class);
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
	public static boolean isSetter(@Nonnull final Method method, @Nullable final Class<?> argType,
		final boolean includeAttributeSetter)
	{
		if(includeAttributeSetter && method.isAnnotationPresent( AttributeSetter.class))
		{
			return true;
		}
		if ( !method.getName().startsWith( "set" ) || method.getName().equals( "set" ) || method.getParameterCount() != 1 )
		{
			return false;
		}
		if ( argType == null || method.getParameterTypes()[0].isAssignableFrom(argType))
		{
			return true;
		}
		if ( argType.isPrimitive() )
		{
			try
			{
				final Field f = method.getParameterTypes()[0].getField( "TYPE" );
				return f != null && f.get( null ).equals( argType );
			}
			catch ( final ReflectiveOperationException rfe )
			{
				return false;
			}
		}
		if ( method.getParameterTypes()[0].isPrimitive() )
		{
			try
			{
				final Field f = argType.getField( "TYPE" );
				return f != null && f.get( null ).equals( method.getParameterTypes()[0] );
			}
			catch ( final ReflectiveOperationException rfe )
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
	public static boolean isGetter(@Nonnull final Method method, final boolean includeAttributeGetter)
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
	@Nullable
	public static Method getConverterMethod(@Nonnull final Method base) throws NoSuchMethodException
	{
		if ( base.isAnnotationPresent( AttributeGetter.class))
		{
			final AttributeGetter getter = base.getAnnotation( AttributeGetter.class);
			if(getter.converterClass().equals( Void.class))
			{
				return null;
			}
			return getter.converterClass().getMethod( getter.converterMethod(), Object.class);
		}
		if ( base.isAnnotationPresent( AttributeSetter.class))
		{
			final AttributeSetter setter = base.getAnnotation( AttributeSetter.class);
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
	public static boolean checkNotNull(@Nonnull final ActiveRecord record, @Nonnull final String attribute,
		@Nullable final Function<Object, Object> converterFunc)
	{
		return Attributes.checkAttribute( record, attribute, (final Object o) -> o!= null, converterFunc);
	}

	/**
	 * @param record
	 * @param attribute
	 * @param checkFunc
	 * @param converterFunc (optional) function to convert data before checking
	 * @return whether the attribute-value matches the <code>checkFunc</code>
	 */
	public static boolean checkAttribute(@Nonnull final ActiveRecord record, @Nonnull final String attribute,
		@Nonnull final Predicate<Object> checkFunc, @Nullable final Function<Object, Object> converterFunc)
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
	public static int getLength(@Nonnull final ActiveRecord record, @Nonnull final String attribute,
		@Nullable final ToIntFunction<Object> lengthFunc, @Nullable final Function<Object, Object> converterFunc)
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
		if (lengthFunc != null)
		{
			return lengthFunc.applyAsInt(val);
		}
		return -1;
	}

	private Attributes()
	{
	}

}
