package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.dsl.Condition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Stores all records in maps
 * @author doe300
 */
public class MapRecordStore implements RecordStore
{
	protected final Map<RecordBase<?>, Map<Integer, Map<String,Object>>> data;

	public MapRecordStore()
	{
		this.data = new HashMap<>(10);
	}

	public MapRecordStore( Map<RecordBase<?>, Map<Integer, Map<String, Object>>> data )
	{
		this.data = data;
	}
	
	@Override
	public void setValue( RecordBase<?> base, int primaryKey, String name, Object value ) throws IllegalArgumentException
	{
		getRow( base, primaryKey ).put( name, value );
		if(base.isTimestamped())
		{
			getRow( base, primaryKey ).put( COLUMN_UPDATED_AT, System.currentTimeMillis());
		}
	}

	@Override
	public Object getValue( RecordBase<?> base, int primaryKey, String name ) throws IllegalArgumentException
	{
		Object ret = getRow( base, primaryKey ).get( name );
		return ret;
	}

	@Override
	public Map<String, Object> getValues( RecordBase<?> base, int primaryKey, String[] columns ) throws IllegalArgumentException
	{
		return getRow( base, primaryKey );
	}
	
	protected Map<String,Object> getRow(RecordBase<?> base, int primaryKey)
	{
		Map<Integer,Map<String,Object>> table = data.get( base );
		if(table == null)
		{
			table = new TreeMap<>();
			data.put( base, table );
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
	public boolean containsRecord( RecordBase<?> base, Integer primaryKey )
	{
		return data.containsKey( base ) && data.get( base ).containsKey( primaryKey);
	}

	@Override
	public void destroy( RecordBase<?> base, int primaryKey )
	{
		if(data.containsKey( base ))
		{
			data.get( base ).remove( primaryKey);
		}
	}

	@Override
	public int count(RecordBase<?> base, Condition condition )
	{
		if(data.containsKey( base))
		{
			return (int) data.get(base).values().stream().filter( (Map<String,Object>m )-> condition.test( m ) ).count();
		}
		return 0;
	}

	@Override
	public Map<String, Object> findFirstWithData( RecordBase<?> base, String[] columns, Condition condition )
	{
		if(data.containsKey( base))
		{
			return data.get(base).entrySet().stream().filter((Map.Entry<Integer,Map<String,Object>> e)->
			{
				return condition.test( e.getValue());
			}).map( (Map.Entry<Integer,Map<String,Object>> e)->e.getValue()).sorted( base.getDefaultOrder()).findFirst().orElse( null );
		}
		return Collections.emptyMap();
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData( RecordBase<?> base, String[] columns, Condition condition )
	{
		if(data.containsKey( base))
		{
			return data.get(base).entrySet().stream().filter((Map.Entry<Integer,Map<String,Object>> e)->
			{
				return condition.test( e.getValue());
			}).map( (Map.Entry<Integer,Map<String,Object>> e)->e.getValue()).sorted( base.getDefaultOrder());
		}
		return Stream.empty();
	}	

	@Override
	public boolean exists( String tableName )
	{
		return data.keySet().stream().anyMatch( (RecordBase<?> b) -> b.getTableName().equalsIgnoreCase( tableName ));
	}

	@Override
	public String[] getAllColumnNames( String tableName )
	{
		throw new UnsupportedOperationException( "Not supported by MapRecordStore." );
	}

	@Override
	public int insertNewRecord(RecordBase<?> base )
	{
		if(!data.containsKey( base))
		{
			data.put( base, new TreeMap<>());
		}
		int key = data.get( base ).size();
		data.get( base ).put( key, new HashMap<>(10));
		return key;
	}

	@Override
	public void close() throws Exception
	{
	}

	@Override
	public Stream<Object> getValues( String tableName, String column, String condColumn, Object condValue ) throws IllegalArgumentException
	{
		//TODO how to implement??
		return Stream.empty();
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
		getRow( base, primaryKey ).putAll(values );
		if(base.isTimestamped())
		{
			getRow( base, primaryKey ).put( COLUMN_UPDATED_AT, System.currentTimeMillis());
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
}