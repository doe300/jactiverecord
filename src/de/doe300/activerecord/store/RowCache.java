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
package de.doe300.activerecord.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Caches one single row of one DB TABLE
 * @author doe300
 */
public class RowCache implements Comparable<RowCache>
{
	private final Map<String,Object> columnData;
	private boolean dataChanged = false;
	private final String primaryKey;
	private boolean isInDB;
	private final boolean isTimestamped;
	private final String tableName;

	private RowCache(final String primaryKey, final String tableName, final Map<String,Object> columnData, final boolean inDB, final boolean isTimestamped)
	{
		this.primaryKey = primaryKey.toLowerCase();
		this.tableName = tableName;
		this.columnData = columnData;
		this.isTimestamped = isTimestamped;
		if(isTimestamped)
		{
			columnData.put( RecordStore.COLUMN_CREATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		this.isInDB = inDB;
	}

	/**
	 * Creates a new cache-entry and fills it with the values from the given map.
	 * @param tableName
	 * @param primaryKey
	 * @param map
	 * @param isTimestamped
	 * @return the newly created cache-entry
	 */
	public static RowCache fromMap(final String tableName, final String primaryKey, final Map<String,Object> map, final boolean isTimestamped)
	{
		return new RowCache(primaryKey,tableName,new HashMap<>(map ),false, isTimestamped );
	}

	/**
	 * @param tableName
	 * @param primaryKey
	 * @param isTimestamped
	 * @return a new created empty cache-row
	 */
	public static RowCache emptyCache(final String tableName, final String primaryKey, final boolean isTimestamped)
	{
		return new RowCache(primaryKey, tableName, new HashMap<>(10),false, isTimestamped);
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
		dataChanged = true;
		if(isTimestamped && updateTimestamp)
		{
			columnData.put( RecordStore.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
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
			columnData.put( RecordStore.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		dataChanged = true;
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
	 * @return the name of the associated table
	 */
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * @return whether this row is represented in the DB
	 */
	public boolean isInDB()
	{
		return isInDB;
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
		isInDB = true;
		dataChanged =false;
	}

	/**
	 * @return a map with all cached values
	 */
	public Map<String,Object> toMap()
	{
		//needs to be modifiable and a copy
		return new HashMap<>(columnData);
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

	@Override
	public String toString()
	{
		return tableName+"@"+getPrimaryKey();
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
		if(o.getTableName().equals( getTableName()))
		{
			return Integer.compare( getPrimaryKey(), o.getPrimaryKey());
		}
		return getTableName().compareTo( o.getTableName());
	}
}
