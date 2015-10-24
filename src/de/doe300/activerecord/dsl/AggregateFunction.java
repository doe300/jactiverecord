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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Syntax;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * An aggregate-function to be applied to a list of record
 * @author doe300
 * @param <T>
 * @param <C>
 * @param <R>
 * @since 0.4
 */
public abstract class AggregateFunction<T extends ActiveRecord, C, R> implements Collector<T, AggregateFunction.ValueHolder<R>, R>
{
	protected final String command;
	protected final Function<T, C> columnFunction;
	private final String columnName;

	protected AggregateFunction(@Nonnull final String command, @Nonnull final String columnName,
		@Nonnull final Function<T, C> columnFunction)
	{
		this.command = command;
		this.columnName = columnName;
		this.columnFunction = columnFunction;
	}

	/**
	 * @param driver the driver to be used for vendor-specific commands
	 * @return the SQL representation of this statement
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String toSQL(@Nonnull final JDBCDriver driver)
	{
		return driver.getAggregateFunction(command, columnName);
	}
	
	@Nullable
	public R aggregate(Stream<Map<String, Object>> dataMaps)
	{
		return aggregateValues( dataMaps.map( (Map<String, Object> map) -> (C)map.get( columnName)));
	}
	
	@Nullable
	protected abstract R aggregateValues(Stream<C> valueStream);

	@Override
	public Supplier<ValueHolder<R>> supplier()
	{
		return () -> new ValueHolder<R>(null);
	}

	@Override
	public Function<ValueHolder<R>, R> finisher()
	{
		return (final ValueHolder<R> h) -> h.value;
	}

	@Override
	public Set<Characteristics> characteristics()
	{
		return new HashSet<>(Arrays.asList( Characteristics.CONCURRENT, Characteristics.UNORDERED));
	}

	/**
	 * @param <T> the record-type
	 * @param <C> the column-type
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 * @return the minimum of all the available <code>non-null</code> values
	 */
	@Nonnull
	public static final <T extends ActiveRecord, C extends Comparable<? super C>> AggregateFunction<T, C, C> MINIMUM(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new AggregateFunction<T, C, C>(JDBCDriver.AGGREGATE_MINIMUM, columnName, columnFunc)
		{
			@Override
			public BiConsumer<ValueHolder<C>, T> accumulator()
			{
				return (final ValueHolder<C> holder, final T record) ->
				{
					final C colVal = columnFunc.apply( record );
					if (colVal != null)
					{
						if (holder.value != null)
						{
							holder.value = colVal.compareTo(holder.value) < 0 ? colVal : holder.value;
						}
						else
						{
							holder.value = colVal;
						}
					}
				};
			}

			@Override
			public BinaryOperator<ValueHolder<C>> combiner()
			{
				return (final ValueHolder<C> h1, final ValueHolder<C> h2) ->
				{
					if (h1.value == null)
					{
						return h2;
					}
					if (h2.value == null)
					{
						return h1;
					}
					return h1.value.compareTo(h2.value) < 0 ? h1 : h2;
				};
			}

			@Override
			protected C aggregateValues( Stream<C> valueStream )
			{
				return valueStream.min( new NullSkippingComparator<C>(true ) ).orElse( null);
			}
		};
	}

	/**
	 * @param <T> the record-type
	 * @param <C> the column-type
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 * @return the maximum of all the available <code>non-null</code> values
	 */
	@Nonnull
	public static final <T extends ActiveRecord, C extends Comparable<? super C>> AggregateFunction<T, C, C> MAXIMUM(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new AggregateFunction<T, C, C>(JDBCDriver.AGGREGATE_MAXIMUM, columnName, columnFunc)
		{
			@Override
			public BiConsumer<ValueHolder<C>, T> accumulator()
			{
				return (final ValueHolder<C> holder, final T record) -> {
					final C colVal = columnFunc.apply(record);
					if (colVal != null)
					{
						if (holder.value != null)
						{
							holder.value = colVal.compareTo(holder.value) > 0 ? colVal : holder.value;
						}
						else
						{
							holder.value = colVal;
						}
					}
				};
			}

			@Override
			public BinaryOperator<ValueHolder<C>> combiner()
			{
				return (final ValueHolder<C> h1, final ValueHolder<C> h2) -> {
					if (h1.value == null)
					{
						return h2;
					}
					if (h2.value == null)
					{
						return h1;
					}
					return h1.value.compareTo(h2.value) > 0 ? h1 : h2;
				};
			}

			@Override
			protected C aggregateValues( Stream<C> valueStream )
			{
				return valueStream.max( new NullSkippingComparator<C>(false)).orElse( null);
			}
		};
	}

