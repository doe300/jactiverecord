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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.jdbc.VendorSpecific;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.scope.Scope;
import java.util.Collections;

/**
 * Uses write-back cache
 *
 * @author doe300
 */
public class CachedJDBCRecordStore extends SimpleJDBCRecordStore
{
	private final Map<RecordBase<?>, BaseCache> cache;
	private final Map<String,Set<String>> columnsCache;
	private final Map<String, Boolean> tableExistsCache;

	/**
	 * @param con
	 */
	public CachedJDBCRecordStore( final Connection con)
	{
		this( con, VendorSpecific.guessDatabaseVendor( con ) );
	}

	/**
	 *
	 * @param con
	 * @param vendorSpecifics
	 */
	public CachedJDBCRecordStore( final Connection con, final VendorSpecific vendorSpecifics)
	{
		super( con, vendorSpecifics );
		this.cache=Collections.synchronizedMap( new HashMap<>(10));
		this.columnsCache = Collections.synchronizedSortedMap( new TreeMap<>());
		tableExistsCache = Collections.synchronizedSortedMap( new TreeMap<>());
	}

	private RowCache getCache(final RecordBase<?> base, final Integer primaryKey)
	{
		BaseCache tableCache = cache.get( base);
		if(tableCache == null)
		{
			tableCache = new BaseCache(base);
			cache.put( base, tableCache );
		}
		return tableCache.getOrCreateRow(primaryKey);
	}

	private boolean hasCache(final RecordBase<?> base, final int primaryKey)
	{
		return cache.containsKey( base) && cache.get( base).containsRow(primaryKey);
	}

	@Override
	public boolean exists(final String tableName)
	{
		if(Boolean.TRUE.equals( tableExistsCache.get( tableName)))
		{
			return true;
		}
		boolean exists = super.exists( tableName );
		tableExistsCache.put(tableName, exists);
		return exists;
	}

	@Override
	public boolean containsRecord( final RecordBase<?> base, final Integer primaryKey )
	{
		if(hasCache( base, primaryKey ))
		{
			return true;
		}
		return super.containsRecord( base, primaryKey );
	}

	@Override
	public void setValue( final RecordBase<?> base, final int primaryKey, final String name, final Object value ) throws IllegalArgumentException
	{
		getCache(base, primaryKey ).setData( name, value, base.isTimestamped());
	}

	@Override
	public void setValues( final RecordBase<?> base, final int primaryKey, final String[] names, final Object[] values ) throws IllegalArgumentException
	{
		getCache(base, primaryKey ).setData( names, values, base.isTimestamped() );
	}

	@Override
	public void setValues( final RecordBase<?> base, final int primaryKey, final Map<String, Object> values ) throws IllegalArgumentException
	{
		getCache( base, primaryKey ).update( values, true );
	}

	@Override
	public Object getValue( final RecordBase<?> base, final int primaryKey, final String name ) throws IllegalArgumentException
	{
		final RowCache c = getCache(base, primaryKey );
		if(c.hasData( name ))
		{
			return c.getData( name );
		}
		// write changes in cache to DB so #getDBValue does not override cached
		// changes with old data
		save(base, primaryKey);
		final Object value=getDBValue( base, primaryKey, c, name);
		c.setData( name, value, false );
		return value;
	}

	@Override
	public Map<String, Object> getValues( final RecordBase<?> base, final int primaryKey, final String[] columns ) throws IllegalArgumentException
	{
		final Map<String,Object> result = new HashMap<>(columns.length);
		final RowCache c = getCache( base,primaryKey );
		//write changes in cache to DB so #getDBValue does not override cached changes with old data
		save( base, primaryKey );
		for(final String col:columns)
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
				final Object val = getDBValue( base, primaryKey, c, col );
				result.put( col, val );
			}
		}
		return result;
	}

	/* Loads the whole row into cache at once */
	private Object getDBValue(final RecordBase<?> base, final int primaryKey, final RowCache cache, final String name) throws IllegalArgumentException
	{
		final String sql =
			"SELECT * FROM " + base.getTableName() + " WHERE " + base.getPrimaryColumn() + " = " + primaryKey;
		Logging.getLogger().debug("CachedJDBCStore", "Loading into cache...");
		Logging.getLogger().debug("CachedJDBCStore", sql);
		try (Statement stmt = con.createStatement(); final ResultSet res = stmt.executeQuery(sql))
		{
			if(res.next())
			{
				cache.update( res, false);
				return cache.getData( name );
			}
			return null;
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "CachedJDBCStore", "Failed to load into cache!");
			Logging.getLogger().error( "CachedJDBCStore", sql);
			Logging.getLogger().error( "CachedJDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public boolean save(final RecordBase<?> base, final int primaryKey)
	{
		//is write-back cache, so synchronization needs to be called explicitely
		if(hasCache( base, primaryKey ))
		{
			final RowCache c = getCache(base, primaryKey );
			if(c.writeBack( this, base ))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean saveAll(final RecordBase<?> base)
	{
		BaseCache baseCache = cache.get( base);
		if(baseCache == null)
		{
			return false;
		}
		boolean changed = baseCache.writeAllBack( this );
		if(changed)
		{
			Logging.getLogger().debug( "CachedJDBCStore", "Cache entries saved!");
		}
		return changed;
	}

	/**
	 * This method is only to be used to write cache back to the DB
	 *
	 * @param base
	 * @param primaryKey
	 * @param values
	 */
	void setDBValues(RecordBase<?> base, int primaryKey, Map<String, Object> values)
	{
		super.setValues( base, primaryKey, values );
	}

	@Override
	public boolean isSynchronized( final RecordBase<?> base, final int primaryKey )
	{
		return !hasCache( base, primaryKey ) || getCache( base, primaryKey ).isSynchronized();
	}

	@Override
	public void destroy(final RecordBase<?> base, final int primaryKey)
	{
		if(hasCache(base, primaryKey ))
		{
			cache.get( base).removeRow(primaryKey );
			Logging.getLogger().debug( "CachedJDBCStore", "Cache entry destroyed!");
		}
		super.destroy( base,primaryKey );
	}

	@Override
	public Map<String, Object> findFirstWithData( final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		//0. write cache into DB
		saveAll( base );
		//1 load from DB
		final Map<String,Object> map = super.findFirstWithData( base, columns, scope );
		//2. store in cache
		if(map!=null && !map.isEmpty())
		{
			getCache(base, ( Integer ) map.get( base.getPrimaryColumn())).update( map, false );
		}
		return map;
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData( final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		//need to combine results from cache and DB -> too expensive, so just grab from DB and store in cache
		//fails if correct value is in cache but not in DB -> save all cache
		saveAll( base );
		return super.streamAllWithData( base, columns, scope ).peek( (final Map<String,Object> map )->
		{
			getCache(base, ( Integer ) map.get( base.getPrimaryColumn())).update( map, false );
		});
	}

	@Override
	public Set<String> getAllColumnNames( final String tableName )
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
	public void clearCache(final RecordBase<?> base, final int primaryKey )
	{
		if(hasCache( base, primaryKey ))
		{
			cache.get( base).getRow(primaryKey).clear();
		}
	}

	@Override
	public boolean isCached()
	{
		return true;
	}

	@Override
	public int count(final RecordBase<?> base, final Condition condition )
	{
		//see #streamAllWithData for why there need to be a save
		saveAll( base );
		return super.count( base, condition );
	}

	@Override
	public void touch(RecordBase<?> base, int primaryKey )
	{
		super.touch( base, primaryKey );
		//required to touch data in DB
		save( base, primaryKey );
	}
}
