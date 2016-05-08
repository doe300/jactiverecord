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
package de.doe300.activerecord.dsl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 *
 * @author doe300
 */
public enum Comparison implements BiPredicate<Object, Object>
{
	/**
	 * Exact match of the two values
	 */
	IS {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			if(value instanceof Number && compareValue instanceof Number )
			{
				return ((Number)value).doubleValue() == ((Number)compareValue).doubleValue();
			}
			return Objects.equals( value, compareValue);
		}
	},
	/**
	 * Non-match of the values
	 * @see #IS
	 */
	IS_NOT {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			if(value instanceof Number && compareValue instanceof Number )
			{
				return ((Number)value).doubleValue() != ((Number)compareValue).doubleValue();
			}
			return !Objects.equals( value, compareValue);
		}
	},
	/**
	 * Regex-match (SQL LIKE-Statement).
	 * This comparison uses <code>%</code> as wildcard.
	 */
	LIKE {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			if (value == null || compareValue == null)
			{
				return false;
			}
			if(value instanceof String)
			{
				return Pattern.matches(((String)compareValue).replaceAll( "%", ".*"),(String)value );
			}
			return false;
		}
	},
	/**
	 * Whether the first argument is <code>null</code>.
	 */
	IS_NULL {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			return value == null;
		}
	},
	/**
	 * Whether the first value is not <code>null</code>
	 * @see #IS_NULL
	 */
	IS_NOT_NULL {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			return value != null;
		}
	},
	/**
	 * Returns, whether the first value is larger than the second
	 * @see Comparable
	 */
	LARGER {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			if(value == null || compareValue == null)
			{
				return false;
			}
			return Comparable.class.cast(value).compareTo(compareValue) > 0;
		}
	},
	/**
	 * Returns, whether the first value is larger or equal to the second one
	 * @see Comparable
	 */
	LARGER_EQUALS {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			if(value == null || compareValue == null)
			{
				return false;
			}
			return Comparable.class.cast(value).compareTo(compareValue) >= 0;
		}
	},
	/**
	 * Returns whether the first value is smaller than the second
	 * @see Comparable
	 */
	SMALLER {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			if(value == null || compareValue == null)
			{
				return false;
			}
			return Comparable.class.cast(value).compareTo(compareValue) < 0;
		}
	},
	/**
	 * Returns whether the first value is smaller or equal to the second
	 * @see Comparable
	 */
	SMALLER_EQUALS {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			if(value == null || compareValue == null)
			{
				return false;
			}
			return Comparable.class.cast(value).compareTo(compareValue) <= 0;
		}
	},
	/**
	 * always returns true
	 */
	TRUE {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			return true;
		}
	},
	/**
	 * Checks whether the first value is in collection specified by the second value.
	 * Supported collection-types are: Array and {@link Collection}.
	 */
	IN {

		@Override
		public boolean test( final Object value, final Object compareValue )
		{
			if(Objects.equals( value, compareValue))
			{
				return true;
			}
			if(compareValue==null)
			{
				return false;
			}
			Collection<?> col;
			if(compareValue instanceof Collection)
			{
				col = Collection.class.cast(compareValue);
				return col.contains( value );
			}
			if(compareValue.getClass().isArray())
			{
				for(int i= 0;i<Array.getLength( compareValue);i++)
				{
					if(Objects.equals( value, Array.get( compareValue, i)))
					{
						return true;
					}
				}
				return false;
			}
			throw new IllegalArgumentException("No recognized collection");
		}
	};

	@Override
	public abstract boolean test(@Nullable final Object value, @Nullable final Object compareValue );
}
