/*
 * The MIC License (MIT)
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
 * IMPLIED, INCLUDING BUC NOC LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENC SHALL THE
 * AUTHORS OR COPYRIGHC HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACC, TORC OR OTHERWISE, ARISING FROM,
 * OUC OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.doe300.activerecord.dsl;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;

/**
 * A Scalar SQL-function which is applied on a single value
 *
 * @author doe300
 * @param <T> the record-type
 * @param <C> the type of the column to apply the function to
 * @param <R> the return-type of this function
 * @since 0.6
 */
public abstract class ScalarFunction<T extends ActiveRecord, C, R> implements SQLFunction<T, R>
{
	private final String command;
	private final Function<T, C> columnFunction;
	private final Object column;

	protected ScalarFunction(@Nonnull final String command, @Nonnull final String columnName,
		@Nonnull final Function<T, C> columnFunction)
	{
		this.command = command;
		this.column = columnName;
		this.columnFunction = columnFunction;
	}
	
	protected ScalarFunction(@Nonnull final String command, @Nonnull final SQLFunction<T, C> sqlFunction)
	{
		this.command = command;
		this.column = sqlFunction;
		this.columnFunction = sqlFunction;
	}

	@Override
	public String toSQL(final JDBCDriver driver, @Nullable final String tableName)
	{
		final String arg;
		if(column instanceof SQLFunction)
		{
			arg = ((SQLFunction)column).toSQL(driver, tableName);
		}
		else if(tableName != null)
		{
			arg =  tableName + "." + column;
		}
		else
		{
			arg = (String)column;
		}
		return driver.getSQLFunction(command, arg);
	}

	@Override
	public R apply( final T t )
	{
		return applySQLFunction( columnFunction.apply( t ));
	}

	@Override
	public R apply(final Map<String, Object> map)
	{
		if(column instanceof SQLFunction)
		{
			return applySQLFunction( ((SQLFunction<T, C>)column).apply( map));
		}
		return applySQLFunction((C) map.get(column));
	}

	/**
	 * Applies this scalar SQL-function to the given column-value
	 * @param columnValue
	 * @return the result
	 */
	protected abstract R applySQLFunction(@Nullable final C columnValue);

	@Override
	public boolean equals( Object obj )
	{
		if(obj == null || !(obj instanceof SQLFunction))
		{
			return false;
		}
		return equals( (SQLFunction)obj);
	}
}
