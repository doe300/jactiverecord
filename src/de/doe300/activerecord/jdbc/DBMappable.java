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
package de.doe300.activerecord.jdbc;

import de.doe300.activerecord.migration.Attribute;

/**
 * An interface to map custom types to db-types.
 * 
 * Any implementation of this interface must have a public default-constructor.
 * 
 * This interface is supported by {@link TypeMappings}
 * @author doe300
 * @see TypeMappings
 */
public interface DBMappable
{
	/**
	 * Reads the data from the db-value.
	 * 
	 * The <code>dbValue</code> is of the JDBC-mapped java-type for the specified {@link Attribute#type() SQL-type}
	 * 
	 * @param dbValue 
	 */
	public void readFromDBValue(Object dbValue);
	
	/**
	 * The return-type must be compatible to the JDBC-mapped java-type for the {@link Attribute#type() SQL-type}
	 * @return the db-value containing the attributes of this object
	 */
	public Object toDBValue();
}
