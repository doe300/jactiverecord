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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;

/**
 * Class for SQL cond1 AND cond2 AND cond3 ... conditions
 * @author doe300
 */
class AndCondition implements Condition
{
	private final Condition[] conditions;

	AndCondition(final Condition[] conditions )
	{
		this.conditions = conditions;
	}
	
	@Override
	public boolean hasWildcards()
	{
		for(final Condition con : conditions)
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
		final List<Object> values = new ArrayList<>(conditions.length);
		for(final Condition cond:conditions)
		{
			if(cond.hasWildcards())
			{
				values.addAll( Arrays.asList( cond.getValues()));
			}
		}
		return values.toArray();
	}

	@Override
	public boolean test(final ActiveRecord record )
	{
		for(final Condition cond:conditions)
		{
			if(!cond.test( record ))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean test( final Map<String, Object> t )
	{
		for(final Condition cond:conditions)
		{
			if(!cond.test( t ))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String toSQL(@Nonnull final JDBCDriver driver, final String tableName)
	{
		return "("+ Arrays.stream( conditions ).map( (final Condition c) -> c.toSQL(driver, tableName) ).collect( Collectors.joining( ") AND ("))+")";
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
	
	@Nonnull
	Condition[] getConditions()
	{
		return conditions;
	}
}
