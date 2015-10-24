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

import java.util.UUID;
import javax.annotation.Nonnull;

/**
 * @author doe300
 * @since 0.5
 */
public class PostgreSQLDriver extends JDBCDriver
{
	PostgreSQLDriver()
	{
		
	}

	//TODO check all the postgre-speicalities
	@Override
	public String getPrimaryKeyKeywords(@Nonnull final String primaryKeyKeywords)
	{
		return "SERIAL PRIMARY KEY";
	}

	@Override
	public String getInsertDataForEmptyRow( String primaryColumn )
	{
		//TODO doesn't work if, can't fetch RETURNING
		return "DEFAULT VALUES";
	}

	@Override
	public Class<?> getJavaType( String sqlType ) throws IllegalArgumentException
	{
		if(sqlType.toUpperCase().startsWith( "UUID"))
		{
			return UUID.class;
		}
		if(sqlType.toUpperCase().startsWith( "SERIAL"))
		{
			return Integer.class;
		}
		if(sqlType.toUpperCase().startsWith( "BPCHAR"))
		{
			return String.class;
		}
		return super.getJavaType( sqlType );
	}

	@Override
	public String getSQLType(Class<?> javaType ) throws IllegalArgumentException
	{
		if(UUID.class.equals( javaType))
		{
			return "UUID";
		}
		return super.getSQLType( javaType );
	}

	@Override
	public String getLimitClause( int offset, int limit )
	{
		return (limit > 0 ? "LIMIT " + limit : "") + (offset > 0 ? " OFFSET " + offset : "");
	}
}
