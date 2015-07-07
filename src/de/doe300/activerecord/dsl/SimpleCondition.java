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

import de.doe300.activerecord.jdbc.VendorSpecific;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author doe300
 */
public class SimpleCondition implements Condition, SQLCommand
{
	private final String key;
	private final Object compValue;
	private final Comparison comp;

	/**
	 * @param key
	 * @param compValue
	 * @param comp 
	 */
	public SimpleCondition( String key, Object compValue, Comparison comp )
	{
		this.key = key;
		this.compValue = checkValue( compValue, comp );
		this.comp = checkComparison( this.compValue, comp);
	}
	
	private static Object checkValue(Object val, Comparison comp)
	{
		//check for IN
		if(comp == Comparison.IN)
		{
			if(val instanceof Collection)
			{
				return ((Collection)val).toArray();
			}
			if(val.getClass().isArray())
			{
				return val;
			}
			throw new IllegalArgumentException("Invalid list-type: "+val.getClass());
		}
		
		return val;
	}
	
	private static Comparison checkComparison(Object val, Comparison comp)
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
	
	/**
	 * @return the key (attribute or column) this condition checks for
	 */
	public String getKey()
	{
		return key;
	}
	
	@Override
	public Object[] getValues()
	{
		if(comp == Comparison.IN)
		{
			return ( Object[] ) compValue;
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
	public String toSQL(VendorSpecific vendorSpecifics)
	{
		switch(comp)
		{
			case IS:
				return key+" = ?";
			case IS_NOT:
				return key+" != ?";
			case LIKE:
				return key+" LIKE ?";
			case IS_NULL:
				return key+" IS NULL";
			case IS_NOT_NULL:
				return key+" IS NOT NULL";
			case LARGER:
				return key+" > ?";
			case LARGER_EQUALS:
				return key+" >= ?";
			case SMALLER:
				return key+" < ?";
			case SMALLER_EQUALS:
				return key+" <= ?";
			case IN:
				//see: https://stackoverflow.com/questions/178479/preparedstatement-in-clause-alternatives
				return key+" IN ("+Arrays.stream( (Object[])compValue).map( (Object o) -> "?").collect( Collectors.joining( ", "))+")";
			case TRUE:
			default:
				return vendorSpecifics.convertBooleanToDB( true );
		}
	}
	
	@Override
	public boolean hasWildcards()
	{
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
	public boolean test( Map<String, Object> t )
	{
		return comp.test( t.get( key), compValue);
	}
	
	@Override
	public boolean test( ActiveRecord t )
	{
		return comp.test(t.getBase().getStore().getValue( t.getBase(), t.getPrimaryKey(), key), compValue);
	}

	@Override
	public Condition negate()
	{
		return InvertedCondition.invertCondition(this );
	}
}
