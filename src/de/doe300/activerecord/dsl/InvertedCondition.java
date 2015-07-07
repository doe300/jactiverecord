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
import java.util.Map;

/**
 * Inverts the given condition (returns <code>false</code> if the condition returns <code>true</code>).
 * Equivalent to the SQL keyword NOT
 * 
 * @author doe300
 */
public class InvertedCondition implements Condition
{
	private final Condition invertedCondition;

	private InvertedCondition( Condition invertedCondition )
	{
		this.invertedCondition = invertedCondition;
	}
	
	/**
	 * Inverts the <code>cond</code>.
	 * 
	 * This method optimizes by unwrapping a twice inverted condition, because <code>NOT(NOT(a))</code> is the same as </code>a</code>
	 * 
	 * @param cond
	 * @return the inverted Condition
	 */
	public static Condition invertCondition(Condition cond)
	{
		if(cond == null)
		{
			throw new IllegalArgumentException();
		}
		if(cond instanceof InvertedCondition)
		{
			return cond.negate();
		}
		return new InvertedCondition(cond );
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
	public boolean test( ActiveRecord record )
	{
		return !invertedCondition.test( record );
	}

	@Override
	public boolean test( Map<String, Object> map )
	{
		return !invertedCondition.test( map );
	}

	@Override
	public String toSQL( VendorSpecific vendorSpecifics )
	{
		return "NOT("+invertedCondition.toSQL( vendorSpecifics )+")";
	}

	@Override
	public Condition negate()
	{
		return invertedCondition;
	}
}
