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

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;

/**
 *
 * @author doe300
 * @param <R> the grouped key
 * @param <T> the type of results
 */
public class GroupResult<R, T extends ActiveRecord> implements QueryMethods<T>, AutoCloseable
{
	@Nonnull
	private final R key;
	@Nonnull
	private final Stream<T> baseStream;
	private final int size;
	private final Order order;

	/**
	 * @param key the value this group has in common
	 * @param baseStream the elements in this group
	 * @param size the size of the group
	 * @param order the ordering of the elements
	 */
	public GroupResult(@Nonnull final R key, @Nonnull final Stream<T> baseStream, final int size,
		@Nullable final Order order)
	{
		this.key = key;
		this.baseStream = order!=null? baseStream.sorted( order) : baseStream;
		this.size = size;
		this.order = order;
	}

	@Override
	public Stream<T> stream()
	{
		return baseStream;
	}

	/**
	 * @return the key, all elements in this group have in common
	 */
	public R getKey()
	{
		return key;
	}

	@Override
	public QueryResult<T> withScope( final Scope scope )
	{
		Stream<T> stream = baseStream;
		int limit = QueryMethods.SIZE_UNKNOWN;
		Order sorting = order;
		if(scope.getCondition()!=null)
		{
			stream = stream.filter( scope.getCondition()::test);
		}
		if(scope.getLimit() != Scope.NO_LIMIT)
		{
			stream = stream.limit( scope.getLimit());
			limit = Math.min( limit, scope.getLimit());
		}
		if(scope.getOrder() != null)
		{
			stream = stream.sorted( scope.getOrder());
			sorting = scope.getOrder();
		}
		return new QueryResult<T>(stream, limit, sorting );
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public Order getOrder()
	{
		return order;
	}

	@Override
	public void close() throws Exception
	{
		baseStream.close();
	}
}