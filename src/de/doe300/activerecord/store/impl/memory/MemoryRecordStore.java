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
package de.doe300.activerecord.store.impl.memory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.DBDriver;
import de.doe300.activerecord.store.RecordStore;
import javax.annotation.Nonnegative;

/**
 *
 * @author doe300
 * @since 0.3
 */
public class MemoryRecordStore implements RecordStore
{
	private final SortedMap<String, MemoryTable> tables;

	/**
	 *
	 */
	public MemoryRecordStore()
	{
		this.tables = new TreeMap<>();
	}

	@Override
	public DBDriver getDriver()
	{
		return MemoryDBDriver.INSTANCE;
	}

	@Override
	public boolean exists( final String tableName )
	{
		return tables.containsKey( tableName);
	}

	@Nonnull
	private MemoryTable assertTableExists(@Nonnull final String tableName) throws IllegalArgumentException
	{
		final MemoryTable table = tables.get( tableName);
		if(table == null)
		{
			throw new IllegalArgumentException("No such table");
		}
		return table;
	}
	
	private static void assertColumnsExist(@Nonnull final MemoryTable table, @Nonnull final String... columns) throws IllegalArgumentException
	{
		for(final String column : columns)
		{
			if(!table.getColumnNames().contains( column))
			{
				throw new IllegalArgumentException("Invalid column: " +column);
			}
		}
	}
	
	private static void assertColumnsExist(@Nonnull final MemoryTable table, @Nonnull final Iterable<String> columns) throws IllegalArgumentException
	{
		for(final String column : columns)
		{
			if(!table.getColumnNames().contains( column))
			{
				throw new IllegalArgumentException("Invalid column: " +column);
			}
		}
	}

	@Nonnull
	private MemoryTable createTableIfNotExists(@Nonnull final RecordBase<?> recordBase) throws IllegalArgumentException
	{
		final MemoryTable table = tables.get( recordBase.getTableName());
		if(table == null)
		{
			if(recordBase.isAutoCreate())
			{
				if(new MemoryMigration(this, recordBase.getRecordType()).apply())
				{
					return assertTableExists( recordBase.getTableName());
				}
				throw new IllegalArgumentException("Failed to create memory-table for: " + recordBase.getTableName());
			}
			throw new IllegalArgumentException("No such table: " + recordBase.getTableName());
		}
		return table;
	}

	@Override
	public Set<String> getAllColumnNames( final String tableName ) throws UnsupportedOperationException
	{
		final MemoryTable table = assertTableExists( tableName );
		return table.getColumnNames();
	}

