package de.doe300.activerecord.dsl;

import de.doe300.activerecord.FinderMethods;
import de.doe300.activerecord.record.ActiveRecord;
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
	public default Stream<T> find( Condition condition )
	{
		return where( condition ).stream().sorted( getOrder().toRecordComparator());
	}

	@Override
	public default T findFirst( Condition condition )
	{
		return where( condition ).stream().sorted( getOrder().toRecordComparator()).findFirst().get();
	}
}
