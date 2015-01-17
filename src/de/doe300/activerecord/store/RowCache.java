
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
	private final String tableName;

	private RowCache(String primaryKey, String tableName, Map<String,Object> columnData, boolean inDB)
	{
		this.primaryKey = primaryKey.toLowerCase();
		this.tableName = tableName;
		this.columnData = columnData;
		columnData.put( RecordStore.COLUMN_CREATED_AT, new Timestamp(System.currentTimeMillis()));
		this.isInDB = inDB;
	}
	
	/**
	 * Only supports ResultSet of one single TABLE.
	 * Chooses the first auto-increment column as primary key
	 * @param set
	 * @return
	 * @throws SQLException 
	 */
	public static RowCache fromResultRow(ResultSet set) throws SQLException
	{
		Map<String,Object> data = new HashMap<>(set.getMetaData().getColumnCount());
		String primaryKey=null, tableName = null;
		for(int i=1;i<=set.getMetaData().getColumnCount();i++)
		{
			data.put( set.getMetaData().getColumnLabel( i).toLowerCase(), set.getObject( i ));
			if(primaryKey==null && set.getMetaData().isAutoIncrement( i ))
			{
				primaryKey = set.getMetaData().getColumnLabel( i );
				tableName = set.getMetaData().getTableName(i);
			}
		}
		return new RowCache(primaryKey,tableName,data, true );
	}
	
	/**
	 * Filters all columns retrieved from the TABLE with the given name, ignores the rest
	 * @param tableName
	 * @param primaryKey
	 * @param set
	 * @return
	 * @throws SQLException 
	 */
	public static RowCache fromResultRow(String tableName, String primaryKey, ResultSet set) throws SQLException
	{
		Map<String,Object> data = new HashMap<>(set.getMetaData().getColumnCount());
		for(int i=1;i<=set.getMetaData().getColumnCount();i++)
		{
			if(!set.getMetaData().getTableName( i ).equals( tableName ))
			{
				continue;
			}
			data.put( set.getMetaData().getColumnLabel( i).toLowerCase(), set.getObject( i ));
		}
		return new RowCache(primaryKey,tableName,data, true);
	}
	
	public static RowCache fromMap(String tableName, String primaryKey, Map<String,Object> map)
	{
		return new RowCache(primaryKey,tableName,new HashMap<>(map ),false );
	}
	
	public static RowCache emptyCache(String tableName, String primaryKey)
	{
		return new RowCache(primaryKey, tableName, new HashMap<>(10),false);
	}
	
	public Object setData(String columnName, Object value, boolean updateTimestamp)
	{
		if(Objects.equals( RowCache.this.getData( columnName.toLowerCase()), value))
		{
			return value;
		}
		dataChanged = true;
		if(updateTimestamp)
		{
			columnData.put( RecordStore.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		return columnData.put( columnName.toLowerCase(), value );
	}
	
	public void setData(String[] names, Object[] values, boolean updateTimestamp)
	{
		for(int i=0;i<names.length;i++)
		{
			columnData.put( names[i].toLowerCase(), values[i]);
		}
		if(updateTimestamp)
		{
			columnData.put( RecordStore.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		dataChanged = true;
	}
	
	public Object getData(String columnName)
	{
		return columnData.get( columnName.toLowerCase() );
	}
	
	public boolean hasData(String columnName)
	{
		return columnData.containsKey( columnName.toLowerCase() );
	}
	
	public int getPrimaryKey()
	{
		return ( int ) columnData.get( primaryKey );
	}
	
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
	public void setSynchronized()
	{
		dataChanged = false;
	}
	
	public void clear()
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
	public void update(ResultSet set, boolean updateTimestamp) throws SQLException
	{
		for(int i=1;i<=set.getMetaData().getColumnCount();i++)
		{
				setData( set.getMetaData().getColumnLabel( i).toLowerCase(), set.getObject( i ),updateTimestamp);
		}
		isInDB = true;
		dataChanged =false;
	}
	
	public Map<String,Object> toMap()
	{
		//needs to be modifiable and a copy
		return new HashMap<>(columnData);
	}
	
	public void update(Map<String,Object> map, boolean updateTimestamp)
	{
		for(Map.Entry<String,Object> e:map.entrySet())
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
	public boolean equals( Object obj )
	{
		return (obj instanceof RowCache) && obj.toString().equals( toString());
	}

	@Override
	public int compareTo( RowCache o )
	{
		if(o.getTableName().equals( getTableName()))
		{
			return Integer.compare( getPrimaryKey(), o.getPrimaryKey());
		}
		return getTableName().compareTo( o.getTableName());
	}	
}