	@Override
	public Map<String, Class<?>> getAllColumnTypes( String tableName ) throws IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( tableName );
		return table.getColumnTypes();
	}

	@Override
	public void setValue(
		final RecordBase<?> base, final int primaryKey, final String name, final Object value ) throws IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		assertColumnsExist( table, name);
		table.putValue( primaryKey, name, value );
		updateTimestamps( base, primaryKey, false);
	}

	@Override
	public void setValues(
		final RecordBase<?> base, final int primaryKey, final String[] names, final Object[] values ) throws IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		assertColumnsExist( table, names );
		table.putValues( primaryKey, names, values );
		updateTimestamps( base, primaryKey, false);
	}

	@Override
	public void setValues(
		final RecordBase<?> base, final int primaryKey, final Map<String, Object> values ) throws IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		assertColumnsExist( table, values.keySet());
		table.putValues( primaryKey, values );
		updateTimestamps( base, primaryKey, false);
	}

	@Override
	public Object getValue(
		final RecordBase<?> base, final int primaryKey, final String name ) throws IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		assertColumnsExist( table, name);
		return table.getValue( primaryKey, name );
	}

	@Override
	public Map<String, Object> getValues(
		final RecordBase<?> base, final int primaryKey, final String[] columns ) throws IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		assertColumnsExist( table, columns);
		return table.getValues( primaryKey, columns);
	}

	@Override
	public Stream<Object> getValues( final String tableName, final String column, final String condColumn, final Object condValue ) throws
	IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( tableName );
		assertColumnsExist( table, column, condColumn);
		return table.getValues( column, condColumn, condValue );
	}

	@Override
	public boolean addRow( final String tableName, final String[] columns, final Object[] values ) throws IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( tableName );
		assertColumnsExist( table, columns);
		final int index = table.insertRow();
		table.putValues( index, columns, values );
		return true;
	}

	@Override
	public boolean removeRow( final String tableName, final Condition cond ) throws IllegalArgumentException
	{
		final MemoryTable table = assertTableExists( tableName );
		final Map.Entry<Integer, MemoryRow> row = table.findFirstRow( new Scope(cond, null, Scope.NO_LIMIT) );
		if(row != null && row.getKey() >= 0)
		{
			table.removeRow( row.getKey() );
			return true;
		}
		return false;
	}

	@Override
	public boolean save(final RecordBase<?> base, final int primaryKey )
	{
		return false;
	}

	@Override
	public boolean saveAll(final RecordBase<?> base )
	{
		return false;
	}

	@Override
	public void clearCache(final RecordBase<?> base, final int primaryKey )
	{
		// no cache
	}

	@Override
	public boolean isCached()
	{
		return false;
	}

	@Override
	public int insertNewRecord(final RecordBase<?> base, final Map<String, Object> columnData )
	{
		final MemoryTable table = createTableIfNotExists(base );
		if(columnData != null)
		{
			assertColumnsExist( table, columnData.keySet());
		}
		final int rowIndex = table.insertRow();
		if(columnData != null)
		{
			for(final Map.Entry<String, Object> e : columnData.entrySet())
			{
				table.putValue( rowIndex, e.getKey(), e.getValue());
			}
		}
		updateTimestamps( base, rowIndex, true);
		return rowIndex;
	}

	@Override
	public boolean isSynchronized(final RecordBase<?> base, final int primaryKey )
	{
		return true;
	}

	@Override
	public boolean containsRecord(final RecordBase<?> base, final int primaryKey )
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		return table.containsValue(primaryKey, base.getPrimaryColumn());
	}

	@Override
	public void destroy(final RecordBase<?> base, final int primaryKey )
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		table.removeRow( primaryKey );
	}

	@Override
	public Map<String, Object> findFirstWithData(final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		assertColumnsExist( table, columns);
		final Map.Entry<Integer, MemoryRow> row = table.findFirstRow( scope);
		if(row != null)
		{
			return new HashMap<>(row.getValue().getRowMap());
		}
		return Collections.emptyMap();
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData(final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		assertColumnsExist( table, columns);
		return table.findAllRows( scope ).map( (final Map.Entry<Integer, MemoryRow> e) -> e.getValue().getRowMap());
	}

	@Override
	public <R> R aggregate(RecordBase<?> base, AggregateFunction<?, ?, ?, R> aggregateFunction, Condition condition )
	{
		final MemoryTable table = assertTableExists( base.getTableName() );
		return aggregateFunction.aggregate( table.findAllRows( new Scope(condition, null, Scope.NO_LIMIT)).
				map( Map.Entry::getValue ).map(MemoryRow::getRowMap));
	}

	@Override
	public void close() throws Exception
	{
		// nothing to close
	}

	////
	// Package-private table-manipulation methods
	////

	/**
	 * Adds a new table to the set of memory-tables
	 *
	 * @param tableName the name of the new table
	 * @param columns the columns for the new table
	 * @param primaryColumn the name of the column holding the primary-keys
	 * @return whether the new table was added
	 */
	boolean addTable(@Nonnull final String tableName, @Nonnull final MemoryColumn[] columns, @Nonnull final String primaryColumn)
	{
		if(tables.containsKey( tableName))
		{
			return false;
		}
		tables.put( tableName, new MemoryTable(primaryColumn, columns));
		return true;
	}

	boolean removeTable(@Nonnull final String tableName)
	{
		return tables.remove( tableName) != null;
	}
	
	private void updateTimestamps(@Nonnull final RecordBase<?> base, @Nonnegative final int primaryKey, final boolean setCreationDate)
	{
		if(!base.isTimestamped())
		{
			return;
		}
		MemoryTable table = assertTableExists( base.getTableName());
		if(setCreationDate)
		{
			table.putValue( primaryKey, TimestampedRecord.COLUMN_CREATED_AT, new java.sql.Timestamp(System.currentTimeMillis()));
		}
		table.putValue( primaryKey, TimestampedRecord.COLUMN_UPDATED_AT, new java.sql.Timestamp(System.currentTimeMillis()));
	}
	
	@Override
	public void touch(@Nonnull final RecordBase<?> base, @Nonnegative final int primaryKey)
	{
		updateTimestamps(base, primaryKey, false);
	}
}
