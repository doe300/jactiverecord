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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.scope.Scope;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Stores all records in maps
 * @author doe300
 */
public class MapRecordStore implements RecordStore
{
	//TODO needs testing
	private final Map<String, Map<Integer, Map<String,Object>>> data;

	public MapRecordStore()
	{
		this.data = new HashMap<>(10);
	}

	public MapRecordStore( Map<String, Map<Integer, Map<String, Object>>> data )
	{
		this.data = data;
	}

	@Override
	public Connection getConnection()
	{
		return null;
	}
	
	@Override
	public void setValue( RecordBase<?> base, int primaryKey, String name, Object value ) throws IllegalArgumentException
	{
		getRow( base.getTableName(), primaryKey ).put( name, value );
		if(base.isTimestamped())
		{
			getRow( base.getTableName(), primaryKey ).put( TimestampedRecord.COLUMN_UPDATED_AT, System.currentTimeMillis());
		}
	}

	@Override
	public Object getValue( RecordBase<?> base, int primaryKey, String name ) throws IllegalArgumentException
	{
		Object ret = getRow( base.getTableName(), primaryKey ).get( name );
		return ret;
	}

	@Override
	public Map<String, Object> getValues( RecordBase<?> base, int primaryKey, String[] columns ) throws IllegalArgumentException
	{
		return getRow( base.getTableName(), primaryKey );
	}
	
	protected Map<String,Object> getRow(String tableName, int primaryKey)
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
		Map<String,Object> row=new HashMap<>(10);
		table.put( primaryKey, row );
		return row;
	}
	
	@Override
	public boolean isSynchronized( RecordBase<?> base, int primaryKey )
	{
		return true;
	}

	@Override
	public boolean containsRecord( RecordBase<?> base, int primaryKey )
	{
		return data.containsKey( base.getTableName() ) && data.get( base.getTableName() ).containsKey( primaryKey);
	}

	@Override
	public void destroy( RecordBase<?> base, int primaryKey )
	{
		if(data.containsKey( base.getTableName() ))
		{
			data.get( base.getTableName() ).remove( primaryKey);
		}
	}

	@Override
	public int count(RecordBase<?> base, Condition condition )
	{
		if(data.containsKey( base.getTableName()))
		{
			return (int) data.get(base.getTableName()).values().stream().filter( (Map<String,Object>m )-> condition.test( m ) ).count();
		}
		return 0;
	}

	@Override
	public Map<String, Object> findFirstWithData( RecordBase<?> base, String[] columns, Scope scope )
	{
		if(data.containsKey( base.getTableName()))
		{
			return data.get(base.getTableName()).entrySet().stream().filter((Map.Entry<Integer,Map<String,Object>> e)->
			{
				return scope.getCondition().test( e.getValue());
			}).map( (Map.Entry<Integer,Map<String,Object>> e)->e.getValue()).sorted( base.getDefaultOrder()).findFirst().orElse( null );
		}
		return Collections.emptyMap();
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData( RecordBase<?> base, String[] columns, Scope scope )
	{
		if(data.containsKey( base.getTableName()))
		{
			return data.get(base.getTableName()).entrySet().stream().filter((Map.Entry<Integer,Map<String,Object>> e)->
			{
				return scope.getCondition().test( e.getValue());
			}).map( (Map.Entry<Integer,Map<String,Object>> e)->e.getValue()).sorted( base.getDefaultOrder());
		}
		return Stream.empty();
	}	

	@Override
	public boolean exists( String tableName )
	{
		return data.keySet().stream().anyMatch( (String name) -> name.equalsIgnoreCase( tableName ));
	}

	@Override
	public Set<String> getAllColumnNames( String tableName )
	{
		throw new UnsupportedOperationException( "Not supported by MapRecordStore." );
	}

	@Override
	public int insertNewRecord(RecordBase<?> base, Map<String,Object> columns )
	{
		if(!data.containsKey( base.getTableName()))
		{
			data.put( base.getTableName(), new TreeMap<>());
		}
		int key = data.get( base.getTableName() ).size();
		data.get( base.getTableName() ).put( key, new HashMap<>(columns != null ? columns.size() : 10));
		if(columns != null)
		{
			data.get( base.getTableName() ).get( key ).putAll( columns );
		}
		return key;
	}

	@Override
	public void close() throws Exception
	{
	}

	@Override
	public Stream<Object> getValues( String tableName, String column, String condColumn, Object condValue ) throws IllegalArgumentException
	{
		if(!data.containsKey( tableName))
		{
			throw new IllegalArgumentException("Table does not exists: "+tableName);
		}
		return data.get( tableName).values().stream().filter( (Map<String, Object> row) -> Objects.equals( row.get( 
				condColumn),condValue)).map( (Map<String, Object> row) -> row.get( column));
	}

	@Override
	public boolean isCached()
	{
		return false;
	}

	@Override
	public void setValues(RecordBase<?> base, int primaryKey, String[] names, Object[] values ) throws IllegalArgumentException
	{
		for(int i=0;i<names.length;i++)
		{
			setValue( base, primaryKey, names[i], values[i]);
		}
	}

	@Override
	public void setValues(RecordBase<?> base, int primaryKey, Map<String, Object> values ) throws IllegalArgumentException
	{
		getRow( base.getTableName(), primaryKey ).putAll(values );
		if(base.isTimestamped())
		{
			getRow( base.getTableName(), primaryKey ).put( TimestampedRecord.COLUMN_UPDATED_AT, System.currentTimeMillis());
		}
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
	public boolean addRow( String tableName, String[] columns, Object[] values ) throws IllegalArgumentException
	{
		if(!data.containsKey( tableName))
		{
			data.put( tableName, new TreeMap<>());
		}
		int key = data.get( tableName).size();
		Map<String, Object> row = new HashMap<>(columns != null ? columns.length : 10);
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
	public boolean removeRow( String tableName, Condition cond ) throws IllegalArgumentException
	{
		if(!data.containsKey( tableName))
		{
			throw new IllegalArgumentException("Table does not exist: "+tableName);
		}
		return data.get( tableName).values().removeIf( cond::test);
	}
}
