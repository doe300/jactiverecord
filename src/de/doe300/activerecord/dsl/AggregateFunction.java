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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
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

/**
 * An aggregate-function to be applied to a list of record
 * @author doe300
 * @param <T>
 * @param <C>
 * @param <R>
 * @since 0.5
 */
public abstract class AggregateFunction<T extends ActiveRecord, C, V extends AggregateFunction.ValueHolder<R>, R> implements Collector<T, V, R>, SQLFunction<T, R>
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
		final String arg = column instanceof SQLFunction ? ((SQLFunction)column).toSQL(driver, tableName) : (String)column;
		return driver.getSQLFunction(command, tableName != null ? tableName + "." + column : arg);
	}

	/**
	 * @param dataMaps
	 * @return the aggregated result
	 */
	@Nullable
	public R aggregate(final Stream<Map<String, Object>> dataMaps)
	{
		return aggregateValues(dataMaps.map((final Map<String, Object> map) -> (C) map.get(column)));
	}

	@Nullable
	protected abstract R aggregateValues(Stream<C> valueStream);

	@Override
	public Supplier<V> supplier()
	{
		return () -> (V)new ValueHolder<R>(null);
	}

	@Override
	public Function<V, R> finisher()
	{
		return (final V h) -> h.value;
	}

	@Override
	public Set<Characteristics> characteristics()
	{
		return new HashSet<>(Arrays.asList( Characteristics.CONCURRENT, Characteristics.UNORDERED));
	}

	@Override
	public R apply( final Map<String, Object> map )
	{
		return aggregate( Stream.of( map));
	}

	@Override
	public R apply( final T t )
	{
		return aggregateValues( Stream.of( columnFunction.apply( t)));
	}

	public static class ValueHolder<T>
	{
		@Nullable
		public T value;

		public ValueHolder( @Nullable final T value )
		{
			this.value = value;
		}
	}

	public static class BiValueHolder<T, U> extends ValueHolder<T>
	{
		@Nullable
		public U secondValue;

		public BiValueHolder(@Nullable final T value, @Nullable final U secondValue)
		{
			super(value);
			this.secondValue = secondValue;
		}
	}

	public static class NullSkippingComparator<T extends Comparable<? super T>> implements Comparator<T>
	{
		private final boolean isNullHigher;

		public NullSkippingComparator( final boolean isNullHigher )
		{
			this.isNullHigher = isNullHigher;
		}

		@Override
		public int compare( final T o1, final T o2 )
		{
			if(o1 == null)
			{
				return isNullHigher ? 1 : -1;
			}
			if(o2 == null)
			{
				return isNullHigher ? -1 : 1;
			}
			return Comparable.class.cast(o1).compareTo(o2);
		}

	}
}
