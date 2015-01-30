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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The result of a query
 * @author doe300
 * @param <T>
 * @see QueryMethods
 */
public class QueryResult<T extends ActiveRecord> implements QueryMethods<T>
{
	private final Stream<T> baseStream;
	private final int size;
	private final Order order;

	/**
	 * 
	 * @param baseStream
	 * @param size
	 * @param order 
	 */
	public QueryResult( Stream<T> baseStream, int size, Order order )
	{
		this.baseStream = order!=null ? baseStream.sorted( order.toRecordComparator()) : baseStream;
		this.size = size;
		this.order = order;
	}
	
	@Override
	public Stream<T> stream()
	{
		return baseStream;
	}
	
	@Override
	public QueryResult<T> where( Condition condition )
	{
		return new QueryResult<T>(baseStream.filter( condition::test), SIZE_UNKNOWN, order );
	}

	@Override
	public QueryResult<T> limit( int number )
	{
		return new QueryResult<T>(baseStream.limit( number ), Math.min( size, number), order );
	}

	@Override
	public int size()
	{
		return size;
	}

	/**
	 * Groups the records in this result by the given column
	 * @param column
	 * @return the grouped result
	 */
	public Stream<GroupResult<Object, T>> groupBy( String column )
	{
		return baseStream.collect( Collectors.groupingBy( (T t)-> {
			return t.getBase().getStore().getValue( t.getBase(), t.getPrimaryKey(), column);
		})).entrySet().stream().map( (Map.Entry<Object, List<T>> e)-> new GroupResult<Object, T>(e.getKey(), e.getValue().stream(), e.getValue().size(),order));
	}
	
	/**
	 * Groups the record in this result by the return-value of the given function.
	 * All records for which the <code>method</code> returns the same value are put into the same group.
	 * @param <R>
	 * @param method
	 * @return the grouped results
	 */
	public <R> Stream<GroupResult<R, T>> groupBy( Function<T, R> method )
	{
		return baseStream.collect( Collectors.groupingBy( (T t)-> method.apply( t )))
				.entrySet().stream().map( (Map.Entry<R, List<T>> e)-> new GroupResult<R, T>(e.getKey(), e.getValue().stream(), e.getValue().size(),order));
	}

	@Override
	public Order getOrder()
	{
		return order;
	}
}
