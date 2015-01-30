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
package de.doe300.activerecord.mapping;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
		Set<String> columns = getBase().getStore().getAllColumnNames( getBase().getTableName());
		Map<String,Object> attributes = new HashMap<>(columns.size());
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
