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
package de.doe300.activerecord.dsl.functions;

import de.doe300.activerecord.dsl.SQLFunction;
import de.doe300.activerecord.dsl.ScalarFunction;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Trims a character string
 * @author doe300
 * @param <T>
 * @since 0.7
 * @see String#trim() 
 */
public class TrimString<T extends ActiveRecord> extends ScalarFunction<T, String, String>
{
	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 */
	public TrimString(@Nonnull final String columnName, @Nonnull final Function<T, String> columnFunc)
	{
		super(JDBCDriver.SCALAR_TRIM, columnName, columnFunc);
	}
	
	/**
	 * 
	 * @param sqlFunction the SQL-function
	 */
	public TrimString(@Nonnull final SQLFunction<T, String> sqlFunction)
	{
		super(JDBCDriver.SCALAR_TRIM, sqlFunction);
	}

	@Override
	protected String applySQLFunction( String columnValue )
	{
		return columnValue == null ? null : columnValue.trim();
	}
}
