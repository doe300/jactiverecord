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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;

/**
 *
 * @author doe300
 */
public class SimpleCondition implements Condition
{
	@Nonnull
	private final Object key;
	@Nullable
	private final Object compValue;
	@Nonnull
	private final Comparison comp;

	/**
	 * @param key
	 * @param compValue
	 * @param comp
	 */
	public SimpleCondition(@Nonnull final String key, @Nullable final Object compValue, @Nonnull final Comparison comp)
	{
		this.key = key;
		this.compValue = SimpleCondition.checkValue( compValue, comp );
		this.comp = SimpleCondition.checkComparison( this.compValue, comp);
	}
	
	/**
	 * @param key
	 * @param compValue
	 * @param comp
	 * @since 0.6
	 */
	public SimpleCondition(@Nonnull final SQLFunction<?,?> key, @Nullable final Object compValue, @Nonnull final Comparison comp)
	{
		this.key = key;
		this.compValue = SimpleCondition.checkValue( compValue, comp );
		this.comp = SimpleCondition.checkComparison( this.compValue, comp);
	}

	private static Object checkValue(final Object val, final Comparison comp)
	{
		//check for IN
		if(comp == Comparison.IN)
		{
			if(val instanceof Collection)
			{
				return Collection.class.cast(val).toArray();
			}
			if(val.getClass().isArray())
			{
				return val;
			}
			throw new IllegalArgumentException("Invalid list-type: "+val.getClass());
		}

		return val;
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
			return ( Object[] ) compValue;
		}
		if(compValue instanceof SQLFunction)
		{
			//SQLFunctions are handled via #toSQL()
			return null;
		}
		return new Object[]{compValue};
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
		final String columnID;
		if(key instanceof SQLFunction)
		{
			columnID = ((SQLFunction<?,?>)key).toSQL( driver, tableName);
		}
		else
		{
			columnID = tableName != null ? tableName + "." + key : (String)key;
		}
		final String condValue = compValue instanceof SQLFunction ? ((SQLFunction<?,?>)compValue).toSQL( driver, tableName ) : "?";
		switch(comp)
		{
			case IS:
				return columnID+" = " + condValue;
			case IS_NOT:
				return columnID+" != " + condValue;
			case LIKE:
				return columnID+" LIKE " + condValue;
			case IS_NULL:
				return columnID+" IS NULL";
			case IS_NOT_NULL:
				return columnID+" IS NOT NULL";
			case LARGER:
				return columnID+" > " + condValue;
			case LARGER_EQUALS:
				return columnID+" >= " + condValue;
			case SMALLER:
				return columnID+" < " + condValue;
			case SMALLER_EQUALS:
				return columnID+" <= " + condValue;
			case IN:
				//see: https://stackoverflow.com/questions/178479/preparedstatement-in-clause-alternatives
				return columnID+" IN ("+Arrays.stream( (Object[])compValue).map( (final Object o) -> "?").collect( Collectors.joining( ", "))+")";
			case TRUE:
			default:
				return driver.convertBooleanToDB( true );
		}
	}

	@Override
	public boolean hasWildcards()
	{
		if(compValue instanceof SQLFunction)
		{
			return false;
		}
		switch(comp)
		{
			case IS:
			case IS_NOT:
			case LIKE:
			case LARGER:
			case LARGER_EQUALS:
			case SMALLER:
			case SMALLER_EQUALS:
			case IN:
				return true;
			case IS_NULL:
			case IS_NOT_NULL:
			case TRUE:
			default:
				return false;
		}
	}

	@Override
	public boolean test( final Map<String, Object> t )
	{
		final Object compValue0 = key instanceof SQLFunction ? ((SQLFunction<?,?>)key).apply( t) : t.get( (String)key );
		if (compValue instanceof SQLFunction)
		{
			return comp.test(compValue0, SQLFunction.class.cast( compValue).apply(t));
		}
		return comp.test( compValue0, compValue);
	}

	@Override
	public boolean test( final ActiveRecord t )
	{
		final Object compValue0 = key instanceof SQLFunction ? ((SQLFunction)key).apply( t) : t.getBase().getStore().getValue( t.getBase(), t.getPrimaryKey(), (String)key);
		if (compValue instanceof SQLFunction)
		{
			return comp.test(compValue0, SQLFunction.class.cast( compValue).apply(t));
		}
		return comp.test(compValue0, compValue);
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
}
