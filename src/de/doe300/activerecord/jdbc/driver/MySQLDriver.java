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
package de.doe300.activerecord.jdbc.driver;

import java.sql.SQLXML;

/**
 * Vendor-specific driver for MySQL Databases
 * <br>
 * These settings include:
 * <ul>
 * <li>The keyword for the auto-increment primary key is set to <code>AUTO_INCREMENT</code></li>
 * <li>The default data-type for strings is set to <code>VARCHAR(4096)</code>.
 * The maximum limit for a cell width is 65535 which is simultaneously the maximum width for all columns in a row.
 * Since we don't know how much cells a row will have, I set the limit to 4096 which allows for up to 16 such string-column.
 * </li>
 * </ul>
 * 
 * @author doe300
 * @since 0.5
 */
public class MySQLDriver extends JDBCDriver
{
	MySQLDriver()
	{
	}

	@Override
	public String getAggregateFunction( String aggregateFunction, String column )
	{
		switch(aggregateFunction)
		{
			case AGGREGATE_SUM:
				return "CAST(SUM(" + column + ") AS SIGNED INTEGER)";
			case AGGREGATE_SUM_DOUBLE:
				return "SUM(" + column + ") + 0.0";
		}
		return super.getAggregateFunction( aggregateFunction, column );
	}
	
	@Override
	public String getAutoIncrementKeyword()
	{
		return "AUTO_INCREMENT";
	}

	@Override
	public String getStringDataType()
	{
		return "VARCHAR(4096)";
	}

	@Override
	public String getLimitClause( int offset, int limit )
	{
		if(offset <= 0 && limit < 0)
		{
			return "";
		}
		return "LIMIT " + (offset > 0 ? offset +", " : "0, ") + (limit > 0 ? limit+"": "");
	}

	@Override
	public String getSQLType(Class<?> javaType ) throws IllegalArgumentException
	{
		if(SQLXML.class.isAssignableFrom( javaType ))
		{
			return getStringDataType();
		}
		return super.getSQLType( javaType );
	}
	
	
}
