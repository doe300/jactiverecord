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
package de.doe300.activerecord.dsl.functions;

import de.doe300.activerecord.dsl.SQLFunction;
import de.doe300.activerecord.dsl.ScalarFunction;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Returns the string in lower case
 * 
 * @author daniel
 *
 * @param <T> the record-type
 * @since 0.6
 * @see String#toLowerCase() 
 */
public class LowerCase<T extends ActiveRecord> extends ScalarFunction<T, String, String>
{
	/**
	 * @param columnName the name of the string-type column
	 * @param columnFunc the function to map the record to a string
	 */
	public LowerCase(@Nonnull final String columnName, @Nonnull final Function<T, String> columnFunc)
	{
		super(JDBCDriver.SCALAR_LOWER, columnName, columnFunc);
	}
	
	/**
	 * @param sqlFunction the nested SQL-function
	 */
	public LowerCase(@Nonnull final SQLFunction<T, String> sqlFunction)
	{
		super(JDBCDriver.SCALAR_LOWER, sqlFunction);
	}

	@Override
	protected String applySQLFunction(final String columnValue)
	{
		return columnValue == null ? null : columnValue.toLowerCase();
	}
}