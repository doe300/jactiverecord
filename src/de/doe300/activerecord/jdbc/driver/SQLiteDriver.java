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

import javax.annotation.Nonnull;

/**
 * Vendor-specific settings for the SQLite database, including:
 * <ul>
 * <li>According to the official documentation (<a href="https://www.sqlite.org/autoinc.html">SQLite doc autoincrement</a>),
 * the <code>AUTOINCREMENT</code> keyword should be avoided and <code>INTEGER PRIMARY KEY</code> implies an automatic increment.
 * </li>
 * <li>According to the official documentation (<a href="https://www.sqlite.org/faq.html#q9">SQLite FAQ</a>),
 * the length of a <code>VARCHAR</code> is not limited and the length-value is ignored.
 * </li>
 * <li>SQLite has no <code>boolean</code>-type (<a href="https://www.sqlite.org/datatype3.html">SQLite data-types</a>),
 * so the <code>boolean</code> values are stored as integers, zero (0) for <code>false</code>, one (1) for <code>true</code>
 * </li>
 * </ul>
 *
 * @author doe300
 * @since 0.5
 */
public class SQLiteDriver extends JDBCDriver
{
	SQLiteDriver()
	{
	}

	@Override
	public String getPrimaryKeyKeywords(@Nonnull final String primaryKeyKeywords)
	{
		return primaryKeyKeywords + " PRIMARY KEY";
	}

	@Override
	public String getStringDataType()
	{
		return "VARCHAR(1)";
	}
	
	@Override
	public String convertBooleanToDB( final boolean value )
	{
		return value ? "1" : "0";
	}

	@Override
	public boolean convertDBToBoolean( final Object value )
	{
		int val;
		if(value instanceof Number)
		{
			val = ((Number)value).intValue();
		}
		else
		{
			val = Integer.valueOf( value.toString() );
		}
		//value of 1 is true, 0 is false
		return val != 0;
	}

	@Override
	public Class<?> getJavaType( String sqlType ) throws IllegalArgumentException
	{
		return super.getJavaType( sqlType );
	}

	@Override
	public String getSQLType(Class<?> javaType ) throws IllegalArgumentException
	{
		if(Boolean.class.equals( javaType) || Boolean.TYPE.equals( javaType ))
		{
			return "TINYINT";
		}
		return super.getSQLType( javaType );
	}
	
	@Override
	public String getLimitClause( int offset, int limit )
	{
		return (limit > 0 ? "LIMIT " + limit : "") + (offset > 0 ? " OFFSET " + offset : "");
	}
	
}
