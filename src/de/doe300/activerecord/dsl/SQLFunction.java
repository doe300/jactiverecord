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

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Syntax;

/**
 * Base interface for all kinds of SQL built-in and user-defined functions
 * 
 * @author doe300
 * @param <T>
 * @param <R> the return-type of this function
 * @since 0.6
 */
public interface SQLFunction<T extends ActiveRecord, R> extends Function<T, R>
{
	/**
	 * @param driver the driver to be used for vendor-specific commands
	 * @param tableName the name of the table to apply this function to
	 * @return the SQL representation of this function
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String toSQL(@Nonnull final JDBCDriver driver, @Nullable final String tableName);
	
	/**
	 * NOTE: This method is not required to be supported
	 * @param map
	 * @return the return-value for this function
	 * @throws UnsupportedOperationException if the method is not supported
	 */
	public R apply(@Nonnull final Map<String, Object> map);
}
