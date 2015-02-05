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
package de.doe300.activerecord;

import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.scope.Scope;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A collection of finder-methods for {@link ActiveRecord records}
 * @author doe300
 * @param <T>
 */
public interface FinderMethods<T extends ActiveRecord>
{
	/**
	 * Finds all records matching the given condition
	 * @param condition
	 * @return all records matching the given condition or an empty Stream
	 */
	public default Stream<T> find(Condition condition)
	{
		return findWithScope( new Scope(condition, null, Scope.NO_LIMIT));
	}
	
	/**
	 * Finds the first record for the given condition
	 * @param condition
	 * @return the first record matching the condition or <code>null</code>
	 */
	public default T findFirst(Condition condition)
	{
		return findFirstWithScope( new Scope(condition, null, Scope.NO_LIMIT));
	}
	
	/**
	 * @param column
	 * @param value
	 * @return all records for the given value or an empty Stream
	 */
	public default Stream<T> findFor(String column, Object value)
	{
		return find( new SimpleCondition(column, value, Comparison.IS));
	}
	
	/**
	 * @param column
	 * @param value
	 * @return the first record for the given value or <code>null</code>
	 */
	public default T findFirstFor(String column, Object value)
	{
		return findFirst( new SimpleCondition(column, value, Comparison.IS));
	}
	
	/**
	 * @param data
	 * @return all records matching all the given values or an empty Stream
	 */
	public default Stream<T> findFor(Map<String,Object> data)
	{
		ArrayList<Condition> conds = new ArrayList<>(data.size());
		for(Map.Entry<String,Object> e :data.entrySet())
		{
			conds.add( new SimpleCondition(e.getKey(), e.getValue(), Comparison.IS));
		}
		if(conds.size()==1)
		{
			return find( conds.get( 0));
		}
		return find( new AndCondition( new Condition[conds.size()]) );
	}
	
	/**
	 * @param data
	 * @return the first record matching all given values or <code>null</code>
	 */
	public default T findFirstFor(Map<String,Object> data)
	{
		ArrayList<Condition> conds = new ArrayList<>(data.size());
		for(Map.Entry<String,Object> e :data.entrySet())
		{
			conds.add( new SimpleCondition(e.getKey(), e.getValue(), Comparison.IS));
		}
		if(conds.size()==1)
		{
			return findFirst( conds.get( 0));
		}
		return findFirst( new AndCondition(new Condition[conds.size()]) );
	}
	
	/**
	 * @return all records
	 */
	public default Stream<T> findAll()
	{
		return find( new SimpleCondition(null, null, Comparison.TRUE));
	}
	
	/**
	 * The most flexible finder-method
	 * @param scope
	 * @return the stream of results
	 */
	public Stream<T> findWithScope(final Scope scope);
	
	/**
	 * @param scope
	 * @return the first record according to the scope or <code>null</code>
	 */
	public T findFirstWithScope(final Scope scope);
}
