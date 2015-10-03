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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.RecordStore;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 *
 * @author doe300
 * @since 0.3
 */
public class MemoryRecordStore implements RecordStore
{
	private final SortedMap<String, MemoryTable> tables;

	//TODO release 0.3 after all/most tests run with memory-store
	public MemoryRecordStore()
	{
		this.tables = new TreeMap<>();
	}

	@Override
	public Connection getConnection()
	{
		return null;
	}
	
	@Override
	public boolean exists( String tableName )
	{
		return tables.containsKey( tableName);
	}
	
	@Nonnull
	private MemoryTable assertTableExists(@Nonnull final String tableName) throws IllegalArgumentException
	{
		MemoryTable table = tables.get( tableName);
		if(table == null)
		{
			throw new IllegalArgumentException("No such table");
		}
		return table;
	}
	
	@Nonnull
	private MemoryTable createTableIfNotExists(@Nonnull final RecordBase<?> recordBase) throws IllegalArgumentException
	{
		MemoryTable table = tables.get( recordBase.getTableName());
		if(table == null)
		{
			if(recordBase.isAutoCreate())
			{
				if(new MemoryMigration(this, recordBase.getRecordType(), false).apply( null ))
				{
					return assertTableExists( recordBase.getTableName());
				}
				throw new IllegalArgumentException("Failed to create memory-table for: " + recordBase.getTableName());
			}
			throw new IllegalArgumentException("No such table");
		}
		return table;
	}

	@Override
	public Set<String> getAllColumnNames( String tableName ) throws UnsupportedOperationException
	{
		MemoryTable table = assertTableExists( tableName );
		return table.getColumnNames();
	}

	@Override
	public void setValue(
			RecordBase<?> base, int primaryKey, String name, Object value ) throws IllegalArgumentException
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		table.putValue( primaryKey, name, value );
	}

	@Override
	public void setValues(
			RecordBase<?> base, int primaryKey, String[] names, Object[] values ) throws IllegalArgumentException
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		table.putValues( primaryKey, names, values );
	}

	@Override
	public void setValues(
			RecordBase<?> base, int primaryKey, Map<String, Object> values ) throws IllegalArgumentException
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		table.putValues( primaryKey, values );
	}

	@Override
	public Object getValue(
			RecordBase<?> base, int primaryKey, String name ) throws IllegalArgumentException
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		return table.getValue( primaryKey, name );
	}

	@Override
	public Map<String, Object> getValues(
			RecordBase<?> base, int primaryKey, String[] columns ) throws IllegalArgumentException
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		return table.getValues( primaryKey, columns);
	}

	@Override
	public Stream<Object> getValues( String tableName, String column, String condColumn, Object condValue ) throws
			IllegalArgumentException
	{
		MemoryTable table = assertTableExists( tableName );
		return table.getValues( column, condColumn, condValue );
	}

	@Override
	public boolean addRow( String tableName, String[] columns, Object[] values ) throws IllegalArgumentException
	{
		MemoryTable table = assertTableExists( tableName );
		int index = table.insertRow();
		table.putValues( index, columns, values );
		return true;
	}

	@Override
	public boolean removeRow( String tableName, Condition cond ) throws IllegalArgumentException
	{
		MemoryTable table = assertTableExists( tableName );
		Map.Entry<Integer, MemoryRow> row = table.findFirstRow( new Scope(cond, null, Scope.NO_LIMIT) );
		if(row != null && row.getKey() >= 0)
		{
			table.removeRow( row.getKey() );
			return true;
		}
		return false;
	}

	@Override
	public boolean save(RecordBase<?> base, int primaryKey )
	{
		return false;
	}

	@Override
	public boolean saveAll(RecordBase<?> base )
	{
		return false;
	}

	@Override
	public void clearCache(RecordBase<?> base, int primaryKey )
	{
	}

	@Override
	public boolean isCached()
	{
		return false;
	}

	@Override
	public int insertNewRecord(RecordBase<?> base, Map<String, Object> columnData )
	{
		MemoryTable table = createTableIfNotExists(base );
		int rowIndex = table.insertRow();
		if(columnData != null)
		{
			for(Map.Entry<String, Object> e : columnData.entrySet())
			{
				table.putValue( rowIndex, e.getKey(), e.getValue());
			}
		}
		return rowIndex;
	}

	@Override
	public boolean isSynchronized(RecordBase<?> base, int primaryKey )
	{
		return true;
	}

	@Override
	public boolean containsRecord(RecordBase<?> base, int primaryKey )
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		return table.containsValue(primaryKey, base.getPrimaryColumn());
	}

	@Override
	public void destroy(RecordBase<?> base, int primaryKey )
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		table.removeRow( primaryKey );
	}

	@Override
	public Map<String, Object> findFirstWithData(RecordBase<?> base, String[] columns, Scope scope )
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		Map.Entry<Integer, MemoryRow> row = table.findFirstRow( scope);
		if(row != null)
		{
			return new HashMap<>(row.getValue().getRowMap());
		}
		return Collections.emptyMap();
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData(RecordBase<?> base, String[] columns, Scope scope )
	{
		MemoryTable table = assertTableExists( base.getTableName() );
		return table.findAllRows( scope ).map( (Map.Entry<Integer, MemoryRow> e) -> e.getValue().getRowMap());
	}

	@Override
	public void close() throws Exception
	{
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
}
