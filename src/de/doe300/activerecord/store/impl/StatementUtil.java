/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.dsl.Condition;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author doe300
 * @since 0.7
 */
final class StatementUtil
{
	
	/**
	 * Converts a SQL-statement with host-parameters ("?") into a SQL-statement without parameters by directly replacing
	 * the parameters with their corresponding values
	 * 
	 * @param sql the prepared statement including host-parameters ("?")
	 * @param condition
	 * @return the resulting statement
	 * @throws IllegalArgumentException if an unsupported value-type is encountered
	 */
	@Nonnull
	static String prepareQuery(@Nonnull final String sql, @Nullable final Condition condition)
	{
		if(condition == null || !condition.hasWildcards())
		{
			return sql;
		}
		String preparedSQL = sql;
		for(final Object val : condition.getValues())
		{
			preparedSQL = replaceNext( preparedSQL, val);
		}
		return preparedSQL;
	}
	
	/**
	 * Converts a SQL-statement with host-parameters ("?") into a SQL-statement without parameters by directly replacing
	 * the parameters with their corresponding values
	 * 
	 * @param sql the prepared statement including host-parameters ("?")
	 * @param values
	 * @return the resulting statement
	 * @throws IllegalArgumentException if an unsupported value-type is encountered
	 */
	@Nonnull
	static String prepareUpdate(@Nonnull final String sql, @Nullable final Object... values)
	{
		if(values == null || values.length == 0)
		{
			return sql;
		}
		String resultSQL = sql;
		for(final Object val : values)
		{
			resultSQL = replaceNext( resultSQL, val);
		}
		return resultSQL;
	}
	
	private static String replaceNext(@Nonnull final String sql, @Nullable final Object value)
	{
		if(value == null)
		{
			return sql.replaceFirst( "\\?", "NULL");
		}
		else if(value instanceof Number)
		{
			return sql.replaceFirst( "\\?", ((Number)value).toString());
		}
		else if(value instanceof String)
		{
			return sql.replaceFirst( "\\?", "'" + value + "'");
		}
		else if(value instanceof Date)
		{
			return sql.replaceFirst( "\\?", "'" + value + "'");
		}
		//TODO support for more types
		else
		{
			throw new IllegalArgumentException("Unsupported type: " + value.getClass());
		}
	}
	
	private StatementUtil()
	{
		
	}
}
