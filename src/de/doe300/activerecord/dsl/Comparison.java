package de.doe300.activerecord.dsl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

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
		public boolean test( Object value, Object compareValue )
		{
			return Objects.equals( value, compareValue);
		}
	}, 
	/**
	 * Non-match of the values
	 * @see #IS
	 */
	IS_NOT {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return !Objects.equals( value, compareValue);
		}
	}, 
	/**
	 * Regex-match (SQL LIKE-Statement).
	 * This comparison uses <code>%</code> as wildcard.
	 */
	LIKE {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			if(value instanceof String)
			{
				return Pattern.matches(((String)compareValue).replaceAll( "%", ".*"),((String)value) );
			}
			return false;
		}
	}, 
	/**
	 * Whether the first argument is <code>null</code>.
	 */
	IS_NULL {

		@Override
		public boolean test( Object value, Object compareValue )
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
		public boolean test( Object value, Object compareValue )
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
		public boolean test( Object value, Object compareValue )
		{
			return ((Comparable)value).compareTo( compareValue) > 0;
		}
	},
	/**
	 * Returns, whether the first value is larger or equal to the second one
	 * @see Comparable
	 */
	LARGER_EQUALS {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return ((Comparable)value).compareTo( compareValue) >= 0;
		}
	},
	/**
	 * Returns whether the first value is smaller than the second
	 * @see Comparable
	 */
	SMALLER {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return ((Comparable)value).compareTo( compareValue) < 0;
		}
	},
	/**
	 * Returns whether the first value is smaller or equal to the second
	 * @see Comparable
	 */
	SMALLER_EQUALS {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return ((Comparable)value).compareTo( compareValue) <= 0;
		}
	},
	/**
	 * always returns true
	 */
	TRUE {

		@Override
		public boolean test( Object value, Object compareValue )
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
		public boolean test( Object value, Object compareValue )
		{
			if(Objects.equals( value, compareValue))
			{
				return true;
			}
			if(compareValue==null)
			{
				return false;
			}
			Collection<? extends Object> col;
			if(compareValue instanceof Collection)
			{
				col=( Collection<? extends Object> ) compareValue;
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
	public abstract boolean test( Object value, Object compareValue );
}
