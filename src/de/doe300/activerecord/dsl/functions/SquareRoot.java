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
 * Calculates the square root
 * 
 * @author daniel
 *
 * @param <T> the record-type
 * @param <C> the column-type
 * @since 0.6
 * @see Math#sqrt(double) 
 */
public class SquareRoot<T extends ActiveRecord, C extends Number> extends ScalarFunction<T, C, Double>
{

	/**
	 * @param columnName
	 *            the name of the number-column
	 * @param columnFunc
	 *            the function returning the column-value
	 */
	public SquareRoot( @Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc )
	{
		super( JDBCDriver.SCALAR_SQRT, columnName, columnFunc );
	}
	
	/**
	 * @param sqlFunction the nested SQL-function
	 */
	public SquareRoot(@Nonnull final SQLFunction<T, C> sqlFunction)
	{
		super(JDBCDriver.SCALAR_SQRT, sqlFunction);
	}

	@Override
	protected Double applySQLFunction( final C columnValue )
	{
		if ( columnValue == null )
		{
			return null;
		}
		return Math.sqrt( columnValue.doubleValue() );
	}
}