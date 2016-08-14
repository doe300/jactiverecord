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
package de.doe300.activerecord.store;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.diagnostics.Diagnostics;

/**
 * Base interface for all kinds of record storing data-base.
 * NOTE: all column-arrays are minimum data, the implementing store can choose to return more than the requested data
 * @author doe300
 */
public interface RecordStore extends AutoCloseable
{
	/**
	 * @return the driver to be used for the underlying data-store
	 */
	@Nonnull
	public DBDriver getDriver();
	
	/**
	 * @return the diagnostics attached to this store
	 * @since 0.8
	 */
	@Nonnull
	public Diagnostics<?> getDiagnostics();

	/**
	 * @param tableName
	 * @return whether the data-store exists
	 */
	public boolean exists(@Nonnull final String tableName);

	/**
	 * NOTE: to unify database-access, this method returns the keys in lower-case independent from the DBMS used.
	 * @param tableName
	 * @return all available column-names
	 * @throws java.lang.IllegalArgumentException if the table for the given name was not found
	 */
	@Nonnull
	public default Set<String> getAllColumnNames(@Nonnull final String tableName) throws NoSuchDataSetException
	{
		return getAllColumnTypes( tableName ).keySet();
	}

	/**
	 * NOTE: to unify database-access, this method returns the keys in lower-case independent from the DBMS used.
	 *
	 * @param tableName
	 * @return a map of all column-names and their java-types
	 * @throws IllegalArgumentException if the table for the given name was not found
	 * @since 0.5
	 */
	@Nonnull
	public Map<String, Class<?>> getAllColumnTypes(@Nonnull final String tableName) throws NoSuchDataSetException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param name the attribute-/column name to set
	 * @param value the new value to store
	 * @throws IllegalArgumentException if the data-set for the given {@link RecordBase} does not exist
	 */
	public void setValue(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final String name, @Nullable final Object value) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param names
	 * @param values
	 * @throws IllegalArgumentException if the data-set for the given {@link RecordBase} does not exist
	 */
	public void setValues(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final String[] names, @Nonnull final Object[] values) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param values
	 * @throws IllegalArgumentException if the data-set for the given {@link RecordBase} does not exist
	 */
	public void setValues(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final Map<String,Object> values) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param name
	 * @return the value or <code>null</code> if there is no entry matching the <code>primaryKey</code>
	 * @throws IllegalArgumentException if the data-set for the given {@link RecordBase} does not exist
	 */
	@Nullable
	public Object getValue(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final String name) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * NOTE: to retrieve all columns for a single row, use {@link #getAllValues(de.doe300.activerecord.RecordBase, int) }
	 * @param base
	 * @param primaryKey
	 * @param columns
	 * @return the values or an empty map, if the <code>primaryKey</code> was not found
	 * @throws IllegalArgumentException if the data-set for the given {@link RecordBase} does not exist
	 */
	@Nonnull
	public Map<String,Object> getValues(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final String[] columns) throws NoSuchDataSetException, NoSuchAttributeException;
	
	/**
	 * 
	 * @param base
	 * @param primaryKey
	 * @return all values for the given entry or an empty map, if the entry does not exists
	 * @throws NoSuchDataSetException  if the data-set for the given {@link RecordBase} does not exist
	 * @since 0.8
	 */
	@Nonnull
	public Map<String, Object> getAllValues(@Nonnull final RecordBase<?> base, @Nonnegative final int primaryKey) throws NoSuchDataSetException;

	/**
	 * This method is for usage only if the table has no mapped model, i.e. for association-tables.
	 * Requests made with this method are not required to be cached and should therefore be only used if no model applies to the requested table.
	 *
	 * NOTE: if the performance of this method is bad, consider adding an index to the <code>condColumn</code>
	 *
	 * @param tableName
	 * @param column the column to retrieve
	 * @param condColumn the column to match to the <code>condValue</code>
	 * @param condValue the value to search for
	 * @return the values for the given <code>column</code>
	 * @throws IllegalArgumentException if the <code>tableName</code> does not exist or the <code>condValue</code> does not match the type for <code>condColumn</code>.
	 *			Also throws if the column-names are not present in the given data-set
	 */
	@Nonnull
	public Stream<Object> getValues(@Nonnull final String tableName, @Nonnull final String column, @Nonnull final String condColumn, Object condValue) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * This method is for usage only if the table has no mapped model, i.e. for association-tables.
	 * Access made with this method is not required to be cached and should therefore be only used if no model applies to the requested table.
	 *
	 * @param tableName
	 * @param columns
	 * @param values
	 * @return whether the row was added
	 * @throws IllegalArgumentException if the <code>tableName</code> does not exist or the <code>condValue</code> does not match the type for <code>condColumn</code>.
	 *			Also throws if the column-names are not present in the given data-set
	 */
	@CheckReturnValue
	public boolean addRow(@Nonnull final String tableName, @Nonnull final String[] columns, @Nonnull final Object[] values) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * This method is for usage only if the table has no mapped model, i.e. for association-tables.
	 *
	 * @param tableName
	 * @param cond the condition to match
	 * @return whether the row was removed
	 * @throws IllegalArgumentException if the <code>tableName</code> does not exist
	 */
	@CheckReturnValue
	public boolean removeRow(@Nonnull final String tableName, @Nullable final Condition cond) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * This method is only necessary for caching RecordStores
	 * @param base
	 * @param primaryKey
	 * @return whether data was updated
	 */
	@CheckReturnValue
	public boolean save(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey);

	/**
	 * This method is only necessary for caching RecordStores
	 * @param base
	 * @return whether any data was updated
	 */
	@CheckReturnValue
	public boolean saveAll(@Nonnull final RecordBase<?> base);

	/**
	 * Clears all cached records for the given RecordBase.
	 * NOTE: this method does NOT write the cached values onto the underlying medium!
	 * @param base
	 * @param primaryKey
	 */
	public void clearCache(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey);

	/**
	 * @return whether this store maintains some kind of cache
	 */
	public boolean isCached();

	/**
	 * Updates the {@link TimestampedRecord#COLUMN_UPDATED_AT} on the given record
	 * @param base
	 * @param primaryKey
	 * @see TimestampedRecord
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code>
	 */
	public default void touch(@Nonnull final RecordBase<?> base, @Nonnegative final int primaryKey) throws NoSuchDataSetException
	{
		setValue( base, primaryKey, TimestampedRecord.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
	}

	/**
	 * If the RecordBase has <code>autoCreate</code> set to true and the table doesn't exists, it will be generated.
	 * if any <code>columnData</code> is set, is has to be written to the underlying resource!
	 * @param base
	 * @param columnData the data to insert, may be <code>null</code>
	 * @return the ID of the new record
	 * @see RecordBase#isAutoCreate()
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code> or any of the column is not present in the data-set
	 */
	public int insertNewRecord(@Nonnull final RecordBase<?> base, @Nullable final Map<String,Object> columnData) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * A record may be non-synchronized if the record-store uses caches or the record was not yet saved to the underlying resource
	 * @param base
	 * @param primaryKey
	 * @return whether the record is synchronized
	 */
	public boolean isSynchronized(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey);

	/**
	 * @param base
	 * @param primaryKey the primary key
	 * @return whether the Store contains a record with this <code>primaryKey</code>
	 */
	public boolean containsRecord(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey);

	/**
	 * Destroys the storage and cache of this record
	 * @param base
	 * @param primaryKey
	 */
	public void destroy(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey);

	////
	// find-Methods
	////

	/**
	 * @param base
	 * @param scope
	 * @return the primaryKey of the first match or <code>null</code>
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code>
	 */
	@Nullable
	public default Integer findFirst(@Nonnull final RecordBase<?> base, @Nonnull final Scope scope) throws NoSuchDataSetException, NoSuchAttributeException
	{
		final Map<String, Object> data = findFirstWithData(base, new String[]{base.getPrimaryColumn()}, scope);
		return ( Integer ) data.get( base.getPrimaryColumn());
	}

	/**
	 * @param base
	 * @param scope
	 * @return the primary keys of all matches or an empty Set
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code>
	 */
	@Nonnull
	public default Set<Integer> findAll(@Nonnull final RecordBase<?> base, @Nonnull final Scope scope) throws NoSuchDataSetException, NoSuchAttributeException
	{
		return streamAll(base, scope).collect( Collectors.toSet());
	}

	/**
	 * @param base
	 * @param scope
	 * @return all matching primary keys or an empty Stream
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code>
	 */
	@Nonnull
	public default Stream<Integer> streamAll(@Nonnull final RecordBase<?> base, @Nonnull final Scope scope) throws NoSuchDataSetException, NoSuchAttributeException
	{
		return streamAllWithData( base, new String[]{base.getPrimaryColumn()}, scope).map( (final Map<String,Object> map)->
		{
			return (Integer)map.get( base.getPrimaryColumn());
		});
	}

	/**
	 * @param base
	 * @param columns
	 * @param scope
	 * @return the data for the first match or an empty map
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code> or any of the <code>columns</code> does not exist in the data-set
	 */
	@Nonnull
	public Map<String, Object> findFirstWithData(@Nonnull final RecordBase<?> base, @Nonnull final String[] columns, @Nonnull final Scope scope) throws NoSuchDataSetException, NoSuchAttributeException;

	/**
	 * @param base
	 * @param columns
	 * @param scope
	 * @return the data for all matches or an empty map
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code> or any of the <code>columns</code> does not exist in the data-set
	 */
	@Nonnull
	public default Map<Integer, Map<String, Object>> findAllWithData(@Nonnull final RecordBase<?> base, @Nonnull final String[] columns, @Nonnull final Scope scope) throws NoSuchDataSetException, NoSuchAttributeException
	{
		return streamAllWithData( base, columns, scope ).collect( Collectors.toMap( (final Map<String,Object> map) ->
		{
			return (Integer)map.get( base.getPrimaryColumn());
		}, (final Map<String,Object> map)->map));
	}

	/**
	 * @param base
	 * @param columns
	 * @param scope
	 * @return the requested data for all matches or an empty Stream
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code> or any of the <code>columns</code> does not exist in the data-set
	 */
	@Nonnull
	public Stream<Map<String, Object>> streamAllWithData(@Nonnull final RecordBase<?> base, @Nonnull final String[] columns, @Nonnull final Scope scope) throws NoSuchDataSetException, NoSuchAttributeException;

	////
	// COUNT
	////

	/**
	 * @param base
	 * @param condition
	 * @return the number of records matching the given <code>condition</code>
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code>
	 */
	@Nonnegative
	public default int count(@Nonnull final RecordBase<?> base, @Nullable final Condition condition) throws NoSuchDataSetException, NoSuchAttributeException
	{
		return ( int ) streamAll( base, new Scope(condition, null, Scope.NO_LIMIT) ).count();
	}

	/**
	 * NOTE: If this RecordStore is based on a SQL-implementation, the aggregation should be 
	 * performed within the SQL-implementation for performance reasons
	 * 
	 * @param <R> the result-type
	 * @param base the {@link RecordBase} to aggregate over
	 * @param aggregateFunction the {@link AggregateFunction aggregation-function}
	 * @param condition the condition to filter the aggregated values
	 * @return the aggregated value
	 * @throws IllegalArgumentException if there is no data-set for the given <code>base</code>
	 */
	@Nullable
	public <R> R aggregate(@Nonnull final RecordBase<?> base, @Nonnull final AggregateFunction<?, ?, ?, R> aggregateFunction, @Nullable final Condition condition) throws NoSuchDataSetException, NoSuchAttributeException;
}
