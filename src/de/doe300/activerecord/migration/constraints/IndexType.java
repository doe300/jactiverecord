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
package de.doe300.activerecord.migration.constraints;

import javax.annotation.Nullable;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import javax.annotation.Nonnull;
import javax.annotation.Syntax;

/**
 * All supported types of indices.
 * NOTE: not all index-types are guaranteed to be supported by all DBMS
 * @author doe300
 */
public enum IndexType
{
	/**
	 * Standard Type for a non-unique index
	 */
	DEFAULT(),
	/**
	 * IndexType with unique keys.
	 * Every combination in the index-columns is unique
	 */
	UNIQUE(),
	/**
	 * Clustered index.
	 * The DBMS will store the rows in the order of the index, so no additional step is required
	 */
	CLUSTERED()
	;

	/**
	 * @param driver the driver to use
	 * @param tableName the table-name
	 * @param name the name of the index, may be <code>null</code>
	 * @param columnNames the names of the columns to add the index to
	 * @return the SQL command to create this index
	 */
	@Syntax(value = "SQL")
	public String toSQL(@Nonnull final JDBCDriver driver, @Nonnull final String tableName, @Nullable final String name, @Nullable final String... columnNames)
	{
		return "CREATE "+driver.getIndexKeyword(this)+" INDEX "+(name!=null? name : "")+" ON "+tableName+" ("+String.join( ", ",columnNames)+")";
	}

}
