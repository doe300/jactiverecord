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

import java.util.Map;

import javax.annotation.Nonnull;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;

/**
 * Inverts the given condition (returns <code>false</code> if the condition returns <code>true</code>).
 * Equivalent to the SQL keyword NOT
 *
 * @author doe300
 */
class InvertedCondition implements Condition
{
	@Nonnull
	private final Condition invertedCondition;

	InvertedCondition(@Nonnull final Condition invertedCondition)
	{
		this.invertedCondition = invertedCondition;
	}

	@Override
	public boolean hasWildcards()
	{
		return invertedCondition.hasWildcards();
	}

	@Override
	public Object[] getValues()
	{
		return invertedCondition.getValues();
	}

	@Override
	public boolean test( final ActiveRecord record )
	{
		return !invertedCondition.test( record );
	}

	@Override
	public boolean test( final Map<String, Object> map )
	{
		return !invertedCondition.test( map );
	}

	@Override
	public String toSQL( final JDBCDriver driver, final String tableName )
	{
		return "NOT("+invertedCondition.toSQL( driver, tableName )+")";
	}

	@Override
	public Condition negate()
	{
		return invertedCondition;
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
