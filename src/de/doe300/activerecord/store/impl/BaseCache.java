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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.RecordBase;

/**
 * Caches all rows of one DB TABLE
 * @author doe300
 */
class BaseCache
{
	private final Map<Integer, RowCache> cachedRows;
	private final RecordBase<?> base;

	private final Set<RowCache> modifiedRows;

	BaseCache(@Nonnull final RecordBase<?> base)
	{
		this.cachedRows = Collections.synchronizedSortedMap( new TreeMap<>());
		this.base = base;
		this.modifiedRows = Collections.synchronizedSet( new HashSet<>(10));
	}

	/**
	 * @param primaryKey
	 * @return the row or <code>null</code>
	 */
	@Nullable
	public RowCache getRow(final int primaryKey)
	{
		return cachedRows.get( primaryKey );
	}

	@Nonnull
	private RowCache createRow(final int primaryKey)
	{
		final RowCache cache = new RowCache(this, base.getPrimaryColumn(), base.isTimestamped() );
		cache.setData( base.getPrimaryColumn(), primaryKey, false);
		cachedRows.put( primaryKey, cache );
		return cache;
	}

	/**
	 *
	 * @param primaryKey
	 * @return the existing or newly created row
	 */
	@Nonnull
	public RowCache getOrCreateRow(final int primaryKey)
	{
		if(cachedRows.containsKey( primaryKey))
		{
			return getRow( primaryKey );
		}
		return createRow( primaryKey );
	}

	/**
	 *
	 * @param primaryKey
	 * @return whether the row is cached
	 */
	public boolean containsRow(final int primaryKey)
	{
		return cachedRows.containsKey( primaryKey );
	}

	/**
	 * @param primaryKey
	 */
	public void removeRow(final int primaryKey)
	{
		cachedRows.remove( primaryKey );
	}

	/**
	 * Called by RowCache to invalidate the BaseCache
	 * 
	 * @param row
	 */
	public void setModified(@Nonnull final RowCache row)
	{
		modifiedRows.add( row );
	}

	/**
	 * Writes all changes from this table
	 * @param store
	 * @return whether some data was written
	 */
	public boolean writeAllBack(@Nonnull final CachedJDBCRecordStore store)
	{
		if(modifiedRows.isEmpty())
		{
			return false;
		}
		boolean changed = false;
		synchronized(modifiedRows)
		{
			//TODO write with batched simple/prepared statements
			for(final RowCache cache : modifiedRows)
			{
				if(cache.writeBack( store, base ))
				{
					changed = true;
				}
			}
			modifiedRows.clear();
		}
		return changed;
	}
}