	/**
	 * @param <T> the record-type
	 * @param <C> the column-type
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 * @return the number of all <code>non-null</code> column-values
	 */
	@Nonnull
	public static final <T extends ActiveRecord, C> AggregateFunction<T, C, Number> COUNT(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new AggregateFunction<T, C, Number>(JDBCDriver.AGGREGATE_COUNT_NOT_NULL, columnName, columnFunc)
		{
			@Override
			public BiConsumer<ValueHolder<Number>, T> accumulator()
			{
				return (final ValueHolder<Number> holder, final T record) ->
				{
					final C colVal = columnFunc.apply( record );
					if(holder.value == null)
					{
						holder.value = colVal != null ? 1l : 0l;
					}
					else
					{
						holder.value = holder.value.longValue() + (colVal != null ? 1l : 0l);
					}
				};
			}

			@Override
			public BinaryOperator<ValueHolder<Number>> combiner()
			{
				return (final ValueHolder<Number> h1, final ValueHolder<Number> h2) ->
				{
					if(h1.value == null)
					{
						return h2;
					}
					if(h2.value == null)
					{
						return h1;
					}
					h1.value = h1.value.longValue() + h2.value.longValue();
					return h1;
				};
			}

			@Override
			protected Long aggregateValues( Stream<C> valueStream )
			{
				return valueStream.count();
			}
		};
	}

	/**
	 * @param <T> the record-type
	 * @param <C> the column-type
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 * @return the number of all distinct <code>non-null</code> column-values
	 */
	@Nonnull
	public static final <T extends ActiveRecord, C> AggregateFunction<T, C, Number> COUNT_DISTINCT(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new AggregateFunction<T, C, Number>(JDBCDriver.AGGREGATE_COUNT_DISTINCT, columnName, columnFunc)
		{
			@Override
			public Supplier<ValueHolder<Number>> supplier()
			{
				return () -> new BiValueHolder<Number, Set<C>>(null, null);
			}
			
			@Override
			public BiConsumer<ValueHolder<Number>, T> accumulator()
			{
				return (ValueHolder<Number> holder, T record) -> 
				{
					C colVal = columnFunc.apply( record );
					if(colVal != null)
					{
						if(holder.value == null)
						{
							holder.value = 1L;
							((BiValueHolder<Number, Set<C>>)holder).secondValue = new HashSet<>();
							((BiValueHolder<Number, Set<C>>)holder).secondValue.add( colVal );
						}
						else
						{
							((BiValueHolder<Number, Set<C>>)holder).secondValue.add( colVal );
							holder.value = (long)((BiValueHolder<Number, Set<C>>)holder).secondValue.size();
							((BiValueHolder<Number, Set<C>>)holder).secondValue.add( colVal );
						}
					}
				};
			}
			
			@Override
			public BinaryOperator<ValueHolder<Number>> combiner()
			{
				return (ValueHolder<Number> h1, ValueHolder<Number> h2) -> 
				{
					if(h1.value == null)
					{
						return h2;
					}
					if(h2.value == null)
					{
						return h1;
					}
					((BiValueHolder<Number, Set<C>>)h1).secondValue.addAll(((BiValueHolder<Number, Set<C>>)h2).secondValue);
					h1.value = (long)((BiValueHolder<Number, Set<C>>)h1).secondValue.size();
					return h1;
				};
			}

			@Override
			protected Long aggregateValues( Stream<C> valueStream )
			{
				return valueStream.distinct().count();
			}
		};
	}
	

	/**
	 * @param <T> the record-type
	 * @param <C> the column-type
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 * @return the sum of all the <code>non-null</code> values
	 */
	@Nonnull
	public static final <T extends ActiveRecord, C extends Number> AggregateFunction<T, C, Number> SUM(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new AggregateFunction<T, C, Number>(JDBCDriver.AGGREGATE_SUM, columnName, columnFunc)
		{
			@Override
			public BiConsumer<ValueHolder<Number>, T> accumulator()
			{
				return (final ValueHolder<Number> holder, final T record) -> {
					final C colVal = columnFunc.apply(record);
					if (colVal != null)
					{
						if (holder.value != null)
						{
							holder.value = holder.value.longValue() + colVal.longValue();
						}
						else
						{
							holder.value = colVal.longValue();
						}
					}
				};
			}

			@Override
			public BinaryOperator<ValueHolder<Number>> combiner()
			{
				return (final ValueHolder<Number> h1, final ValueHolder<Number> h2) -> {
					if (h1.value == null)
					{
						return h2;
					}
					if (h2.value == null)
					{
						return h1;
					}
					h1.value = h1.value.longValue() + h2.value.longValue();
					return h1;
				};
			}

			@Override
			protected Long aggregateValues( Stream<C> valueStream )
			{
				return valueStream.reduce( 0L, (Long c1, C c2) -> c1.longValue() + c2.longValue(), (Long l1, Long l2) -> l1 + l2);
			}
		};
	}

