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

import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.stream.Stream;

/**
 * A RecordSet containing all records in a table
 * 
 * NOTE: this set is unmodifiable
 * @author doe300
 * @param <T>
 */
public class TableSet<T extends ActiveRecord> extends AbstractSet<T> implements RecordSet<T>
{
	private final RecordBase<T> base;
	private final int fromKey, toKey;

	/**
	 * Standard constructor for creating a set containing all records in the {@link RecordBase}
	 * @param base 
	 */
	public TableSet( RecordBase<T> base )
	{
		this.base = base;
		fromKey = -1;
		toKey = Integer.MAX_VALUE;
	}
	
	/**
	 * This constructor creates a set containing all records with their primary-key greater than <code>fromKey</code>
	 * and smaller than <code>toKey</code>
	 * @param base
	 * @param fromKey the minimum key, excluded
	 * @param toKey the maximum key, excluded
	 */
	public TableSet(RecordBase<T> base, int fromKey, int toKey)
	{
		this.base = base;
		this.fromKey = fromKey;
		this.toKey = toKey;
	}
	
	private Condition createRangeCondition(Condition additionalCond)
	{
		if(fromKey < 0 && toKey == Integer.MAX_VALUE)
		{
			if(additionalCond == null)
			{
				return new SimpleCondition(base.getPrimaryColumn(), true, Comparison.TRUE);
			}
			return additionalCond;
		}
		if(fromKey < 0)
		{
			return new AndCondition(
					new SimpleCondition(base.getPrimaryColumn(), toKey, Comparison.SMALLER ),
					additionalCond
			);
		}
		if(toKey == Integer.MAX_VALUE)
		{
			return new AndCondition(
					new SimpleCondition(base.getPrimaryColumn(), fromKey, Comparison.LARGER),
					additionalCond
			);
		}
		return new AndCondition(
				new SimpleCondition(base.getPrimaryColumn(), fromKey, Comparison.LARGER),
				new SimpleCondition(base.getPrimaryColumn(), toKey, Comparison.SMALLER ),
				additionalCond
		);
	}
	
	@Override
	public ReadOnlyRecordBase<T> getRecordBase()
	{
		return base;
	}

	@Override
	public SortedSet<T> subSet( T fromElement, T toElement )
	{
		return new TableSet<T>(base, fromElement.getPrimaryKey(), toElement.getPrimaryKey() );
	}

	@Override
	public SortedSet<T> headSet( T toElement )
	{
		return new TableSet<T>(base, -1, toElement.getPrimaryKey());
	}

	@Override
	public SortedSet<T> tailSet( T fromElement )
	{
		return new TableSet<T>(base, fromElement.getPrimaryKey(), Integer.MAX_VALUE);
	}
	
	@Override
	public Stream<T> stream()
	{
		return base.find( createRangeCondition( null));
	}

	@Override
	public int size()
	{
		return base.count( createRangeCondition( null ));
	}

	@Override
	public boolean contains( Object o )
	{
		return base.getRecordType().isInstance( o ) && base.hasRecord( ((ActiveRecord)o).getPrimaryKey() ) && createRangeCondition( null ).test( ((ActiveRecord)o));
	}

	@Override
	public Iterator<T> iterator()
	{
		return stream().iterator();
	}

	@Override
	public boolean add( T e )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean remove( Object o )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean addAll(Collection<? extends T> c )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean retainAll(Collection<?> c )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean removeAll(Collection<?> c )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean containsAll(Collection<?> c )
	{
		return c.stream().allMatch( this::contains);
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public Stream<T> findWithScope( Scope scope )
	{
		return base.findWithScope( new Scope(createRangeCondition( scope.getCondition()), scope.getOrder(), scope.getLimit()) );
	}

	@Override
	public T findFirstWithScope( Scope scope )
	{
		return base.findFirstWithScope( new Scope(createRangeCondition( scope.getCondition()), scope.getOrder(), scope.getLimit()) );
	}
}
