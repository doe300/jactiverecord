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
package de.doe300.activerecord.record.validation;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.record.ActiveRecord;

/**
 *
 * @author doe300
 */
public final class Validations
{
	/**
	 * Tests whether the object is not empty.
	 * This test will only work for String, array, Collection, Map and Iterable
	 * @param obj
	 * @return whether the object is not empty
	 * @throws IllegalArgumentException
	 */
	public static boolean notEmpty(@Nullable final Object obj)
	{
		if(obj == null)
		{
			return false;
		}
		if(obj instanceof String)
		{
			return !((String)obj).isEmpty();
		}
		if(obj instanceof Collection)
		{
			return !((Collection)obj).isEmpty();
		}
		if(obj.getClass().isArray())
		{
			return Array.getLength( obj )!=0;
		}
		if(obj instanceof Map)
		{
			return !((Map)obj).isEmpty();
		}
		if(obj instanceof Iterable)
		{
			return ((Iterable)obj).iterator().hasNext();
		}
		if(obj instanceof Number)
		{
			final double d = ((Number)obj).doubleValue();
			return d != 0.0;
		}
		throw new IllegalArgumentException("Invalid data-type, can't check empty for: "+ obj.getClass());
	}

	/**
	 * @param obj
	 * @return whether the object is empty
	 * @see #notEmpty(java.lang.Object)
	 */
	public static boolean isEmpty(@Nullable final Object obj)
	{
		return !Validations.notEmpty( obj );
	}

	/**
	 * @param obj
	 * @return whether this object is a positive number
	 * @throws IllegalArgumentException if the parameter is not a number
	 */
	public static boolean positiveNumber(@Nullable final Object obj)
	{
		if(obj == null)
		{
			return false;
		}
		if(obj instanceof Number)
		{
			return ((Number)obj).doubleValue() > 0;
		}
		throw new IllegalArgumentException("Can only determine positive for numbers");
	}

	/**
	 *
	 * @param obj
	 * @return whether the parameter is a negative number
	 * @throws IllegalArgumentException if the object is no number
	 */
	public static boolean negativeNumber(@Nullable final Object obj)
	{
		if(obj == null)
		{
			return false;
		}
		if(obj instanceof Number)
		{
			return ((Number)obj).doubleValue() < 0;
		}
		throw new IllegalArgumentException("Can only determine negative for numbers");
	}

	/**
	 * @param column
	 * @param value
	 * @param pred
	 * @param message
	 * @throws ValidationFailed if the validation failed
	 */
	public static void validate(@Nonnull final String column, @Nullable final Object value,
		@Nonnull final Predicate<Object> pred, final String message) throws ValidationFailed
	{
		if(pred.test( value ))
		{
			return;
		}
		throw new ValidationFailed(column, value, message);
	}

	/**
	 * @param validate
	 * @return the BiPredicate for validation
	 */
	@Nonnull
	public static BiPredicate<ActiveRecord, Object> getValidationMethod(@Nonnull final Validate validate)
	{
		switch(validate.type())
		{
			case IS_NULL:
				return (final ActiveRecord rec, final Object obj) -> obj == null;
			case IS_EMPTY:
				return (final ActiveRecord rec, final Object obj) -> Validations.isEmpty( obj);
			case NOT_NULL:
				return (final ActiveRecord rec, final Object obj) -> obj != null;
			case NOT_EMPTY:
				return (final ActiveRecord rec, final Object obj) -> Validations.notEmpty( obj);
			case POSITIVE:
				return (final ActiveRecord rec, final Object obj) -> Validations.positiveNumber( obj);
			case NEGATIVE:
				return (final ActiveRecord rec, final Object obj) -> Validations.negativeNumber( obj);
			case CUSTOM:
			default:
				try{
					final Method m = validate.customClass().getMethod( validate.customMethod(), Object.class);
					return (final ActiveRecord record, final Object obj) -> {
						try
						{
							return (boolean) m.invoke( record, obj);
						}
						catch ( final ReflectiveOperationException ex )
						{
							throw new RuntimeException("Error while running custom validation-method", ex);
						}
					};
				}
				catch(final ReflectiveOperationException roe)
				{
					throw new IllegalArgumentException("Could not determine custom validation-method", roe);
				}

		}
	}

	private Validations()
	{
	}
}
