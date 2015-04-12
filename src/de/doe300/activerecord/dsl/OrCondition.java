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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Grouped Condition, returning <code>true</code> if any of the children conditions returns <code>true</code>
 * @author doe300
 */
public class OrCondition implements Condition
{
	private final Condition[] conditions;

	/**
	 * @param conditions 
	 */
	public OrCondition( Condition... conditions )
	{
		this.conditions = Objects.requireNonNull( conditions);
	}

	@Override
	public boolean hasWildcards()
	{
		for(Condition con : conditions)
		{
			if(con.hasWildcards())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Object[] getValues()
	{
		List<Object> values = new ArrayList<>(conditions.length);
		for(Condition cond:conditions)
		{
			values.addAll( Arrays.asList( cond.getValues()));
		}
		return values.toArray();
	}

	@Override
	public boolean test( ActiveRecord record )
	{
		for(Condition cond:conditions)
		{
			if(cond.test( record ))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean test( Map<String, Object> t )
	{
		for(Condition cond:conditions)
		{
			if(cond.test( t ))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String toSQL(VendorSpecific vendorSpecifics)
	{
		return "("+ Arrays.stream( conditions ).map( (Condition c) -> c.toSQL(vendorSpecifics) ).collect( Collectors.joining( ") OR ("))+")";
	}

	@Override
	public Condition negate()
	{
		return new InvertedCondition(this );
	}
}
