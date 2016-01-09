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

import de.doe300.activerecord.AggregateMethods;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.FinderMethods;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import javax.annotation.Nonnegative;

/**
 * An implementation of QueryMethods will behave like {@link Stream}, more precise, the stored records are not guaranteed to be accessible more than one time
 * @author doe300
 * @param <T>
 */
public interface QueryMethods<T extends ActiveRecord> extends FinderMethods<T>, AggregateMethods<T>
{
	/**
	 * Value for unknown getEstimatedSize
	 */
	public static final int SIZE_UNKNOWN = -1;

	/**
	 * @return the stream of records
	 */
	@Nonnull
	public Stream<T> stream();
	
	/**
	 * @param condition
	 * @return a new QueryResult matching the given condition
	 */
	@Nonnull
	public default QueryResult<T> where(@Nullable final Condition condition)
	{
		return withScope(new Scope(condition, null, getEstimatedSize()));
	}
	
	/**
	 * @param number
	 * @return a new QueryResult with the limit of records applied
	 */
	@Nonnull
	public default QueryResult<T> limit(@Nonnegative final int number)
	{
		return withScope( new Scope(null, null, number));
	}
	
	/**
	 * @param order
	 * @return a new QueryResult with ordered records
	 */
	@Nonnull
	public default QueryResult<T> order(@Nullable final Order order)
	{
		return withScope(new Scope(null, order, getEstimatedSize()));
	}

	/**
	 * @return the estimated number of records, or {@link #SIZE_UNKNOWN} if the number is not known
	 */
	public int getEstimatedSize();
	
	/**
	 * @return the {@link Order} this query is sorted by
	 */
	public Order getOrder();
	
	/**
	 * @param scope
	 * @return  a new QueryResult matching the given scope
	 */
	@Nonnull
	public QueryResult<T> withScope(@Nonnull final Scope scope);
	
	@Override
	@Nonnull
	public default Stream<T> findWithScope(@Nonnull final Scope scope )
	{
		return withScope( scope ).stream();
	}

	@Override
	@Nullable
	public default T findFirstWithScope(@Nonnull final Scope scope)
	{
		return withScope( scope ).stream().findFirst().get();
	}

	@Override
	@Nonnegative
	public default int count( @Nullable final Condition condition )
	{
		return ( int ) findWithScope(new Scope(condition, null, Scope.NO_LIMIT)).count();
	}

	@Override
	public default <C, R> R aggregate( AggregateFunction<T, C, ?, R> aggregateFunction, Condition condition )
	{
		return stream().filter( (T record) -> condition == null || condition.test( record)).collect( aggregateFunction );
	}
}