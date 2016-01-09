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

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Grouped Condition, returning <code>true</code> if any of the children conditions returns <code>true</code>
 * @author doe300
 */
public class OrCondition implements Condition
{
	private final Condition[] conditions;

	private OrCondition( Condition[] conditions )
	{
		this.conditions = conditions;
	}
	
	/**
	 * Combines the <code>conds</code> and optimizes according to the following rules:
	 * <ul>
	 * <li>Removes all <code>null</code>-conditions</li>
	 * <li>Unrolls all children OR-conditions, because <code>a OR (b OR c)</code> is the same as <code>a OR b OR c</code></li>
	 * <li>If any condition produces the SQL-symbol <code>TRUE</code> this condition will always be <code>true</code> and therefore any other condition can be discarded</li>
	 * <li>Skips conditions which are already in the list</li>
	 * <li>Returns the single condition, if only one passes all other tests</li>
	 * </ul>
	 * @param conds
	 * @return the combined Condition
	 */
	@Nonnull
	public static Condition orConditions(@Nullable Condition... conds)
	{
		if(conds == null || conds.length == 0)
		{
			throw new IllegalArgumentException();
		}
		ArrayList<Condition> list = new ArrayList<>(conds.length);
		for(Condition cond: conds)
		{
			//clears nulls
			if(cond == null)
			{
				continue;
			}
			//unrolls or-conditions
			if(cond instanceof OrCondition)
			{
				list.addAll( Arrays.asList( ((OrCondition)cond).conditions));
				continue;
			}
			//check for non-false condition
			if(cond instanceof SimpleCondition && ((SimpleCondition)cond).getComparison() == Comparison.TRUE)
			{
				//this condition is non-false, no other conditions matter
				return cond;
			}
			//if condition is already in list, skip
			if(list.contains( cond ))
			{
				continue;
			}
			list.add( cond );
		}
		if(list.isEmpty())
		{
			throw new IllegalArgumentException("Can't OR null conditions");
		}
		if(list.size() == 1)
		{
			return list.get( 0);
		}
		return new OrCondition(list.toArray( new Condition[list.size()]));
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
	public String toSQL(final JDBCDriver driver, final String tableName)
	{
		return "("+ Arrays.stream( conditions ).map( (Condition c) -> c.toSQL(driver, tableName) ).collect( Collectors.joining( ") OR ("))+")";
	}

	@Override
	public Condition negate()
	{
		return InvertedCondition.invertCondition(this );
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
}
