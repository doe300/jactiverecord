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
package de.doe300.activerecord;

import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.functions.Average;
import de.doe300.activerecord.dsl.functions.CountDistinct;
import de.doe300.activerecord.dsl.functions.CountNotNull;
import de.doe300.activerecord.dsl.functions.Maximum;
import de.doe300.activerecord.dsl.functions.Minimum;
import de.doe300.activerecord.dsl.functions.Sum;
import de.doe300.activerecord.dsl.functions.SumDouble;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.function.Function;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Signed;

/**
 * Interface for common aggregate functions to be executed on a set of record
 * 
 * @author doe300
 * @param <T>
 * @since 0.4
 * @see AggregateFunction
 */
public interface AggregateMethods<T extends ActiveRecord> extends FinderMethods<T>
{
	/**
	 * Aggregates all records with the given function
	 * 
	 * @param <C> the type of the column
	 * @param <R> the type of the result
	 * @param aggregateFunction the aggregate-function to be applied on the column-values
	 * @param condition an optional condition
	 * @return the result
	 */
	@Nullable
	public <C, R> R aggregate(@Nonnull final AggregateFunction<T, C, ?, R> aggregateFunction, @Nullable final Condition condition);
	
	/**
	 * @param <C> the column-type
	 * @param columnName the name of the column to aggregate
	 * @param columnFunc the function mapping the record to its column-value
	 * @return the minimum value
	 * @see #aggregate(AggregateFunction, Condition)
	 */
	@Nullable
	public default <C extends Comparable<? super C>> C minimum(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return aggregate( new Minimum<>(columnName, columnFunc), null);
	}
	
	/**
	 * 
	 * @param <C> the type of the column-value
	 * @param columnName the name of the column to get the minimum from
	 * @param columnFunc the function mapping the record to its column-value
	 * @return the maximum value
	 */
	@Nullable
	public default <C extends Comparable<? super C>> C maximum(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return aggregate( new Maximum<>(columnName, columnFunc), null);
	}
	
	/**
	 * NOTE: this method counts the number of records, with a column-value which is not <code>null</code>
	 * @param <C> the type of the column-value
	 * @param columnName the name of the column to count the <code>non-null</code> values
	 * @param columnFunc the function mapping the record to its column-value
	 * @return the count
	 */
	@Nonnegative
	public default <C> long count(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return aggregate( new CountNotNull<>(columnName, columnFunc), null).longValue();
	}
	
	/**
	 * Counts all records which column-values are distinct values
	 * 
	 * @param <C> the type of the column-value
	 * @param columnName the name of the column to count the distinct values
	 * @param columnFunc the function mapping the record to its column-value
	 * @return the count of records with the given condition
	 */
	@Nonnegative
	public default <C> long countDistinct(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return aggregate( new CountDistinct<>(columnName, columnFunc), null).longValue();
	}
	
	/**
	 * Calculates the sum of the column-values by casting all single column-values to long
	 * 
	 * @param <C> the type of the column-value
	 * @param columnName the name of the column to sum the values
	 * @param columnFunc the function mapping the record to its column-value
	 * @return the sum of all the column-values
	 */
	@Signed
	public default <C extends Number> long sum(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return aggregate( new Sum<>(columnName, columnFunc), null).longValue();
	}
	
	/**
	 * Calculates the sum of the column-values by casting all single column-values to double
	 * 
	 * @param <C> the type of the column-value
	 * @param columnName the name of the column to sum its values
	 * @param columnFunc the function mapping the record to its column-value
	 * @return the sum of all the column-values
	 */
	@Signed
	public default <C extends Number> double sumFloating(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return aggregate( new SumDouble<>(columnName, columnFunc), null).doubleValue();
	}
	
	/**
	 * Calculates the average column-value
	 * @param <C> the type of the column-value
	 * @param columnName the name of the column to retrieve the average value
	 * @param columnFunc the function mapping the record to its column-value
	 * @return the average column-value
	 */
	@Signed
	public default <C extends Number> double average(@Nonnull final String columnName, @Nonnull final Function<T, C> columnFunc)
	{
		return aggregate( new Average<>(columnName, columnFunc), null).doubleValue();
	}
}
