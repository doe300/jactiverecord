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
package de.doe300.activerecord.generation;

import javax.annotation.Nonnull;

/**
 * The data-type of an attribute
 * 
 * @author doe300
 */
public enum AttributeType
{
	BOOLEAN(Boolean.class.getCanonicalName(), java.sql.Types.BIT),
	BYTE(Byte.class.getCanonicalName(), java.sql.Types.TINYINT),
	SHORT(Short.class.getCanonicalName(), java.sql.Types.SMALLINT),
	INTEGER(Integer.class.getCanonicalName(), java.sql.Types.INTEGER),
	LONG(Long.class.getCanonicalName(), java.sql.Types.BIGINT),
	FLOAT(Float.class.getCanonicalName(), java.sql.Types.REAL),
	DOUBLE(Double.class.getCanonicalName(), java.sql.Types.DOUBLE),
	DATE(java.sql.Date.class.getCanonicalName(), java.sql.Types.DATE),
	TIME(java.sql.Time.class.getCanonicalName(), java.sql.Types.TIME),
	TIMESTAMP(java.sql.Timestamp.class.getCanonicalName(), java.sql.Types.TIMESTAMP),
	STRING(String.class.getCanonicalName(), java.sql.Types.VARCHAR);
	
	private final String javaType;
	private final int sqlType;

	private AttributeType(@Nonnull final String javaType, final int sqlType )
	{
		this.javaType = javaType;
		this.sqlType = sqlType;
	}
	
	@Nonnull
	public String getType()
	{
		return javaType;
	}
	
	public int getSQLType()
	{
		return sqlType;
	}
}