	/**
	 * @param <T> the record-type
	 * @param <C> the column-type
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 * @return the sum of all the <code>non-null</code> values
	 */
	//XXX combine sums? need to compute result in some all-matching data-type (BigDecimal?)
	public static final <T extends ActiveRecord, C extends Number> AggregateFunction<T, C, Number> SUM_FLOATING(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new AggregateFunction<T, C, Number>(JDBCDriver.AGGREGATE_SUM_DOUBLE, columnName, columnFunc)
		{
			@Override
			public BiConsumer<ValueHolder<Number>, T> accumulator()
			{
				return (final ValueHolder<Number> holder, final T record) -> {
					final C colVal = columnFunc.apply(record);
					if (colVal != null)
					{
						if (holder.value != null)
						{
							holder.value = holder.value.doubleValue() + colVal.doubleValue();
						}
						else
						{
							holder.value = colVal.doubleValue();
						}
					}
				};
			}

			@Override
			public BinaryOperator<ValueHolder<Number>> combiner()
			{
				return (final ValueHolder<Number> h1, final ValueHolder<Number> h2) -> {
					if (h1.value == null)
					{
						return h2;
					}
					if (h2.value == null)
					{
						return h1;
					}
					h1.value = h1.value.doubleValue() + h2.value.doubleValue();
					return h1;
				};
			}

			@Override
			protected Double aggregateValues( Stream<C> valueStream )
			{
				return valueStream.reduce( 0d, (Double d, C c) -> d + c.doubleValue(), (Double d1, Double d2) -> d1 + d2);
			}
		};
	}

	/**
	 * @param <T> the record-type
	 * @param <C> the column-type
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 * @return the average of all the <code>non-null</code> values
	 */
	public static final <T extends ActiveRecord, C extends Number> AggregateFunction<T, C, Number> AVERAGE(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return new AggregateFunction<T, C, Number>(JDBCDriver.AGGREGATE_AVERAGE, columnName, columnFunc)
		{
			@Override
			public Supplier<ValueHolder<Number>> supplier()
			{
				return () -> new BiValueHolder<Number, Long>(null, null);
			}

			@Override
			public BiConsumer<ValueHolder<Number>, T> accumulator()
			{
				return (final ValueHolder<Number> holder, final T record) -> {
					final C colVal = columnFunc.apply(record);
					if (colVal != null)
					{
						if (holder.value != null)
						{
							holder.value = holder.value.longValue() + colVal.doubleValue();
							((BiValueHolder<Number, Long>) holder).secondValue += 1L;
						}
						else
						{
							holder.value = colVal.doubleValue();
							((BiValueHolder<Number, Long>) holder).secondValue = 1L;
						}
					}
				};
			}

			@Override
			public BinaryOperator<ValueHolder<Number>> combiner()
			{
				return (final ValueHolder<Number> h1, final ValueHolder<Number> h2) -> {
					if (h1.value == null)
					{
						return h2;
					}
					if (h2.value == null)
					{
						return h1;
					}
					h1.value = h1.value.doubleValue() + h2.value.doubleValue();
					((BiValueHolder<Number, Long>) h1).secondValue += ((BiValueHolder<Number, Long>) h2).secondValue;
					return h1;
				};
			}

			@Override
			public Function<ValueHolder<Number>, Number> finisher()
			{
				return (final ValueHolder<Number> holder) -> holder.value.doubleValue() / ((BiValueHolder<Number, Long>) holder).secondValue;
			}

			@Override
			protected Double aggregateValues( Stream<C> valueStream )
			{
				return valueStream.map( (C c) -> new BiValueHolder<Double, Long>(c.doubleValue(), 1L)).
						reduce( (BiValueHolder<Double, Long> b1, BiValueHolder<Double, Long> b2) -> 
						{
							return new BiValueHolder<>(b1.value+b2.value, b1.secondValue + b2.secondValue);
						}).map( (BiValueHolder<Double, Long> b) -> b.value / b.secondValue).orElse( null);
			}
		};

	}

	static class ValueHolder<T>
	{
		@Nullable
		T value;

		ValueHolder( @Nullable final T value )
		{
			this.value = value;
		}
	}

	static class BiValueHolder<T, U> extends ValueHolder<T>
	{
		@Nullable
		U secondValue;

		BiValueHolder(@Nullable final T value, @Nullable final U secondValue)
		{
			super(value);
			this.secondValue = secondValue;
		}
	}
	
	static class NullSkippingComparator<T extends Comparable<? super T>> implements Comparator<T>
	{
		private final boolean isNullHigher;

		NullSkippingComparator( boolean isNullHigher )
		{
			this.isNullHigher = isNullHigher;
		}
		
		@Override
		public int compare( T o1, T o2 )
		{
			if(o1 == null)
			{
				return isNullHigher ? 1 : -1;
			}
			if(o2 == null)
			{
				return isNullHigher ? -1 : 1;
			}
			return  ((Comparable<T>)o1).compareTo( o2 );
		}
		
	}
}
