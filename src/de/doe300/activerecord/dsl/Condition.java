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

import de.doe300.activerecord.record.ActiveRecord;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 * @author doe300
 */
public interface Condition extends Predicate<Map<String, Object>>, SQLCommand
{
	/**
	 * The wildcards (<code>?</code>) are replaced by the conditions {@link #getValues() values}.
	 * @return whether this conditions uses wildcards in its SQL statement
	 */
	public boolean hasWildcards();
	
	/**
	 * @return the values to match
	 */
	public Object[] getValues();
	
	/**
	 * This method acts as a {@link Predicate Predicate&lt;ActiveRecord&gt;}
	 * @param record
	 * @return whether the <code>record</code> matches this condition
	 */
	public boolean test(ActiveRecord record);
}
