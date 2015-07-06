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

import de.doe300.activerecord.FinderMethods;
import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.stream.Stream;

/**
 * Base interface for record-based sets
 * @author doe300
 * @param <T>
 */
public interface RecordSet<T extends ActiveRecord> extends SortedSet<T>, FinderMethods<T>
{
	@Override
	public default Stream<T> findAll()
	{
		return stream();
	}

	@Override
	public Stream<T> stream();
	
	/**
	 * @return the RecordBase of the record-type
	 */
	public ReadOnlyRecordBase<T> getRecordBase();
	
	/**
	 * Returns result-set backed by this set for the given Condition
	 * 
	 * NOTE: the returned set may be immutable
	 * 
	 * @param cond
	 * @return a sub-set for the given condition
	 * @see #subSet(java.lang.Object, java.lang.Object) 
	 */
	public RecordSet<T> getForCondition(Condition cond);

	@Override
	public default Comparator<? super T> comparator()
	{
		//uses natural ordering
		return null;
	}

	@Override
	public default SortedSet<T> headSet( T toElement )
	{
		return getForCondition( new SimpleCondition(getRecordBase().getPrimaryColumn(), toElement.getPrimaryKey(), Comparison.SMALLER) );
	}

	@Override
	public default SortedSet<T> tailSet( T fromElement )
	{
		return getForCondition( new SimpleCondition(getRecordBase().getPrimaryColumn(), fromElement.getPrimaryKey(), Comparison.LARGER));
	}

	@Override
	public default SortedSet<T> subSet( T fromElement, T toElement )
	{
		return getForCondition( new AndCondition(
				new SimpleCondition(getRecordBase().getPrimaryColumn(), fromElement.getPrimaryKey(), Comparison.LARGER),
				new SimpleCondition(getRecordBase().getPrimaryColumn(), toElement.getPrimaryKey(), Comparison.SMALLER)
		));
	}
	
	@Override
	public default T first()
	{
		return stream().findFirst().get();
	}

	@Override
	public default T last()
	{
		return stream().sorted( getRecordBase().getDefaultOrder().toRecordComparator().reversed()).findFirst().get();
	}
}
