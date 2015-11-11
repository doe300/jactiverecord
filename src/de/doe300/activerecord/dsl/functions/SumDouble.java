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

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.AggregateFunction.ValueHolder;
import de.doe300.activerecord.dsl.SQLFunction;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import javax.annotation.Nonnull;

/**
 * Calculates the sum of all the <code>non-null</code> values
 * 
 * @author daniel
 *
 * @param <T> the record-type
 * @param <C> the column-type
 * 
 * @since 0.6
 */
public class SumDouble<T extends ActiveRecord, C extends Number> extends AggregateFunction<T, C, ValueHolder<Number>, Number>
{

	/**
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 */
	public SumDouble(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		super(JDBCDriver.AGGREGATE_SUM_DOUBLE, columnName, columnFunc);
	}
	
	public SumDouble(@Nonnull final SQLFunction<T, C> sqlFunction)
	{
		super(JDBCDriver.AGGREGATE_SUM_DOUBLE, sqlFunction);
	}

	@Override
	public BiConsumer<ValueHolder<Number>, T> accumulator()
	{
		return (final ValueHolder<Number> holder, final T record) -> {
			final C colVal = columnFunction.apply(record);
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
	protected Double aggregateValues( final Stream<C> valueStream )
	{
		return valueStream.reduce( 0d, (final Double d, final C c) -> d + c.doubleValue(), (final Double d1, final Double d2) -> d1 + d2);
	}
}