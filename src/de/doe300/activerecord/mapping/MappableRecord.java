package de.doe300.activerecord.mapping;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.HashMap;
import java.util.Map;

/**
 * An ActiveRecord which can be mapped to and from a {@link Map}
 * 
 * @author doe300
 */
public interface MappableRecord extends ActiveRecord
{
	/**
	 * Maps the attributes of this record to key-value pairs with the column-name as key and the cell-value as the value.
	 * @return the mapped attributes
	 */
	public default Map<String,Object> getAttributes()
	{
		String[] columns = getBase().getStore().getAllColumnNames( getBase().getTableName());
		Map<String,Object> attributes = new HashMap<>(columns.length);
		for(String col:columns)
		{
			attributes.put( col, getBase().getStore().getValue( getBase(), getPrimaryKey(), col));
		}
		return attributes;
	}
	
	/**
	 * 
	 * @param column
	 * @return the value for the given <code>column</code>
	 */
	public default Object getAttribute(String column)
	{
		return getBase().getStore().getValue( getBase(), getPrimaryKey(), column);
	}
	
	/**
	 * Sets all attributes for this record from the key-value pairs in the given <code>map</code>
	 * @param map 
	 */
	public default void setAttributes(Map<String,Object> map)
	{
		save();//TODO save is wrong, but how to make sure, changes are not omitted??
		getBase().getStore().setValues( getBase(), getPrimaryKey(), map);
		reload();
	}
	
	/**
	 * Sets the given attribute for this record
	 * @param column
	 * @param value 
	 */
	public default void setAttribute(String column, Object value)
	{
		save();
		getBase().getStore().setValue( getBase(), getPrimaryKey(), column, value);
		reload();
	}
}