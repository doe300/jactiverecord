package de.doe300.activerecord.dsl;

import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.dsl.Condition;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author doe300
 * @param <T>
 */
public class QueryResult<T extends ActiveRecord> implements QueryMethods<T>
{
	private final Stream<T> baseStream;
	private final int size;
	private final Order order;

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

	public Stream<GroupResult<Object, T>> groupBy( String column )
	{
		return baseStream.collect( Collectors.groupingBy( (T t)-> {
			return t.getBase().getStore().getValue( t.getBase(), t.getPrimaryKey(), column);
		})).entrySet().stream().map( (Map.Entry<Object, List<T>> e)-> new GroupResult<Object, T>(e.getKey(), e.getValue().stream(), e.getValue().size(),order));
	}
	
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
