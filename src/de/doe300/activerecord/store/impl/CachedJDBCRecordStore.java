package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.store.RowCache;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.dsl.Condition;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Uses write-back cache
 * @author doe300
 */
public class CachedJDBCRecordStore extends SimpleJDBCRecordStore implements RecordStore
{
	private final Map<RecordBase<?>, Map<Integer, RowCache>> cache;
	private final Map<String,String[]> columnsCache;

	/**
	 * @param con 
	 */
	public CachedJDBCRecordStore( Connection con)
	{
		super( con );
		this.cache=new HashMap<>(10);
		this.columnsCache = new TreeMap<>();
	}
	
	private RowCache getCache(RecordBase<?> base, Integer primaryKey)
	{
		Map<Integer, RowCache> tableCache = cache.get( base);
		if(tableCache == null)
		{
			tableCache = new TreeMap<>();
			cache.put( base, tableCache );
		}
		RowCache c = tableCache.get( primaryKey);
		if(c == null)
		{
			c = RowCache.emptyCache( base.getTableName(), base.getPrimaryColumn() );
			c.setData( base.getPrimaryColumn(), primaryKey, false);
			tableCache.put( primaryKey, c );
		}
		return c;
	}
	
	private boolean hasCache(RecordBase<?> base, int primaryKey)
	{
		return cache.containsKey( base) && cache.get( base).containsKey( primaryKey);
	}

	@Override
	public boolean containsRecord( RecordBase<?> base, Integer primaryKey )
	{
		if(hasCache( base, primaryKey ))
		{
			return true;
		}
		return super.containsRecord( base, primaryKey );
	}
	
	@Override
	public void setValue( RecordBase<?> base, int primaryKey, String name, Object value ) throws IllegalArgumentException
	{
		getCache(base, primaryKey ).setData( name, value, base.isTimestamped());
	}

	@Override
	public void setValues( RecordBase<?> base, int primaryKey, String[] names, Object[] values ) throws IllegalArgumentException
	{
		getCache(base, primaryKey ).setData( names, values, base.isTimestamped() );
	}

	@Override
	public void setValues( RecordBase<?> base, int primaryKey, Map<String, Object> values ) throws IllegalArgumentException
	{
		getCache( base,primaryKey).update( values, base.isTimestamped() );
	}
	
	@Override
	public Object getValue( RecordBase<?> base, int primaryKey, String name ) throws IllegalArgumentException
	{
		RowCache c = getCache(base, primaryKey );
		if(c.hasData( name ))
		{
			return c.getData( name );
		}
		Object value=getDBValue( base, primaryKey, c, name);
		c.setData( name, value, false );
		return value;
	}

	@Override
	public Map<String, Object> getValues( RecordBase<?> base, int primaryKey, String[] columns ) throws IllegalArgumentException
	{
		Map<String,Object> result = new HashMap<>(columns.length);
		RowCache c = getCache( base,primaryKey );
		//write changes in cache to DB so #getDBValue does not override cached changes with old data
		save( base, primaryKey );
		for(String col:columns)
		{
			if(c.hasData( col ))
			{
				result.put( col, c.getData( col));
			}
			//this else clause is only called the first time a column is not in the cache
			else
			{
				//TODO better handling for non existing row
				//currently getDBValue is called for every column
				Object val = getDBValue( base, primaryKey, c, col );
				result.put( col, val );
			}
		}
		return result;
	}

	/* Loads the whole row into cache at once */
	private Object getDBValue(RecordBase<?> base, int primaryKey, RowCache cache, String name) throws IllegalArgumentException
	{
		try(Statement stmt = con.createStatement())
		{
			//XXX only load default columns + requested column??
			ResultSet res = stmt.executeQuery( "SELECT * FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey);
			if(res.next())
			{
				cache.update( res, false);
				return cache.getData( name );
			}
			return null;
		}
		catch ( SQLException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public boolean save(RecordBase<?> base, int primaryKey)
	{
		//is write-back cache, so synchronization needs to be called explicitely
		if(hasCache( base, primaryKey ))
		{
			RowCache c = getCache(base, primaryKey );
			if(!c.isSynchronized())
			{
				//TODO improve: set only changed values
				Map<String,Object> values = c.toMap();
				super.setValues( base, primaryKey, values );
				c.setSynchronized();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean saveAll(RecordBase<?> base)
	{
		if(!cache.containsKey( base) || cache.get( base).isEmpty())
		{
			return false;
		}
		boolean changed = false;
		for(Map.Entry<Integer,RowCache> c : cache.get( base).entrySet())
		{
			if(!c.getValue().isSynchronized())
			{
				//XXX see #save
				Map<String,Object> values = c.getValue().toMap();
				super.setValues( base, c.getKey(), values);
				c.getValue().setSynchronized();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean isSynchronized( RecordBase<?> base, int primaryKey )
	{
		return !hasCache( base, primaryKey ) || getCache( base, primaryKey ).isSynchronized();
	}

	@Override
	public void destroy(RecordBase<?> base, int primaryKey)
	{
		if(hasCache(base, primaryKey ))
		{
			cache.get( base).remove(primaryKey );
		}
		super.destroy( base,primaryKey );
	}

	@Override
	public Map<String, Object> findFirstWithData( RecordBase<?> base, String[] columns, Condition condition )
	{
		//1.check in cache for conditions
		Map<String,Object> res;
		if(cache.containsKey( base))
		{
			res= cache.get( base).entrySet().stream().filter( (Map.Entry<Integer,RowCache> e) ->
			{
				return condition.test( e.getValue().toMap() );
			}).map( (Map.Entry<Integer,RowCache> e) -> e.getValue().toMap()).sorted( base.getDefaultOrder()).findFirst().orElse( null);
			if(res!=null)
			{
				return res;
			}
		}
		//1.1 load from DB if not found
		Map<String,Object> map = super.findFirstWithData( base, columns, condition );
		//2. store in cache
		if(map!=null && !map.isEmpty())
		{
			getCache(base, ( Integer ) map.get( base.getPrimaryColumn())).update( map, false );
		}
		return map;
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData( RecordBase<?> base, String[] columns, Condition condition )
	{
		//need to combine results from cache and DB -> too expensive, so just grab from DB and store in cache
		//fails if correct value is in cache but not in DB -> save all cache
		saveAll( base );
		return super.streamAllWithData( base, columns, condition ).peek( (Map<String,Object> map )-> 
		{
			getCache(base, ( Integer ) map.get( base.getPrimaryColumn())).update( map, false );
		});
	}

	@Override
	public String[] getAllColumnNames( String tableName )
	{
		String[] columns=columnsCache.get( tableName );
		if(columns==null)
		{
			columns = super.getAllColumnNames( tableName );
			columnsCache.put( tableName, columns );
		}
		return columns;
	}

	@Override
	public void clearCache(RecordBase<?> base, int primaryKey )
	{
		if(hasCache( base, primaryKey ))
		{
			cache.get( base).get( primaryKey).clear();
		}
	}

	@Override
	public boolean isCached()
	{
		return true;
	}

	@Override
	public int count(RecordBase<?> base, Condition condition )
	{
		//see #streamAllWithData for why there need to be a save
		saveAll( base );
		return super.count( base, condition );
	}
}