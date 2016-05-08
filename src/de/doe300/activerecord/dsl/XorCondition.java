/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.doe300.activerecord.dsl;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Class for XOR'ing of conditions.
 * 
 * Since SQL doesn't support a XOR operator, the XOR is emulated by (A and not B) or (B and not A)
 * 
 * NOTE: Since the single conditions are evaluated several times, this could result in poor performance!
 * 
 * @author doe300
 * @since 0.8
 */
class XorCondition implements Condition
{
	private final Condition[] conditions;

	XorCondition(@Nonnull final Condition[] conditions )
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
	public boolean test( final ActiveRecord record )
	{
		boolean oneMatch = false;
		for(final Condition cond : conditions)
		{
			if(cond.test( record ))
			{
				if(oneMatch)
				{
					//second match -> fail
					return false;
				}
				oneMatch = true;
			}
		}
		return oneMatch;
	}

	@Override
	public boolean test( final Map<String, Object> map )
	{
		boolean oneMatch = false;
		for(final Condition cond : conditions)
		{
			if(cond.test( map ))
			{
				if(oneMatch)
				{
					//second match -> fail
					return false;
				}
				oneMatch = true;
			}
		}
		return oneMatch;
	}

	@Override
	public String toSQL( JDBCDriver driver, String tableName )
	{
		//see: https://stackoverflow.com/questions/5411619/t-sql-xor-operator/22867207#22867207
		final List<String> parts = new ArrayList<>(conditions.length);
		for(final Condition cond : conditions)
		{
			parts.add( Arrays.stream( conditions).map( (final Condition c) -> {
				return c.equals( cond) ? c.toSQL( driver, tableName ) : "NOT " + c.toSQL( driver, tableName );
			}).collect( Collectors.joining( " AND ")));
		}
		return "(" + parts.stream().map( (final String part) -> "(" + part + ")").collect( Collectors.joining( " OR ")) + ")";
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
