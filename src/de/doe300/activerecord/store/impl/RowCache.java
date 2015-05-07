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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.doe300.activerecord.record.TimestampedRecord;
import java.util.Collections;

/**
 * Caches one single row of one DB TABLE
 * @author doe300
 */
class RowCache implements Comparable<RowCache>
{
	private final Map<String,Object> columnData;
	private boolean dataChanged = false;
	private final String primaryKey;
	private final boolean isTimestamped;
	private final BaseCache parent;

	RowCache(final BaseCache parent, final String primaryKey, final boolean isTimestamped)
	{
		this.parent = parent;
		this.primaryKey = primaryKey.toLowerCase();
		this.columnData = Collections.synchronizedMap( new HashMap<>(20));
		this.isTimestamped = isTimestamped;
		if(isTimestamped)
		{
			columnData.put( TimestampedRecord.COLUMN_CREATED_AT, new Timestamp(System.currentTimeMillis()));
		}
	}

	/**
	 * @param columnName
	 * @param value
	 * @param updateTimestamp
	 * @return the previous value or <code>null</code>
	 */
	public synchronized Object setData(final String columnName, final Object value, final boolean updateTimestamp)
	{
		if(Objects.equals( RowCache.this.getData( columnName.toLowerCase()), value))
		{
			return value;
		}
		setModified();
		if(isTimestamped && updateTimestamp)
		{
			columnData.put( TimestampedRecord.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		return columnData.put( columnName.toLowerCase(), value );
	}

	/**
	 * @param names
	 * @param values
	 * @param updateTimestamp
	 */
	public synchronized void setData(final String[] names, final Object[] values, final boolean updateTimestamp)
	{
		for(int i=0;i<names.length;i++)
		{
			columnData.put( names[i].toLowerCase(), values[i]);
		}
		if(isTimestamped && updateTimestamp)
		{
			columnData.put( TimestampedRecord.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		setModified();
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
		return !dataChanged;
	}

	/**
	 * Sets this cache to a synchronized state
	 */
	public synchronized void setSynchronized()
	{
		dataChanged = false;
	}

	/**
	 * clears all entries from the cache
	 */
	public synchronized void clear()
	{
		columnData.clear();
		dataChanged = false;
	}

	/**
	 * Updates the cache-entry with data from the DB
	 * @param set
	 * @param updateTimestamp
	 * @throws SQLException
	 */
	public synchronized void update(final ResultSet set, final boolean updateTimestamp) throws SQLException
	{
		for(int i=1;i<=set.getMetaData().getColumnCount();i++)
		{
				setData( set.getMetaData().getColumnLabel( i).toLowerCase(), set.getObject( i ),updateTimestamp);
		}
		dataChanged =false;
	}

	/**
	 * Sets all cached values to the values given by <code>map</code>
	 * @param map
	 * @param updateTimestamp
	 */
	public synchronized void update(final Map<String,Object> map, final boolean updateTimestamp)
	{
		for(final Map.Entry<String,Object> e:map.entrySet())
		{
			setData( e.getKey().toLowerCase(), e.getValue(),updateTimestamp);
		}
	}
	
	public synchronized boolean writeBack(CachedJDBCRecordStore store, RecordBase<?> base)
	{
		if(!dataChanged)
		{
			return false;
		}
		//XXX could optimize to only write changed values, but how to know?
		store.setDBValues(base, getPrimaryKey(), columnData );
		dataChanged = false;
		return true;
	}
	
	public void setModified()
	{
		dataChanged = true;
		parent.setModified();
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
		return obj instanceof RowCache && obj.toString().equals( toString());
	}

	@Override
	public int compareTo( final RowCache o )
	{
		return Integer.compare( getPrimaryKey(), o.getPrimaryKey());
	}
}
