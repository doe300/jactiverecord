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
package de.doe300.activerecord.dsl.functions;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.SQLFunction;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.util.MutablePair;
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
public class CountDistinct<T extends ActiveRecord, C> extends AggregateFunction<T, C, MutablePair<Number, Set<C>>, Number>
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
	public BiConsumer<MutablePair<Number, Set<C>>, T> accumulator()
	{
		return (final MutablePair<Number, Set<C>> holder, final T record) ->
		{
			final C colVal = columnFunction.apply( record );
			if(colVal != null)
			{
				if(!holder.hasFirst())
				{
					holder.setFirst( 1L);
					holder.setSecond( new HashSet<>(10));
					holder.getSecondOrThrow().add( colVal);
				}
				else
				{
					holder.getSecondOrThrow().add( colVal);
					holder.setFirst( holder.getSecondOrThrow().size());
				}
			}
		};
	}

	@Override
	public BinaryOperator<MutablePair<Number, Set<C>>> combiner()
	{
		return (final MutablePair<Number, Set<C>> h1, final MutablePair<Number, Set<C>> h2) ->
		{
			if(!h1.hasFirst())
			{
				return h2;
			}
			if(!h2.hasFirst())
			{
				return h1;
			}
			h1.getSecondOrThrow().addAll( h2.getSecondOrThrow());
			h1.setFirst( h1.getSecondOrThrow().size());
			return h1;
		};
	}

	@Override
	public Function<MutablePair<Number, Set<C>>, Number> finisher()
	{
		return MutablePair::getFirst;
	}

	@Override
	protected Long aggregateValues( final Stream<C> valueStream )
	{
		return valueStream.parallel().distinct().count();
	}
}