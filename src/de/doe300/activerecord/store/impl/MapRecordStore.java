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
package de.doe300.activerecord.store.impl;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.RecordStore;

/**
 * Stores all records in maps
 * 
 * @author doe300
 * @deprecated Implementation incomplete
 */
@Deprecated
public class MapRecordStore implements RecordStore
{
	private final Map<String, Map<Integer, Map<String,Object>>> data;

	public MapRecordStore()
	{
		this.data = new HashMap<>(10);
	}

	public MapRecordStore( final Map<String, Map<Integer, Map<String, Object>>> data )
	{
		this.data = data;
	}

	@Override
	public Connection getConnection()
	{
		return null;
	}

	@Override
	public void setValue( final RecordBase<?> base, final int primaryKey, final String name, final Object value ) throws IllegalArgumentException
	{
		getRow( base.getTableName(), primaryKey ).put( name, value );
		if(base.isTimestamped())
		{
			getRow( base.getTableName(), primaryKey ).put( TimestampedRecord.COLUMN_UPDATED_AT, System.currentTimeMillis());
		}
	}

	@Override
	public Object getValue( final RecordBase<?> base, final int primaryKey, final String name ) throws IllegalArgumentException
	{
		final Object ret = getRow( base.getTableName(), primaryKey ).get( name );
		return ret;
	}

	@Override
	public Map<String, Object> getValues( final RecordBase<?> base, final int primaryKey, final String[] columns ) throws IllegalArgumentException
	{
		return getRow( base.getTableName(), primaryKey );
	}

	protected Map<String,Object> getRow(final String tableName, final int primaryKey)
	{
		Map<Integer,Map<String,Object>> table = data.get( tableName);
		if(table == null)
		{
			table = new TreeMap<>();
			data.put( tableName, table );
		}
		if(table.containsKey( primaryKey))
		{
			return table.get( primaryKey );
		}
		final Map<String,Object> row=new HashMap<>(10);
		table.put( primaryKey, row );
		return row;
	}

	@Override
	public boolean isSynchronized( final RecordBase<?> base, final int primaryKey )
	{
		return true;
	}

	@Override
	public boolean containsRecord( final RecordBase<?> base, final int primaryKey )
	{
		return data.containsKey( base.getTableName() ) && data.get( base.getTableName() ).containsKey( primaryKey);
	}

	@Override
	public void destroy( final RecordBase<?> base, final int primaryKey )
	{
		if(data.containsKey( base.getTableName() ))
		{
			data.get( base.getTableName() ).remove( primaryKey);
		}
	}

	@Override
	public int count(final RecordBase<?> base, final Condition condition )
	{
		if(data.containsKey( base.getTableName()))
		{
			return (int) data.get(base.getTableName()).values().stream().filter( 
					(final Map<String,Object>m )-> condition == null ? true : condition.test( m ) 
			).count();
		}
		return 0;
	}

	@Override
	public Map<String, Object> findFirstWithData( final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		if(data.containsKey( base.getTableName()))
		{
			return data.get(base.getTableName()).entrySet().stream().filter((final Map.Entry<Integer,Map<String,Object>> e)->
			{
				return scope.getCondition() == null ? true : scope.getCondition().test( e.getValue());
			}).map( (final Map.Entry<Integer,Map<String,Object>> e)->e.getValue()).sorted( base.getDefaultOrder()).findFirst().orElse( null );
		}
		return Collections.emptyMap();
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData( final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		if(data.containsKey( base.getTableName()))
		{
			return data.get(base.getTableName()).entrySet().stream().filter((final Map.Entry<Integer,Map<String,Object>> e)->
			{
				return scope.getCondition() == null ? true : scope.getCondition().test( e.getValue());
			}).map( (final Map.Entry<Integer,Map<String,Object>> e)->e.getValue()).sorted( base.getDefaultOrder());
		}
		return Stream.empty();
	}

	@Override
	public boolean exists( final String tableName )
	{
		return data.keySet().stream().anyMatch( (final String name) -> name.equalsIgnoreCase( tableName ));
	}

	@Override
	public Set<String> getAllColumnNames( final String tableName )
	{
		throw new UnsupportedOperationException( "Not supported by MapRecordStore." );
	}

	@Override
	public int insertNewRecord(final RecordBase<?> base, final Map<String,Object> columns )
	{
		if(!data.containsKey( base.getTableName()))
		{
			data.put( base.getTableName(), new TreeMap<>());
		}
		//FIXME this is totally not safe!
		final int key = data.get( base.getTableName() ).size();
		data.get( base.getTableName() ).put( key, new HashMap<>(columns != null ? columns.size() : 10));
		if(columns != null)
		{
			data.get( base.getTableName() ).get( key ).putAll( columns );
		}
		data.get( base.getTableName() ).get( key ).put(base.getPrimaryColumn(), key);
		return key;
	}

	@Override
	public void close() throws Exception
	{
	}

	@Override
	public Stream<Object> getValues( final String tableName, final String column, final String condColumn, final Object condValue ) throws IllegalArgumentException
	{
		if(!data.containsKey( tableName))
		{
			throw new IllegalArgumentException("Table does not exists: "+tableName);
		}
		return data.get( tableName).values().stream().filter( (final Map<String, Object> row) -> Objects.equals( row.get(
			condColumn),condValue)).map( (final Map<String, Object> row) -> row.get( column));
	}

	@Override
	public boolean isCached()
	{
		return false;
	}

	@Override
	public void setValues(final RecordBase<?> base, final int primaryKey, final String[] names, final Object[] values ) throws IllegalArgumentException
	{
		for(int i=0;i<names.length;i++)
		{
			setValue( base, primaryKey, names[i], values[i]);
		}
	}

	@Override
	public void setValues(final RecordBase<?> base, final int primaryKey, final Map<String, Object> values ) throws IllegalArgumentException
	{
		getRow( base.getTableName(), primaryKey ).putAll(values );
		if(base.isTimestamped())
		{
			getRow( base.getTableName(), primaryKey ).put( TimestampedRecord.COLUMN_UPDATED_AT, System.currentTimeMillis());
		}
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
	}

	@Override
	public boolean addRow( final String tableName, final String[] columns, final Object[] values ) throws IllegalArgumentException
	{
		if(!data.containsKey( tableName))
		{
			data.put( tableName, new TreeMap<>());
		}
		final int key = data.get( tableName).size();
		final Map<String, Object> row = new HashMap<>(columns != null ? columns.length : 10);
		data.get( tableName ).put( key, row);
		if(columns != null)
		{
			for(int i = 0; i<columns.length;i++)
			{
				row.put( columns[i], values[i]);
			}
		}
		return true;
	}

	@Override
	public boolean removeRow( final String tableName, final Condition cond ) throws IllegalArgumentException
	{
		if(!data.containsKey( tableName))
		{
			throw new IllegalArgumentException("Table does not exist: "+tableName);
		}
		return data.get( tableName).values().removeIf( cond::test);
	}
}
