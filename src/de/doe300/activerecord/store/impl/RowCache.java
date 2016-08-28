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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import de.doe300.activerecord.record.TimestampedRecord;

/**
 * Caches one single row of one DB TABLE
 * @author doe300
 */
class RowCache implements Comparable<RowCache>
{
	@Nonnull
	private final Map<String,Object> columnData, modifiedData;
	private final String primaryKey;
	private final boolean isTimestamped;
	@Nonnull
	private final BaseCache parent;

	RowCache(@Nonnull final BaseCache parent, final String primaryKey, final boolean isTimestamped)
	{
		this.parent = parent;
		this.primaryKey = primaryKey.toLowerCase();
		this.columnData = Collections.synchronizedMap( new HashMap<>(10));
		this.modifiedData = new HashMap<>(10);
		this.isTimestamped = isTimestamped;
		if(isTimestamped)
		{
			final Timestamp stamp = new Timestamp(System.currentTimeMillis());
			columnData.put( TimestampedRecord.COLUMN_CREATED_AT, stamp);
		}
	}

	/**
	 * @param columnName
	 * @param value
	 * @param updateValues
	 * @return the previous value or <code>null</code>
	 */
	public synchronized Object setData(final String columnName, final Object value, final boolean updateValues)
	{
		final String columnLower = columnName.toLowerCase();
		//we need to check for existence, to allow storing null-objects, for later checks
		if(hasData( columnName ) && Objects.equals(getData( columnLower), value))
		{
			return value;
		}
		if(updateValues)
		{
			modifiedData.put( columnLower, value);
			parent.setModified( this );
			updateModifiedTimestamp();
		}
		return columnData.put( columnLower, value );
	}

	/**
	 * @param names
	 * @param values
	 */
	public synchronized void setData(final String[] names, final Object[] values)
	{
		//prevents updating only the timestamps
		boolean anyUpdates = false;
		for(int i=0;i<names.length;i++)
		{
			if(hasData( names[i].toLowerCase() ) && Objects.equals( getData( names[i].toLowerCase()), values[i]))
			{
				continue;
			}
			anyUpdates = true;
			modifiedData.put( names[i].toLowerCase(), values[i]);
			parent.setModified( this );
			columnData.put( names[i].toLowerCase(), values[i]);
		}
		if(anyUpdates && isTimestamped)
		{
			updateModifiedTimestamp();
		}
	}

	/**
	 * @param columnName
	 * @return the stored value
	 */
	public synchronized Object getData(final String columnName)
	{
		return columnData.get( columnName.toLowerCase() );
	}

	/**
	 * @param columnName
	 * @return whether the given column is cached
	 */
	public boolean hasData(final String columnName)
	{
		return columnData.containsKey( columnName.toLowerCase() );
	}

	/**
	 * @return the primary key of the associated record
	 */
	public int getPrimaryKey()
	{
		return ( int ) columnData.get( primaryKey );
	}

	/**
	 * This method does not consider external changes to the DB
	 * @return whether the data in this cache is the same as in the DB
	 */
	public boolean isSynchronized()
	{
		return modifiedData.isEmpty();
	}

	/**
	 * clears all entries from the cache (except primary key)
	 */
	public synchronized void clear()
	{
		int key = getPrimaryKey();
		columnData.clear();
		modifiedData.clear();
		columnData.put( primaryKey, key );
	}

	/**
	 * Updates the cache-entry with data from the DB
	 * @param set
	 * @throws SQLException
	 */
	public synchronized void update(final ResultSet set) throws SQLException
	{
		for(int i=1;i<=set.getMetaData().getColumnCount();i++)
		{
			setData( set.getMetaData().getColumnLabel( i).toLowerCase(), set.getObject( i ),false);
		}
		//loading from DB overwrites all modified data
		modifiedData.clear();
	}

	/**
	 * Sets all cached values to the values given by <code>map</code>
	 * @param map
	 * @param updateValues
	 */
	public synchronized void update(final Map<String,Object> map, final boolean updateValues)
	{
		for(final Map.Entry<String,Object> e:map.entrySet())
		{
			setData( e.getKey().toLowerCase(), e.getValue(), updateValues);
		}
	}

	public synchronized boolean writeBack(@Nonnull final WriteBack container)
	{
		if(modifiedData.isEmpty())
		{
			return false;
		}
		container.addRow( getPrimaryKey(), modifiedData);
		modifiedData.clear();
		return true;
	}

	@Override
	public String toString()
	{
		return "RowCache@"+getPrimaryKey();
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj != null && obj instanceof RowCache && parent.equals( ((RowCache)obj).parent) && obj.toString().equals( toString());
	}

	@Override
	public int compareTo( final RowCache o )
	{
		return Integer.compare( getPrimaryKey(), o.getPrimaryKey());
	}

	private void updateModifiedTimestamp()
	{
		//if no other data was changed, don't update modified
		if(!isTimestamped || modifiedData.isEmpty())
		{
			return;
		}
		final Timestamp stamp = new Timestamp(System.currentTimeMillis());
		columnData.put( TimestampedRecord.COLUMN_UPDATED_AT, stamp);
		modifiedData.put( TimestampedRecord.COLUMN_UPDATED_AT, stamp);
		parent.setModified( this );
	}
}
