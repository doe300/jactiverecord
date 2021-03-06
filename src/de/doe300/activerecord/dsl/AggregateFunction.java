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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.util.MutablePair;
import java.util.Collections;
import java.util.Optional;

/**
 * An aggregate-function to be applied to a list of record
 * @author doe300
 * @param <T>
 * @param <C>
 * @param <V>
 * @param <R>
 * @since 0.5
 */
public abstract class AggregateFunction<T extends ActiveRecord, C, V extends MutablePair<?,?>, R> implements Collector<T, V, R>, SQLFunction<T, R>
{
	private final String command;
	protected final Function<T, C> columnFunction;
	private final Object column;

	protected AggregateFunction(@Nonnull final String command, @Nonnull final String columnName,
		@Nonnull final Function<T, C> columnFunction)
	{
		this.command = command;
		this.column = columnName;
		this.columnFunction = columnFunction;
	}

	protected AggregateFunction(@Nonnull final String command, @Nonnull final SQLFunction<T, C> sqlFunction)
	{
		this.command = command;
		this.column = sqlFunction;
		this.columnFunction = sqlFunction;
	}

	@Nonnull
	@Override
	public String toSQL(final JDBCDriver driver, @Nullable final String tableName)
	{
		final String arg;
		if(column instanceof SQLFunction)
		{
			arg = ((SQLFunction<?,?>)column).toSQL(driver, tableName);
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
	public String getAttributeName()
	{
		return column instanceof SQLFunction ? ((SQLFunction)column).getAttributeName() : (String)column;
	}
	
	/**
	 * @param dataMaps
	 * @return the aggregated result
	 */
	@Nullable
	public R aggregate(@Nonnull final Stream<Map<String, Object>> dataMaps)
	{
		if(column instanceof SQLFunction)
		{
			return aggregateValues(dataMaps.map((final Map<String, Object> map) -> Optional.ofNullable(((SQLFunction<T,C>)column).apply( map ))));
		}
		return aggregateValues(dataMaps.peek( (final Map<String, Object> map) -> {
			if(!map.containsKey( column ))
				{
					throw new IllegalArgumentException("No such key: " + column);
				}
		}).map((final Map<String, Object> map) -> Optional.ofNullable((C) map.get(column))));
	}

	/**
	 * Aggregates the values from the given stream
	 * @param valueStream the input stream to aggregate
	 * @return the aggregation result
	 */
	@Nullable
	protected abstract R aggregateValues(@Nonnull final Stream<Optional<C>> valueStream);

	@Override
	public Supplier<V> supplier()
	{
		return () -> (V) new MutablePair<>( null, null);
	}

	@Override
	public Set<Characteristics> characteristics()
	{
		return Collections.singleton( Characteristics.UNORDERED);
	}

	@Override
	public R apply( final Map<String, Object> map )
	{
		return aggregate( Stream.of( map));
	}

	@Override
	public R apply( final T t )
	{
		return aggregateValues( Stream.of( Optional.ofNullable( columnFunction.apply( t))));
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(obj == null || !(obj instanceof SQLFunction))
		{
			return false;
		}
		return equals( (SQLFunction<?,?>)obj);
	}
	
	@Override
	public String toString()
	{
		return toSQL( JDBCDriver.DEFAULT, null );
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
}
