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

import de.doe300.activerecord.util.Pair;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.scope.Scope;
import java.util.Arrays;

/**
 * Uses write-back cache
 *
 * @author doe300
 */
public class CachedJDBCRecordStore extends SimpleJDBCRecordStore
{
	private final Map<RecordBase<?>, BaseCache> cache;
	private final Map<String, Map<String, Class<?>>> columnsCache;
	private final Map<String, Boolean> tableExistsCache;

	/**
	 * @param con
	 */
	public CachedJDBCRecordStore(@Nonnull final Connection con)
	{
		this( con, JDBCDriver.guessDriver( con ) );
	}

	/**
	 *
	 * @param con
	 * @param driver
	 */
	public CachedJDBCRecordStore(@Nonnull final Connection con, final JDBCDriver driver)
	{
		super( con, driver );
		this.cache=Collections.synchronizedMap( new HashMap<>(10));
		this.columnsCache = Collections.synchronizedSortedMap( new TreeMap<>());
		tableExistsCache = Collections.synchronizedSortedMap( new TreeMap<>());
	}

	private RowCache getCache(final RecordBase<?> base, final Integer primaryKey)
	{
		if(!super.containsRecord( base, primaryKey ))
		{
			throw new IllegalArgumentException("Invalid row ID: " + primaryKey);
		}
		BaseCache tableCache = cache.get( base);
		if(tableCache == null)
		{
			tableCache = new BaseCache(base);
			cache.put( base, tableCache );
		}
		//FIXME currently, if a record is created and data is set before read the cache can't check if the data was modified
		//because it is not yet filled with the DB-data
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
		final boolean exists = super.exists( tableName );
		tableExistsCache.put(tableName, exists);
		return exists;
	}

	@Override
	public boolean containsRecord( final RecordBase<?> base, final int primaryKey )
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
		if(!getAllColumnNames( base.getTableName()).contains( name))
		{
			throw new IllegalArgumentException("No such column for the name: " + name);
		}
		getCache(base, primaryKey ).setData( name, value, true);
	}

	@Override
	public void setValues( final RecordBase<?> base, final int primaryKey, final String[] names, final Object[] values ) throws IllegalArgumentException
	{
		if(names.length != values.length)
		{
			throw new IllegalArgumentException("Number of columns and values do not match!");
		}
		if(!getAllColumnNames( base.getTableName()).containsAll( Arrays.asList( names)))
		{
			throw new IllegalArgumentException("No such columns for the names: " + Arrays.toString( names));
		}
		getCache(base, primaryKey ).setData( names, values );
	}

	@Override
	public void setValues( final RecordBase<?> base, final int primaryKey, final Map<String, Object> values ) throws IllegalArgumentException
	{
		if(!getAllColumnNames( base.getTableName()).containsAll( values.keySet()))
		{
			throw new IllegalArgumentException("No such columns for the names: " + values.keySet());
		}
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
		final Object value = getDBValue( base, primaryKey, c, name).getFirst();
		c.setData( name, value, false );
		return value;
	}

	@Override
	public Map<String, Object> getValues( final RecordBase<?> base, final int primaryKey, final String[] columns ) throws IllegalArgumentException
	{
		final Map<String,Object> result = new HashMap<>(columns.length);
		final RowCache c = getCache( base,primaryKey );
		for(final String col:columns)
		{
			if(c.hasData( col ))
			{
				result.put( col, c.getData( col));
			}
			//this else clause is only called the first time a column is not in the cache
			else
			{
				//write changes in cache to DB so #getDBValue does not override cached changes with old data
				save( base, primaryKey );
				final Pair<Object, Boolean> val = getDBValue( base, primaryKey, c, col );
				if(!val.getSecond())
				{
					//no data was written into cache -> row was not read from DB -> every successive call will fail to read data too
					break;
				}
				result.put( col, val.getFirst() );
			}
		}
		return result;
	}

	@Override
	public Stream<Object> getValues( String tableName, String column, String condColumn, Object condValue ) throws IllegalArgumentException
	{
		//need to write cache into DB(if request on managed/cached table)
		for(final Map.Entry<RecordBase<?>, BaseCache> c : cache.entrySet())
		{
			if(c.getKey().getTableName().equals( tableName))
			{
				if(c.getValue() != null)
				{
					c.getValue().writeAllBack( this );
				}
			}
		}
		return super.getValues( tableName, column, condColumn, condValue );
	}

	/* Loads the whole row into cache at once */
	@Nonnull
	private Pair<Object, Boolean> getDBValue(final RecordBase<?> base, final int primaryKey, final RowCache cacheRow, final String name) throws IllegalArgumentException
	{
		final String sql =
			"SELECT * FROM " + base.getTableName() + " WHERE " + base.getPrimaryColumn() + " = " + primaryKey;
		Logging.getLogger().debug("CachedJDBCStore", "Loading into cache...");
		Logging.getLogger().debug("CachedJDBCStore", sql);
		try (Statement stmt = con.createStatement(); final ResultSet res = stmt.executeQuery(sql))
		{
			if(res.next())
			{
				cacheRow.update( res);
				return Pair.createPair( cacheRow.getData( name ), true);
			}
			//no such row in DB
			return Pair.createPair( null, false);
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
		final BaseCache baseCache = cache.get( base);
		if(baseCache == null)
		{
			return false;
		}
		final boolean changed = baseCache.writeAllBack( this );
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
	void setDBValues(@Nonnull final RecordBase<?> base, final int primaryKey, @Nonnull final Map<String, Object> values)
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
		if (!map.isEmpty())
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
		Map<String, Class<?>> columns=columnsCache.get( tableName );
		if(columns==null)
		{
			columns = super.getAllColumnTypes(tableName );
			columnsCache.put( tableName, columns );
		}
		return columns.keySet();
	}

	@Override
	public Map<String, Class<?>> getAllColumnTypes( String tableName ) throws IllegalArgumentException
	{
		Map<String, Class<?>> columns=columnsCache.get( tableName );
		if(columns==null)
		{
			columns = super.getAllColumnTypes(tableName );
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
	public <R> R aggregate(RecordBase<?> base, AggregateFunction<?, ?, ?, R> aggregateFunction, Condition condition )
	{
		//see #streamAllWithData for why there need to be a save
		saveAll( base );
		return super.aggregate( base, aggregateFunction, condition );
	}

	@Override
	public void touch(final RecordBase<?> base, final int primaryKey )
	{
		super.touch( base, primaryKey );
		//required to touch data in DB
		save( base, primaryKey );
	}

	@Override
	public boolean dropTable( String tableName ) throws SQLException
	{
		tableExistsCache.remove( tableName);
		return super.dropTable( tableName );
	}
}
