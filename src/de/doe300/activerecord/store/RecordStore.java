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

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.scope.Scope;
import javax.annotation.Nonnegative;

/**
 * Base interface for all kinds of record storing data-base.
 * NOTE: all column-arrays are minimum data, the implementing store can choose to return more than the requested data
 * @author doe300
 */
public interface RecordStore extends AutoCloseable
{
	/**
	 * @return the underlying Connection or <code>null</code>
	 */
	@Nullable
	public Connection getConnection();

	/**
	 * @param tableName
	 * @return whether the data-store exists
	 */
	public boolean exists(@Nonnull final String tableName);

	/**
	 * NOTE: to unify database-access, this method returns the keys in lower-case independent from the DBMS used.
	 * @param tableName
	 * @return all available column-names
	 * @throws java.lang.UnsupportedOperationException if the store can't retrieve the column-names
	 */
	@Nonnull
	public Set<String> getAllColumnNames(@Nonnull final String tableName) throws UnsupportedOperationException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param name
	 * @param value
	 * @throws IllegalArgumentException
	 */
	public void setValue(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final String name, @Nullable final Object value) throws IllegalArgumentException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param names
	 * @param values
	 * @throws IllegalArgumentException
	 */
	public void setValues(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final String[] names, @Nonnull final Object[] values) throws IllegalArgumentException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param values
	 * @throws IllegalArgumentException
	 */
	public void setValues(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final Map<String,Object> values) throws IllegalArgumentException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param name
	 * @return the value or <code>null</code>
	 * @throws IllegalArgumentException
	 */
	@Nullable
	public Object getValue(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final String name) throws IllegalArgumentException;

	/**
	 * @param base
	 * @param primaryKey
	 * @param columns
	 * @return the values or an empty map, if the <code>primaryKey</code> was not found
	 * @throws IllegalArgumentException
	 */
	@Nonnull
	public Map<String,Object> getValues(@Nonnull final RecordBase<?> base, @Nonnegative int primaryKey, @Nonnull final String[] columns) throws IllegalArgumentException;

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
	 * @return the values for the given <code>column</code> or <code>null</code>
	 * @throws IllegalArgumentException if the <code>tableName</code> does not exist or the <code>condValue</code> does not match the type for <code>condColumn</code>
	 */
	@Nonnull
	public Stream<Object> getValues(@Nonnull final String tableName, @Nonnull final String column, @Nonnull final String condColumn, Object condValue) throws IllegalArgumentException;

	/**
	 * This method is for usage only if the table has no mapped model, i.e. for association-tables.
	 * Access made with this method is not required to be cached and should therefore be only used if no model applies to the requested table.
	 *
	 * @param tableName
	 * @param columns
	 * @param values
	 * @return whether the row was added
	 * @throws IllegalArgumentException if the <code>tableName</code> does not exist or the <code>condValue</code> does not match the type for <code>condColumn</code>
	 */
	@CheckReturnValue
	public boolean addRow(@Nonnull final String tableName, @Nonnull final String[] columns, @Nonnull final Object[] values) throws IllegalArgumentException;

	/**
	 * This method is for usage only if the table has no mapped model, i.e. for association-tables.
	 *
	 * @param tableName
	 * @param cond the condition to match
	 * @return whether the row was removed
	 * @throws IllegalArgumentException if the <code>tableName</code> does not exist or the <code>condValue</code> does not match the type for <code>condColumn</code>
	 */
	@CheckReturnValue
	public boolean removeRow(@Nonnull final String tableName, @Nullable final Condition cond) throws IllegalArgumentException;

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
	 */
	public default void touch(@Nonnull final RecordBase<?> base, @Nonnegative final int primaryKey)
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
	 */
	public int insertNewRecord(@Nonnull final RecordBase<?> base, @Nullable final Map<String,Object> columnData);

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
	 */
	@Nullable
	public default Integer findFirst(@Nonnull final RecordBase<?> base, @Nonnull final Scope scope)
	{
		final Map<String, Object> data = findFirstWithData(base, new String[]{base.getPrimaryColumn()}, scope);
		return ( Integer ) data.get( base.getPrimaryColumn());
	}

	/**
	 * @param base
	 * @param scope
	 * @return the primary keys of all matches or an empty Set
	 */
	@Nonnull
	public default Set<Integer> findAll(@Nonnull final RecordBase<?> base, @Nonnull final Scope scope)
	{
		return streamAll(base, scope).collect( Collectors.toSet());
	}

	/**
	 * @param base
	 * @param scope
	 * @return all matching primary keys or an empty Stream
	 */
	@Nonnull
	public default Stream<Integer> streamAll(@Nonnull final RecordBase<?> base, @Nonnull final Scope scope)
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
	 */
	@Nonnull
	public Map<String, Object> findFirstWithData(@Nonnull final RecordBase<?> base, @Nonnull final String[] columns, @Nonnull final Scope scope);

	/**
	 * @param base
	 * @param columns
	 * @param scope
	 * @return the data for all matches or an empty map
	 */
	@Nonnull
	public default Map<Integer, Map<String, Object>> findAllWithData(@Nonnull final RecordBase<?> base, @Nonnull final String[] columns, @Nonnull final Scope scope)
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
	 */
	@Nonnull
	public Stream<Map<String, Object>> streamAllWithData(@Nonnull final RecordBase<?> base, @Nonnull final String[] columns, @Nonnull final Scope scope);

	////
	// COUNT
	////

	/**
	 * @param base
	 * @param condition
	 * @return the number of records matching the given <code>condition</code>
	 */
	@Nonnegative
	public default int count(@Nonnull final RecordBase<?> base, @Nullable final Condition condition)
	{
		return ( int ) streamAll( base, new Scope(condition, null, Scope.NO_LIMIT) ).count();
	}
}
