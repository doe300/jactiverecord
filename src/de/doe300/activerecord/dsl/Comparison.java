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
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 *
 * @author doe300
 */
public enum Comparison implements BiPredicate<Optional<Object>, Optional<Object>>
{
	/**
	 * Exact match of the two values
	 */
	IS {

		@Override
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null || compareValue == null)
			{
				throw new IllegalArgumentException("Can't compare IS with nonexistent values!");
			}
			if(value.orElse( null) instanceof Number && compareValue.orElse( null) instanceof Number )
			{
				return ((Number)value.get()).doubleValue() == ((Number)compareValue.get()).doubleValue();
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
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null || compareValue == null)
			{
				throw new IllegalArgumentException("Can't compare IS NOT with nonexistent values!");
			}
			if(value.orElse( null) instanceof Number && compareValue.orElse( null) instanceof Number )
			{
				return ((Number)value.get()).doubleValue() != ((Number)compareValue.get()).doubleValue();
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
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if (value == null || compareValue == null)
			{
				throw new IllegalArgumentException("Can't compare LIKE with nonexistent values!");
			}
			if(!value.isPresent() || !compareValue.isPresent())
			{
				return false;
			}
			if(value.get() instanceof String)
			{
				return Pattern.matches(((String)compareValue.get()).replaceAll( "%", ".*"),(String)value.get() );
			}
			return false;
		}
	},
	/**
	 * Whether the first argument is <code>null</code>.
	 */
	IS_NULL {

		@Override
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null)
			{
				throw new IllegalArgumentException("Can't compare nonexistent value to NULL!");
			}
			return !value.isPresent();
		}
	},
	/**
	 * Whether the first value is not <code>null</code>
	 * @see #IS_NULL
	 */
	IS_NOT_NULL {

		@Override
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null)
			{
				throw new IllegalArgumentException("Can't compare nonexistent value to NULL!");
			}
			return value.isPresent();
		}
	},
	/**
	 * Returns, whether the first value is larger than the second
	 * @see Comparable
	 */
	LARGER {

		@Override
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null || compareValue == null)
			{
				throw new IllegalArgumentException("Can't compare LARGER with nonexistent values!");
			}
			if(!value.isPresent() || !compareValue.isPresent())
			{
				return false;
			}
			return Comparable.class.cast(value.get()).compareTo(compareValue.get()) > 0;
		}
	},
	/**
	 * Returns, whether the first value is larger or equal to the second one
	 * @see Comparable
	 */
	LARGER_EQUALS {

		@Override
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null || compareValue == null)
			{
				throw new IllegalArgumentException("Can't compare LARGER with nonexistent values!");
			}
			if(!value.isPresent() || !compareValue.isPresent())
			{
				return false;
			}
			return Comparable.class.cast(value.get()).compareTo(compareValue.get()) >= 0;
		}
	},
	/**
	 * Returns whether the first value is smaller than the second
	 * @see Comparable
	 */
	SMALLER {

		@Override
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null || compareValue == null)
			{
				throw new IllegalArgumentException("Can't compare SMALLER with nonexistent values!");
			}
			if(!value.isPresent() || !compareValue.isPresent())
			{
				return false;
			}
			return Comparable.class.cast(value.get()).compareTo(compareValue.get()) < 0;
		}
	},
	/**
	 * Returns whether the first value is smaller or equal to the second
	 * @see Comparable
	 */
	SMALLER_EQUALS {

		@Override
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null || compareValue == null)
			{
				throw new IllegalArgumentException("Can't compare SMALLER with nonexistent values!");
			}
			if(!value.isPresent() || !compareValue.isPresent())
			{
				return false;
			}
			return Comparable.class.cast(value.get()).compareTo(compareValue.get()) <= 0;
		}
	},
	/**
	 * always returns true
	 */
	TRUE {

		@Override
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
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
		public boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue)
		{
			if(value == null || compareValue == null)
			{
				throw new IllegalArgumentException("Can't check nonexistent values with IN!");
			}
			if(Objects.equals( value.orElse( null), compareValue.orElse( null)))
			{
				return true;
			}
			if(!compareValue.isPresent())
			{
				return false;
			}
			Collection<?> col;
			if(compareValue.get() instanceof Collection)
			{
				col = Collection.class.cast(compareValue.get());
				return col.contains( value.orElse( null) );
			}
			if(compareValue.get().getClass().isArray())
			{
				for(int i= 0;i<Array.getLength( compareValue.get());i++)
				{
					if(isEquals( value.orElse( null),Array.get( compareValue.get(), i)))
					{
						return true;
					}
				}
				return false;
			}
			throw new IllegalArgumentException("No recognized collection");
		}
		
		private boolean isEquals(@Nullable final Object obj0, @Nullable final Object obj1)
		{
			if(Objects.equals( obj0, obj1))
			{
				return true;
			}
			if(obj0 instanceof Number && obj1 instanceof Number)
			{
				return ((Number)obj0).doubleValue() == ((Number)obj1).doubleValue();
			}
			return false;
		}
	};

	/**
	 * NOTE: A value of <code>null</code> represents a value which was not in the data-set.
	 * An {@link Optional} with a <code>null</code>-value represents a <code>null</code> stored in the data-set.
	 * @param value
	 * @param compareValue
	 * @return whether the comparison is true
	 */
	@Override
	public abstract boolean test(@Nullable final Optional<Object> value, @Nullable final Optional<Object> compareValue);
}
