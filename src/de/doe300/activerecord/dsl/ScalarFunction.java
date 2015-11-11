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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUC WARRANTY OF ANY KIND, EXPRESS OR
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
	private final String columnName;

	protected ScalarFunction(@Nonnull final String command, @Nonnull final String columnName,
		@Nonnull final Function<T, C> columnFunction)
	{
		this.command = command;
		this.columnName = columnName;
		this.columnFunction = columnFunction;
	}


	@Override
	public String toSQL(final JDBCDriver driver, @Nullable final String tableName)
	{
		return driver.getSQLFunction(command, tableName != null ? tableName + "." + columnName : columnName);
	}

	@Override
	public R apply( final T t )
	{
		return applySQLFunction( columnFunction.apply( t ));
	}

	@Override
	public R apply(final Map<String, Object> map)
	{
		return applySQLFunction((C) map.get(columnName));
	}

	protected abstract R applySQLFunction(@Nullable final C columnValue);

	////
	// String Functions
	////

	/**
	 *
	 * @param columnName the name of the string-type column
	 * @param columnFunc the function to map the record to a string
	 * @return the string in lower case
	 * @see String#toLowerCase()
	 */
	public static final <T extends ActiveRecord> ScalarFunction<T, String, String> LOWER(@Nonnull final String columnName, @Nonnull final Function<T, String> columnFunc)
	{
		return new ScalarFunction<T, String, String>(JDBCDriver.SCALAR_LOWER, columnName, columnFunc)
		{

			@Override
			protected String applySQLFunction(final String columnValue)
			{
				return columnValue == null ? null : columnValue.toLowerCase();
			}
		};
	}

	/**
	 *
	 * @param columnName the name of the string-type column
	 * @param columnFunc the function to map the record to a string
	 * @return the string in upper case
	 * @see String#toUpperCase()
	 */
	public static final <T extends ActiveRecord> ScalarFunction<T, String, String> UPPER(@Nonnull final String columnName, @Nonnull final Function<T, String> columnFunc)
	{
		return new ScalarFunction<T, String, String>(JDBCDriver.SCALAR_UPPER, columnName, columnFunc)
		{

			@Override
			protected String applySQLFunction(final String columnValue)
			{
				return columnValue == null ? null : columnValue.toUpperCase();
			}
		};
	}

	////
	// Numeric Functions
	////

	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 * @return the absolute numeric value
	 * @see Math#abs(long)
	 */
	public static final <T extends ActiveRecord, C extends Number> ScalarFunction<T, C, Long> ABS(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new ScalarFunction<T, C, Long>(JDBCDriver.SCALAR_ABS, columnName, columnFunc)
		{
			@Override
			protected Long applySQLFunction(final C columnValue)
			{
				if (columnValue == null)
				{
					return null;
				}
				return Long.valueOf(Math.abs(columnValue.longValue()));
			}
		};
	}

	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 * @return the absolute numeric value
	 * @see Math#abs(double)
	 */
	public static final <T extends ActiveRecord, C extends Number> ScalarFunction<T, C, Double> ABS_FLOATING(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new ScalarFunction<T, C, Double>(JDBCDriver.SCALAR_ABS_DOUBLE, columnName, columnFunc)
		{
			@Override
			protected Double applySQLFunction(final C columnValue)
			{
				if (columnValue == null)
				{
					return null;
				}
				return Double.valueOf(Math.abs(columnValue.doubleValue()));
			}
		};
	}

	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 * @return the signum (1, 0 or -1)
	 * @see Math#signum(double)
	 */
	public static final <T extends ActiveRecord, C extends Number> ScalarFunction<T, C, Integer> SIGN(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new ScalarFunction<T, C, Integer>(JDBCDriver.SCALAR_SIGN, columnName, columnFunc)
		{
			@Override
			protected Integer applySQLFunction(final C columnValue)
			{
				if (columnValue == null)
				{
					return null;
				}
				return (int) Math.signum(columnValue.doubleValue());
			}
		};
	}

	// TODO modulo

	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 * @return the rounded-down integer value
	 * @see Math#floor(double)
	 */
	public static final <T extends ActiveRecord, C extends Number> ScalarFunction<T, C, Long> FLOOR(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new ScalarFunction<T, C, Long>(JDBCDriver.SCALAR_FLOOR, columnName, columnFunc)
		{
			@Override
			protected Long applySQLFunction(final C columnValue)
			{
				if (columnValue == null)
				{
					return null;
				}
				return (long) Math.floor(columnValue.doubleValue());
			}
		};
	}

	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 * @return the rounded-up integer value
	 * @see Math#ceil(double)
	 */
	public static final <T extends ActiveRecord, C extends Number> ScalarFunction<T, C, Long> CEILING(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new ScalarFunction<T, C, Long>(JDBCDriver.SCALAR_CEILING, columnName, columnFunc)
		{
			@Override
			protected Long applySQLFunction(final C columnValue)
			{
				if (columnValue == null)
				{
					return null;
				}
				return (long) Math.ceil(columnValue.doubleValue());
			}
		};
	}

	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 * @return the rounded integer value
	 * @see Math#round(double)
	 */
	public static final <T extends ActiveRecord, C extends Number> ScalarFunction<T, C, Long> ROUND(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new ScalarFunction<T, C, Long>(JDBCDriver.SCALAR_ROUND, columnName, columnFunc)
		{
			@Override
			protected Long applySQLFunction(final C columnValue)
			{
				if (columnValue == null)
				{
					return null;
				}
				return Math.round(columnValue.doubleValue());
			}
		};
	}

	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 * @return the square root
	 * @see Math#sqrt(double)
	 */
	public static final <T extends ActiveRecord, C extends Number> ScalarFunction<T, C, Double> SQUARE_ROOT(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new ScalarFunction<T, C, Double>(JDBCDriver.SCALAR_SQRT, columnName, columnFunc)
		{
			@Override
			protected Double applySQLFunction(final C columnValue)
			{
				if (columnValue == null)
				{
					return null;
				}
				return Math.sqrt(columnValue.doubleValue());
			}
		};
	}
}
