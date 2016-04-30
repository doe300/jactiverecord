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
 * Calculates the maximum of all the available <code>non-null</code> values
 * 
 * @author daniel
 *
 * @param <T> the record-type
 * @param <C> the column-type
 * 
 * @since 0.6
 */
public class Maximum<T extends ActiveRecord, C extends Comparable<? super C>> extends AggregateFunction<T, C, MutablePair<C, Void>, C>
{

	/**
	 * @param columnName
	 *            the name of the column to aggregate
	 * @param columnFunc
	 *            the function returning the column-value to aggregate
	 */
	public Maximum(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		super(JDBCDriver.AGGREGATE_MAXIMUM, columnName, columnFunc);
	}
	
	/**
	 * @param sqlFunction the SQL-function to select the maximum of the results
	 */
	public Maximum(@Nonnull final SQLFunction<T, C> sqlFunction)
	{
		super(JDBCDriver.AGGREGATE_MAXIMUM, sqlFunction);
	}

	@Override
	public BiConsumer<MutablePair<C, Void>, T> accumulator()
	{
		return (final MutablePair<C, Void> holder, final T record) -> {
			final C colVal = columnFunction.apply(record);
			if (colVal != null)
			{
				if (holder.hasFirst())
				{
					holder.setFirst( colVal.compareTo( holder.getFirst()) > 0 ? colVal : holder.getFirst());
				}
				else
				{
					holder.setFirst( colVal );
				}
			}
		};
	}

	@Override
	public BinaryOperator<MutablePair<C, Void>> combiner()
	{
		return (final MutablePair<C, Void> h1, final MutablePair<C, Void> h2) -> {
			if (!h1.hasFirst())
			{
				return h2;
			}
			if (!h2.hasFirst())
			{
				return h1;
			}
			return h1.getFirstOrThrow().compareTo(h2.getFirstOrThrow()) > 0 ? h1 : h2;
		};
	}

	@Override
	public Function<MutablePair<C, Void>, C> finisher()
	{
		return MutablePair::getFirst;
	}
	
	@Override
	protected C aggregateValues( final Stream<C> valueStream )
	{
		return valueStream.parallel().max(new NullSkippingComparator<C>(false)).orElse( null);
	}
}