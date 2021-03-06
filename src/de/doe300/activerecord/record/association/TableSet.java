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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import javax.annotation.Nullable;

/**
 * A RecordSet containing all records in a table
 *
 * NOTE: this set is unmodifiable
 * @author doe300
 * @param <T>
 */
public class TableSet<T extends ActiveRecord> extends AbstractSet<T> implements RecordSet<T>
{
	@Nonnull
	private final RecordBase<T> base;
	@Nonnull
	private final Order order;

	/**
	 * Standard constructor for creating a set containing all records in the {@link RecordBase}
	 * @param base
	 * @param order
	 */
	public TableSet(@Nonnull final RecordBase<T> base, @Nullable final Order order)
	{
		this.base = base;
		this.order = order != null ? order : base.getDefaultOrder();
	}

	@Override
	public ReadOnlyRecordBase<T> getRecordBase()
	{
		return base;
	}

	@Override
	public Order getOrder()
	{
		return order;
	}

	@Override
	public Stream<T> stream()
	{
		return base.find( null);
	}

	@Override
	public int size()
	{
		return base.count( null);
	}

	@Override
	public boolean contains( final Object o )
	{
		return base.getRecordType().isInstance( o ) && base.hasRecord( ((ActiveRecord)o).getPrimaryKey() );
	}

	@Override
	public Iterator<T> iterator()
	{
		return stream().iterator();
	}

	@Override
	public boolean add( final T e )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean remove( final Object o )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean addAll(final Collection<? extends T> c )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean retainAll(final Collection<?> c )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean removeAll(final Collection<?> c )
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public boolean containsAll(final Collection<?> c )
	{
		return c.stream().allMatch( this::contains);
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("TableSets are unmodifiable");
	}

	@Override
	public Stream<T> findWithScope( final Scope scope )
	{
		return base.findWithScope( new Scope(scope.getCondition(), scope.getOrder() != null ? scope.getOrder() : order, scope.getLimit()) );
	}

	@Override
	public T findFirstWithScope( final Scope scope )
	{
		return base.findFirstWithScope( new Scope(scope.getCondition(), scope.getOrder() != null ? scope.getOrder() : order, scope.getLimit()) );
	}

	@Override
	public RecordSet<T> getForCondition( final Condition cond, @Nullable final Order order)
	{
		return new ConditionSet<T>(base, cond, order != null ? order : this.order );
	}

	@Override
	public <C, R> R aggregate( AggregateFunction<T, C, ?, R> aggregateFunction, @Nullable final Condition condition )
	{
		return base.aggregate( aggregateFunction, condition );
	}
}
