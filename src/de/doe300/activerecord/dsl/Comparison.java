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
	IS {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return Objects.equals( value, compareValue);
		}
	}, 
	IS_NOT {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return !Objects.equals( value, compareValue);
		}
	}, 
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
	IS_NULL {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return value == null;
		}
	}, 
	IS_NOT_NULL {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return value != null;
		}
	},
	LARGER {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return ((Comparable)value).compareTo( compareValue) > 0;
		}
	},
	LARGER_EQUALS {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return ((Comparable)value).compareTo( compareValue) >= 0;
		}
	},
	SMALLER {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return ((Comparable)value).compareTo( compareValue) < 0;
		}
	},
	SMALLER_EQUALS {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return ((Comparable)value).compareTo( compareValue) <= 0;
		}
	},
	TRUE {

		@Override
		public boolean test( Object value, Object compareValue )
		{
			return true;
		}
	},
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
