/*
 * The MIT License (MIT)
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
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.doe300.activerecord.record.association;

import de.doe300.activerecord.AggregateMethods;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.FinderMethods;
import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.ActiveRecord;

/**
 * Base interface for record-based sets
 * @author doe300
 * @param <T>
 */
public interface RecordSet<T extends ActiveRecord> extends SortedSet<T>, FinderMethods<T>, AggregateMethods<T>
{
	@Override
	@Nonnull
	public default Stream<T> findAll()
	{
		return stream();
	}

	@Override
	@Nonnull
	public Stream<T> stream();

	/**
	 * @return the RecordBase of the record-type
	 */
	@Nonnull
	public ReadOnlyRecordBase<T> getRecordBase();
	
	/**
	 * @return the order of this set
	 * @since 0.7
	 */
	@Nonnull
	public Order getOrder();

	/**
	 * Returns result-set backed by this set for the given Condition
	 *
	 * NOTE: the returned set may be immutable
	 *
	 * @param cond
	 * @return a sub-set for the given condition
	 * @see #subSet(java.lang.Object, java.lang.Object)
	 */
	@Nonnull
	public default RecordSet<T> getForCondition(@Nullable final Condition cond)
	{
		return getForCondition( cond, getOrder());
	}
	
	/**
	 * Returns result-set backed by this set for the given Condition and Order
	 *
	 * NOTE: the returned set may be immutable
	 *
	 * @param cond
	 * @param order
	 * @return a sub-set for the given condition
	 * @since 0.7
	 */
	@Nonnull
	public RecordSet<T> getForCondition(@Nullable final Condition cond, @Nullable final Order order);

	@Override
	@Nullable
	public default Comparator<? super T> comparator()
	{
		//uses natural ordering
		return null;
	}

	@Override
	@Nonnull
	public default RecordSet<T> headSet(final T toElement)
	{
		return getForCondition( new SimpleCondition(getRecordBase().getPrimaryColumn(), toElement.getPrimaryKey(), Comparison.SMALLER), getOrder() );
	}

	@Override
	@Nonnull
	public default RecordSet<T> tailSet(final T fromElement)
	{
		return getForCondition( new SimpleCondition(getRecordBase().getPrimaryColumn(), fromElement.getPrimaryKey(), Comparison.LARGER), getOrder());
	}

	@Override
	@Nonnull
	public default RecordSet<T> subSet(final T fromElement, final T toElement)
	{
		return getForCondition( AndCondition.andConditions(
			new SimpleCondition(getRecordBase().getPrimaryColumn(), fromElement.getPrimaryKey(), Comparison.LARGER_EQUALS),
			new SimpleCondition(getRecordBase().getPrimaryColumn(), toElement.getPrimaryKey(), Comparison.SMALLER)
			), getOrder());
	}

	@Override
	@Nonnull
	public default T first()
	{
		return stream().sorted( getOrder().toRecordComparator()).findFirst().get();
	}

	@Override
	@Nonnull
	public default T last()
	{
		return stream().sorted( getOrder().toRecordComparator().reversed()).findFirst().get();
	}
	
	/**
	 * NOTE: the cached-version is read-only
	 * 
	 * @return a cached version of this record-set
	 * @since 0.7
	 */
	@Nonnull
	public default RecordSet<T> cached()
	{
		return new CachedRecordSet<T>(this );
	}
}
