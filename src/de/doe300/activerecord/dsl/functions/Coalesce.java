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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Returns the first non-NULL argument.
 *
 * @author doe300
 * @param <T> the record-type
 * @param <V> the value-type
 * @since 0.9
 * @see Optional#orElse(java.lang.Object) 
 */
public class Coalesce<T extends ActiveRecord, V> extends ScalarFunction<T, V, V>
{
	private final List<SQLFunction<T, V>> columnFunctions;
			
	@SafeVarargs
	public Coalesce(@Nonnull final SQLFunction<T, V> column1, @Nullable final SQLFunction<T, V>... otherColumns)
	{
		super(JDBCDriver.SCALAR_COALESCE, column1);
		columnFunctions = createList( column1, otherColumns);
	}
	
	@SafeVarargs
	static <T> List<T> createList(@Nonnull final T el1, @Nullable final T... els)
	{
		if(els == null || els.length == 0)
		{
			return Collections.singletonList( el1 );
		}
		List<T> list = new ArrayList<>(els.length+1);
		list.add( el1 );
		for(final T t : els)
		{
			list.add( t );
		}
		return list;
	}
	
	@Override
	public V apply( final T t )
	{
		V value = null;
		final Iterator<SQLFunction<T, V>> it = columnFunctions.iterator();
		while(value == null || it.hasNext())
		{
			value = it.next().apply( t);
		}
		return value;
	}

	@Override
	public V apply(final Map<String, Object> map)
	{
		V value = null;
		final Iterator<SQLFunction<T, V>> it = columnFunctions.iterator();
		while(value == null || it.hasNext())
		{
			value = it.next().apply( map);
		}
		return value;
	}

	@Override
	protected V applySQLFunction( V columnValue )
	{
		throw new UnsupportedOperationException( "Not called." );
	}

	@Override
	public String toSQL( JDBCDriver driver, String tableName )
	{
		String sql = JDBCDriver.SCALAR_COALESCE;
		for(int i = 0; i < columnFunctions.size(); i++)
		{
			sql = ScalarFunction.toSQL( driver, tableName, columnFunctions.get( i), sql);
			if(i < columnFunctions.size() - 2)
			{
				sql = sql.replace( "%other%", "%column%, %other%");
			}
			else
			{
				sql = sql.replace( "%other%", "%column%");
			}
		}
		return sql;
	}
}
