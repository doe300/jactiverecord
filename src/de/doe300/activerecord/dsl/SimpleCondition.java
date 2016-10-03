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

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Syntax;
import javax.annotation.concurrent.Immutable;

/**
 *
 * @author doe300
 */
public class SimpleCondition implements Condition
{
	//TODO add support for value left and column-name right -> could be used for JOINs
	@Nullable
	private final Side left;
	@Nullable
	private final Side right;
	@Nonnull
	private final Comparison comp;

	/**
	 * @param leftSide
	 * @param rightSide
	 * @param comparison
	 */
	SimpleCondition(@Nonnull final Object leftSide, @Nullable final Object rightSide, @Nonnull final Comparison comparison)
	{
		if(!(leftSide instanceof String) && !(leftSide instanceof SQLFunction))
		{
			throw new UnsupportedOperationException("Value as left is is currently not supported!");
		}
		left = comparison.hasLeft ? checkValue( leftSide, comparison, false) : null;
		right = comparison.hasRight ? checkValue( rightSide, comparison, true ) : null;
		this.comp = checkComparison( rightSide, comparison);
	}
	
	private static Side checkValue(@Nullable final Object val, @Nonnull final Comparison comp, final boolean mayBeValue)
	{
		//XXX check for type compatibility for SQL-functions and rows too ?!?
		final boolean isValue = mayBeValue && (val == null || !SQLFunction.class.isAssignableFrom( val.getClass()));
		
		//check for IN (only check for values)
		if(isValue && comp == Comparison.IN)
		{
			if(val == null)
			{
				throw new IllegalArgumentException("Can't match item to NULL list!");
			}
			if(val instanceof Collection)
			{
				return new Side(Collection.class.cast(val).toArray(), isValue);
			}
			if(val.getClass().isArray())
			{
				return new Side(val, isValue );
			}
			throw new IllegalArgumentException("Invalid list-type: "+val.getClass());
		}
		//check for LARGER/SMALLER (only check for values)
		if(comp == Comparison.LARGER || comp == Comparison.LARGER_EQUALS || comp == Comparison.SMALLER || comp == Comparison.SMALLER_EQUALS)
		{
			if(!(val instanceof Comparable))
			{
				throw new IllegalArgumentException("Type must be comparable: " + val.getClass());
			}
		}
		return new Side(val, isValue );
	}

	@Nonnull
	private static Comparison checkComparison(@Nullable final Object val, @Nonnull final Comparison comp)
	{
		//check for null
		if(val == null)
		{
			if(comp == Comparison.IS)
			{
				return Comparison.IS_NULL;
			}
			if(comp == Comparison.IS_NOT)
			{
				return Comparison.IS_NOT_NULL;
			}
		}
		return comp;
	}

	@Override
	public Object[] getValues()
	{
		if(comp == Comparison.IN)
		{
			return ( Object[] ) right.data;
		}
		if(right == null || right.data instanceof SQLFunction)
		{
			//SQLFunctions are handled via #toSQL()
			return null;
		}
		return new Object[]{right.data};
	}

	/**
	 * @return the comparison-method
	 */
	public Comparison getComparison()
	{
		return comp;
	}

	@Override
	public String toSQL(@Nonnull final JDBCDriver driver, final String tableName)
	{
		final String leftSide = comp.hasLeft ? left.toSQL( driver, tableName ) : "";
		final String rightSide = comp.hasRight ? right.toSQL( driver, tableName ) : "";
		return leftSide + comp.toSQL( driver ) + rightSide;
	}

	@Override
	public boolean hasWildcards()
	{
		return (comp.hasLeft && left.isValue) || (comp.hasRight && right.isValue);
	}

	@Override
	public boolean test( final Map<String, Object> t )
	{
		final Optional<Object> leftValue = comp.hasLeft ? left.getValue( t) : null;
		final Optional<Object> rightValue = comp.hasRight ? right.getValue( t) : null;
		return comp.test( leftValue, rightValue );
	}

	@Override
	public boolean test( final ActiveRecord t )
	{
		final Optional<Object> leftValue = comp.hasLeft ? left.getValue( t) : null;
		final Optional<Object> rightValue = comp.hasRight ? right.getValue( t) : null;
		return comp.test( leftValue, rightValue );
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(obj == null || !(obj instanceof Condition))
		{
			return false;
		}
		return equals( (Condition)obj);
	}
	
	@Override
	public int hashCode()
	{
		return toSQL( JDBCDriver.DEFAULT, null ).hashCode();
	}
	
	@Immutable
	private static class Side
	{
		@Nullable
		final Object data;
		final boolean isValue;

		Side(@Nullable final Object data, boolean isValue )
		{
			this.data = data;
			this.isValue = isValue;
		}
		
		@Nonnull
		@Syntax("SQL")
		public String toSQL(@Nonnull final JDBCDriver driver, @Nullable final String tableName)
		{
			if(isValue)
			{
				if(data != null && data.getClass().isArray())
				{
					if(((Object[])data).length == 0)
					{
						//Matching item to empty list is not supported by SQL
						return "(NULL)";
					}
					//see: https://stackoverflow.com/questions/178479/preparedstatement-in-clause-alternatives
					return "("+Arrays.stream( (Object[])data).map( (final Object o) -> "?").collect( Collectors.joining( ", "))+")";
				}
				return "?";
			}
			if(data instanceof SQLFunction)
			{
				return ((SQLFunction)data).toSQL( driver, tableName );
			}
			return (String)(tableName != null ? tableName + "." + data : data);
		}
		
		@Nonnull
		public Optional<Object> getValue(@Nonnull final ActiveRecord record)
		{
			if(isValue)
			{
				return Optional.ofNullable( data);
			}
			if(data instanceof SQLFunction)
			{
				return Optional.ofNullable( ((SQLFunction)data).apply( record));
			}
			return Optional.ofNullable( record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), (String)data));
		}
		
		@Nullable
		public Optional<Object> getValue(@Nonnull final Map<String, Object> row)
		{
			if(isValue)
			{
				return Optional.ofNullable( data);
			}
			if(data instanceof SQLFunction)
			{
				return Optional.ofNullable( ((SQLFunction)data).apply( row));
			}
			if(!row.containsKey( (String)data))
			{
				return null;
			}
			return Optional.ofNullable( row.get( (String)data));
		}
	}
}
