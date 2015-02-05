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

import de.doe300.activerecord.FinderMethods;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import java.util.stream.Stream;

/**
 * An implementation of QueryMethods will behave like {@link Stream}, more precise, the stored records are not guaranteed to be accessible more than one time
 * @author doe300
 * @param <T>
 */
public interface QueryMethods<T extends ActiveRecord> extends FinderMethods<T>
{
	/**
	 * Value for unknown size
	 */
	public static final int SIZE_UNKNOWN = -1;

	/**
	 * @return the stream of records
	 */
	public Stream<T> stream();
	
	/**
	 * @param condition
	 * @return a new QueryResult matching the given condition
	 */
	public QueryResult<T> where(Condition condition);
	
	/**
	 * @param number
	 * @return a new QueryResult with the limit of records applied
	 */
	public QueryResult<T> limit(int number);
	
	/**
	 * @param order
	 * @return a new QueryResult with ordered records
	 */
	public default QueryResult<T> order(Order order)
	{
		return new QueryResult<T>(stream(), size(), order);
	}

	/**
	 * @return the estimated number of records, or {@link #SIZE_UNKNOWN} if the number is not known
	 */
	public int size();
	
	/**
	 * @return the {@link Order} this query is sorted by
	 */
	public Order getOrder();
	
	@Override
	public default Stream<T> findWithScope( final Scope scope )
	{
		return null; //where( condition ).stream().sorted( getOrder().toRecordComparator());
	}

	@Override
	public default T findFirstWithScope( final Scope scope)
	{
		return null; //where( condition ).stream().sorted( getOrder().toRecordComparator()).findFirst().get();
	}
}
