package de.doe300.activerecord;

import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.SimpleCondition;
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
	public Stream<T> find(Condition condition);
	
	/**
	 * Finds the first record for the given condition
	 * @param condition
	 * @return the first record matching the condition or <code>null</code>
	 */
	public T findFirst(Condition condition);
	
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
	
	//TODO public Stream<ActiveRecord> findBySQL(String sql)
}
