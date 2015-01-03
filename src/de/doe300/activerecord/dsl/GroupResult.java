package de.doe300.activerecord.dsl;

import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.dsl.Condition;
import java.util.stream.Stream;

/**
 *
 * @author doe300
 * @param <R> the grouped key
 * @param <T> the type of results
 */
public class GroupResult<R, T extends ActiveRecord> implements QueryMethods<T>
{
	private final R key;
	private final Stream<T> baseStream;
	private final int size;
	private final Order order;

	public GroupResult( R key, Stream<T> baseStream, int size, Order order )
	{
		this.key = key;
		this.baseStream = order!=null? baseStream.sorted( order.toRecordComparator()) : baseStream;
		this.size = size;
		this.order = order;
	}
	
	@Override
	public Stream<T> stream()
	{
		return baseStream;
	}
	
	public R getKey()
	{
		return key;
	}

	@Override
	public QueryResult<T> where( Condition condition )
	{
		return new QueryResult<T>(baseStream.filter( condition::test), SIZE_UNKNOWN,order);
	}

	@Override
	public QueryResult<T> limit( int number )
	{
		return new QueryResult<T>(baseStream.limit( number), Math.min(number,size),order);
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
}
