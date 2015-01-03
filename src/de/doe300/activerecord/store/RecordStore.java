package de.doe300.activerecord.store;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.dsl.Condition;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.management.openmbean.KeyAlreadyExistsException;

/**
 * NOTE: all column-arrays are minimum data, the implementing store can choose to return more than the requested data
 * @author doe300
 */
public interface RecordStore extends AutoCloseable
{
	/**
	 * The default column for the primary key
	 */
	public static final String DEFAULT_COLUMN_ID = "id";
	
	/**
	 * The creation timestamp
	 */
	public static final String COLUMN_CREATED_AT="created_at";
	
	/**
	 * The timestamp of the last update
	 */
	public static final String COLUMN_UPDATED_AT="updated_at";
	
	/**
	 * @param tableName 
	 * @return whether the data-store exists
	 */
	public boolean exists(String tableName);
	
	/**
	 * NOTE: to unify database-access, this method returns the keys in lower-case independent from the DBMS used.
	 * @param tableName
	 * @return all available column-names
	 * @throws java.lang.UnsupportedOperationException if the store can't retrieve the column-names
	 */
	public String[] getAllColumnNames(String tableName) throws UnsupportedOperationException;
	
	public void setValue(RecordBase<?> base, int primaryKey, String name, Object value) throws IllegalArgumentException;
	
	public default void setValues(RecordBase<?> base, int primaryKey, String[] names, Object[] values) throws IllegalArgumentException
	{
		for(int i=0;i<names.length;i++)
		{
			setValue(base, primaryKey, names[i], values[i]);
		}
	}
	
	public default void setValues(RecordBase<?> base, int primaryKey, Map<String,Object> values) throws IllegalArgumentException
	{
		for(Map.Entry<String,Object> e:values.entrySet())
		{
			setValue(base, primaryKey, e.getKey(), e.getValue());
		}
	}
	
	/**
	 * @param base
	 * @param primaryKey
	 * @param name
	 * @return the value or <code>null</code>
	 * @throws IllegalArgumentException 
	 */
	public Object getValue(RecordBase<?> base, int primaryKey, String name) throws IllegalArgumentException;
	
	/**
	 * @param base
	 * @param primaryKey
	 * @param columns
	 * @return the values or an empty map, if the <code>primaryKey</code> was not found
	 * @throws IllegalArgumentException 
	 */
	public default Map<String,Object> getValues(RecordBase<?> base, int primaryKey, String[] columns) throws IllegalArgumentException
	{
		Map<String,Object> map = new HashMap<>(columns.length);
		for(String column:columns)
		{
			map.put( column, getValue(base, primaryKey, column));
		}
		return map;
	}
	
	/**
	 * This method is for usage only if the table has no mapped model, i.e. for association-tables.
	 * Requests made with this method are not required to be cached and should therefore be only used if no model applies to the requested table.
	 * @param tableName
	 * @param column the column to retrieve
	 * @param condColumn the column to match to the <code>condValue</code>
	 * @param condValue the value to search for
	 * @return the values for the given <code>column</code> or <code>null</code>
	 * @throws IllegalArgumentException if the <code>tableName</code> does not exist or the <code>condValue</code> does not match the type for <code>condColumn</code>
	 */
	public Stream<Object> getValues(String tableName, String column, String condColumn, Object condValue) throws IllegalArgumentException;
	
	/**
	 * This method is for usage only if the table has no mapped model, i.e. for association-tables.
	 * Access made with this method is not required to be cached and should therefore be only used if no model applies to the requested table.
	 * 
	 * @param tableName
	 * @param column the column to set
	 * @param value the value to set
	 * @param condColumn the column to match to the <code>condValue</code>
	 * @param condValue the value to search for
	 * @throws IllegalArgumentException if the <code>tableName</code> does not exist or the <code>condValue</code> does not match the type for <code>condColumn</code>
	 */
//	public void setValue(String tableName, String column, Object value, String condColumn, Object condValue) throws IllegalArgumentException;
	
	/**
	 * This method is only necessary for caching RecordStores
	 * @param base
	 * @param primaryKey  
	 * @return whether data was updated
	 */
	public default boolean save(RecordBase<?> base, int primaryKey)
	{
		return false;
	}
	
