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
import de.doe300.activerecord.store.RowCache;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.scope.Scope;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Uses write-back cache
 * @author doe300
 */
public class CachedJDBCRecordStore extends SimpleJDBCRecordStore implements RecordStore
{
	private final Map<RecordBase<?>, Map<Integer, RowCache>> cache;
	private final Map<String,Set<String>> columnsCache;

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
			String sql = "SELECT * FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
			Logging.getLogger().debug( "CachedJDBCStore", "Loading into cache...");
			Logging.getLogger().debug( "CachedJDBCStore", sql);
			ResultSet res = stmt.executeQuery(sql );
			if(res.next())
			{
				cache.update( res, false);
				return cache.getData( name );
			}
			return null;
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "CachedJDBCStore", "Failed to load into cache!");
			Logging.getLogger().error( "CachedJDBCStore", ex);
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
				Map<String,Object> values = c.toMap();
				super.setValues( base, primaryKey, values );
				Logging.getLogger().debug( "CachedJDBCStore", "Cache entry saved!");
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
		if(changed)
		{
			Logging.getLogger().debug( "CachedJDBCStore", "Cache entries saved!");
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
			Logging.getLogger().debug( "CachedJDBCStore", "Cache entry destroyed!");
		}
		super.destroy( base,primaryKey );
	}

	@Override
	public Map<String, Object> findFirstWithData( RecordBase<?> base, String[] columns, Scope scope )
	{
		//1.check in cache for conditions
		Map<String,Object> res;
		if(cache.containsKey( base))
		{
			res= cache.get( base).entrySet().stream().filter( (Map.Entry<Integer,RowCache> e) ->
			{
				return scope.getCondition().test( e.getValue().toMap() );
			}).map( (Map.Entry<Integer,RowCache> e) -> e.getValue().toMap()).sorted( toOrder( base, scope )).findFirst().orElse( null);
			if(res!=null)
			{
				return res;
			}
		}
		//TODO is wrong, if a matching record is in cache could still be first in DB
		//1.1 load from DB if not found
		Map<String,Object> map = super.findFirstWithData( base, columns, scope );
		//2. store in cache
		if(map!=null && !map.isEmpty())
		{
			getCache(base, ( Integer ) map.get( base.getPrimaryColumn())).update( map, false );
		}
		return map;
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData( RecordBase<?> base, String[] columns, Scope scope )
	{
		//need to combine results from cache and DB -> too expensive, so just grab from DB and store in cache
		//fails if correct value is in cache but not in DB -> save all cache
		saveAll( base );
		return super.streamAllWithData( base, columns, scope ).peek( (Map<String,Object> map )-> 
		{
			getCache(base, ( Integer ) map.get( base.getPrimaryColumn())).update( map, false );
		});
	}

	@Override
	public Set<String> getAllColumnNames( String tableName )
	{
		Set<String> columns=columnsCache.get( tableName );
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