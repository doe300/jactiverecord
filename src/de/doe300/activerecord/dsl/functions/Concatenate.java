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

package de.doe300.activerecord.dsl.functions;

import de.doe300.activerecord.dsl.SQLFunction;
import de.doe300.activerecord.dsl.ScalarFunction;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Scalar function for concatenating character strings.
 * 
 * "If at least one operand is NULL, then the result is NULL."
 * 
 * @author doe300
 * @param <T> the record-type
 * @since 0.9
 * @see "http://troels.arvin.dk/db/rdbms/#functions-concat"
 */
public class Concatenate<T extends ActiveRecord> extends ScalarFunction<T, String, String>
{
	private final SQLFunction<T, String> column2;
	
	public Concatenate(@Nonnull final SQLFunction<T, String> column1, @Nonnull final SQLFunction<T, String> column2)
	{
		super(JDBCDriver.SCALAR_CONCATENATE, column1);
		this.column2 = column2;
	}
	
	@Override
	public String apply( final T t )
	{
		final String s1 = super.apply( t);
		if(s1 == null)
		{
			return null;
		}
		final String s2 = column2.apply( t);
		if(s2 == null)
		{
			return null;
		}
		return s1 + s2;
	}

	@Override
	public String apply(final Map<String, Object> map)
	{
		final String s1 = super.apply( map);
		if(s1 == null)
		{
			return null;
		}
		final String s2 = column2.apply( map);
		if(s2 == null)
		{
			return null;
		}
		return s1 + s2;
	}

	@Override
	protected String applySQLFunction( String columnValue )
	{
		return columnValue;
	}

	@Override
	public String toSQL( JDBCDriver driver, String tableName )
	{
		String sql = super.toSQL( driver, tableName );
		sql = sql.replace( "%other%", "%column%");
		return ScalarFunction.toSQL( driver, tableName, column2, sql );
	}
}