	/**
	 * This method is only necessary for caching RecordStores
	 * @param base
	 * @return whether any data was updated
	 */
	public default boolean saveAll(RecordBase<?> base)
	{
		return false;
	}
	
	public default void clearCache(RecordBase<?> base, int primaryKey)
	{
		
	}
	
	/**
	 * @return whether this store maintains some kind of cache
	 */
	public boolean isCached();
	
	/**
	 * Updates the {@link #COLUMN_UPDATED_AT} on the given record
	 * @param base
	 * @param primaryKey 
	 * @see TimestampedRecord
	 */
	public default void touch(RecordBase<?> base, int primaryKey)
	{
		setValue( base, primaryKey, COLUMN_UPDATED_AT, System.currentTimeMillis());
	}
	
	/**
	 * @param base
	 * @return the ID of the new record
	 */
	public int insertNewRecord(RecordBase<?> base);
	
	/**
	 * Called upon the creation of a new proxy instance
	 * @param proxy 
	 * @param primaryKey the primary key, <code>null</code> if no key is set
	 * @param createNew whether to create a new Record
	 * @return whether the initialization was successful
	 * @throws KeyAlreadyExistsException if the primary key is already is use
	 */
	@Deprecated
	public default boolean initProxy(ActiveRecord proxy, Integer primaryKey, boolean createNew) throws KeyAlreadyExistsException
	{
		return true;
	}
	
	/**
	 * A record may be non-synchronized if the record-store uses caches or the record was not yet saved to the underlying resource
	 * @param base
	 * @param primaryKey 
	 * @return whether the record is synchronized
	 */
	public boolean isSynchronized(RecordBase<?> base, int primaryKey);
	
	/**
	 * @param base
	 * @param primaryKey the primary key
	 * @return whether the Store contains a record with this <code>primaryKey</code>
	 */
	public boolean containsRecord(RecordBase<?> base, Integer primaryKey);	
	
	/**
	 * Destroys the storage and cache of this record
	 * @param base
	 * @param primaryKey 
	 */
	public void destroy(RecordBase<?> base, int primaryKey);
	
	////
	// find-Methods
	////
	
	/**
	 * @param base
	 * @param condition
	 * @return the primaryKey of the first match or <code>null</code>
	 */
	public default Integer findFirst(RecordBase<?> base, Condition condition)
	{
		Map<String, Object> data = findFirstWithData(base, new String[]{base.getPrimaryColumn()}, condition );
		if(data!=null)
		{
			return ( Integer ) data.get( base.getPrimaryColumn());
		}
		return null;
	}
	
	/**
	 * @param base
	 * @param condition
	 * @return the primary keys of all matches or an empty Set
	 */
	public default Set<Integer> findAll(RecordBase<?> base, Condition condition)
	{
		return streamAll(base, condition ).collect( Collectors.toSet());
	}
	
	/**
	 * @param base
	 * @param condition
	 * @return all matching primary keys or an empty Stream
	 */
	public default Stream<Integer> streamAll(RecordBase<?> base, Condition condition)
	{
		return streamAllWithData( base, new String[]{base.getPrimaryColumn()}, condition).map( (Map<String,Object> map)->
		{
			return (Integer)map.get( base.getPrimaryColumn());
		});
	}
	
	/**
	 * @param base
	 * @param columns
	 * @param condition
	 * @return the data for the first match or an empty map
	 */
	public Map<String, Object> findFirstWithData(RecordBase<?> base, String[] columns, Condition condition);
	
	/**
	 * @param base
	 * @param columns
	 * @param condition
	 * @return the data for all matches or an empty map
	 */
	public default Map<Integer, Map<String, Object>> findAllWithData(RecordBase<?> base, String[] columns, Condition condition)
	{
		return streamAllWithData( base, columns, condition ).collect( Collectors.toMap( (Map<String,Object> map) -> 
		{
			return (Integer)map.get( base.getPrimaryColumn());
		}, (Map<String,Object> map)->map));
	}
	
	/**
	 * @param base
	 * @param columns
	 * @param condition
	 * @return the requested data for all matches or an empty Stream
	 */
	public Stream<Map<String, Object>> streamAllWithData(RecordBase<?> base, String[] columns, Condition condition);
	
	////
	// COUNT
	////
	
	public default int count(RecordBase<?> base, Condition condition)
	{
		return ( int ) streamAll( base, condition ).count();
	}
}
