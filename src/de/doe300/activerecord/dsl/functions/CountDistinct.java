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

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.AggregateFunction.BiValueHolder;
import de.doe300.activerecord.dsl.SQLFunction;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import javax.annotation.Nonnull;

/**
 * Calculates the number of all distinct <code>non-null</code> column-values
 * 
 * @author daniel
 *
 * @param <T> the record-type
 * @param <C> the column-type
 * 
 * @since 0.6
 */
public class CountDistinct<T extends ActiveRecord, C> extends AggregateFunction<T, C, BiValueHolder<Number, Set<C>>, Number>
{
	/**
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 */
	public CountDistinct(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		super(JDBCDriver.AGGREGATE_COUNT_DISTINCT, columnName, columnFunc);
	}
	
	/**
	 * @param sqlFunction
	 *            the function returning the values to be aggregated
	 */
	public CountDistinct(@Nonnull final SQLFunction<T, C> sqlFunction)
	{
		super(JDBCDriver.AGGREGATE_COUNT_DISTINCT, sqlFunction);
	}

	@Override
	public Supplier<BiValueHolder<Number, Set<C>>> supplier()
	{
		return () -> new AggregateFunction.BiValueHolder<Number, Set<C>>(null, null);
	}

	@Override
	public BiConsumer<BiValueHolder<Number, Set<C>>, T> accumulator()
	{
		return (final BiValueHolder<Number, Set<C>> holder, final T record) ->
		{
			final C colVal = columnFunction.apply( record );
			if(colVal != null)
			{
				if(holder.value == null)
				{
					holder.value = 1L;
					holder.secondValue = new HashSet<>(10);
					holder.secondValue.add( colVal );
				}
				else
				{
					holder.secondValue.add( colVal );
					holder.value = (long)holder.secondValue.size();
					holder.secondValue.add( colVal );
				}
			}
		};
	}

	@Override
	public BinaryOperator<BiValueHolder<Number, Set<C>>> combiner()
	{
		return (final BiValueHolder<Number, Set<C>> h1, final BiValueHolder<Number, Set<C>> h2) ->
		{
			if(h1.value == null)
			{
				return h2;
			}
			if(h2.value == null)
			{
				return h1;
			}
			h1.secondValue.addAll(h2.secondValue);
			h1.value = (long)h1.secondValue.size();
			return h1;
		};
	}

	@Override
	protected Long aggregateValues( final Stream<C> valueStream )
	{
		return valueStream.distinct().count();
	}
}