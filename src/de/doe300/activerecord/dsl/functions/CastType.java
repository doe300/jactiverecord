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
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * SQL-function for converting a column-value to another type
 * 
 * NOTE: the available conversions depend on the DBMS used
 * 
 * @author doe300
 * @param <T> the record-type
 * @param <C> the original column-type
 * @param <R> the result-type
 * @since 0.7
 */
public class CastType<T extends ActiveRecord, C, R> extends ScalarFunction<T, C, R>
{
	private final Class<R> resultType;
	private final Function<C, R> converterFunction;
	
	/**
	 * @param columnName the column-name to cast
	 * @param columnFunction the column-function for record-based casting
	 * @param resultType the result-type, MUST be mappable to SQL
	 * @param castFunction the cast-function for record-based casting
	 */
	public CastType(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunction, @Nonnull final Class<R> resultType, @Nonnull final Function<C, R> castFunction)
	{
		super(JDBCDriver.SCALAR_CAST, columnName, columnFunction );
		this.resultType = resultType;
		this.converterFunction = castFunction;
	}
	
	/**
	 * @param sqlFunction the SQL-function to cast
	 * @param resultType the result-type, MUST be mappable to SQL
	 * @param castFunction the cast-function for record-based casting
	 */
	public CastType(@Nonnull final SQLFunction<T, C> sqlFunction, @Nonnull final Class<R> resultType, @Nonnull final Function<C, R> castFunction)
	{
		super(JDBCDriver.SCALAR_CAST, sqlFunction);
		this.resultType = resultType;
		this.converterFunction = castFunction;
	}

	@Override
	protected R applySQLFunction( C columnValue )
	{
		return converterFunction.apply( columnValue );
	}

	@Override
	public String toSQL( JDBCDriver driver, String tableName )
	{
		final String sql = super.toSQL( driver, tableName);
		return sql.replace( "%type%", driver.getSQLType( resultType ));
	}
}
