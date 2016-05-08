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
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;

/**
 * Mapping of the has-many association into a modifiable Set writing all changes into the backing record-store
 * @author doe300
 * @param <T>
 */
public class HasManyAssociationSet<T extends ActiveRecord> extends AbstractSet<T> implements RecordSet<T>
{
	@Nonnull
	private final ReadOnlyRecordBase<T> destBase;
	@Nonnull
	private final Condition associationCond;
	@Nonnull
	private final Order order;
	@Nonnull
	private final Consumer<T> setAssociationFunc, unsetAssociationFunc;

	/**
	 * @param destBase the RecordBase for the containing records
	 * @param associationCondition the condition for the association
	 * @param order the order of the associated records
	 * @param setAssociationFunction the function to set the association
	 * @param unsetAssociationFunction the function to unset the association
	 */
	public HasManyAssociationSet(@Nonnull final ReadOnlyRecordBase<T> destBase, @Nonnull final Condition associationCondition,
		@Nullable final Order order, @Nonnull final Consumer<T> setAssociationFunction, @Nonnull final Consumer<T> unsetAssociationFunction)
	{
		this.destBase = destBase;
		this.associationCond = associationCondition;
		this.order = order != null ? order : destBase.getDefaultOrder();
		this.setAssociationFunc = setAssociationFunction;
		this.unsetAssociationFunc = unsetAssociationFunction;
	}

	@Override
	public int size()
	{
		return destBase.count( associationCond );
	}

	@Override
	public Order getOrder()
	{
		return order;
	}

	@Override
	public boolean contains( final Object o )
	{
		if(o == null || !destBase.getRecordType().isInstance( o))
		{
			return false;
		}
		final T otherRecord = destBase.getRecordType().cast( o );
		return associationCond.test(otherRecord);
	}

	@Override
	public Iterator<T> iterator()
	{
		return stream().iterator();
	}

	@Override
	public boolean add( final T e )
	{
		if(contains(e ))
		{
			return false;
		}
		setAssociationFunc.accept( e );
		return contains( e );
	}

	@Override
	public boolean remove( final Object o )
	{
		if(contains( o))
		{
			unsetAssociationFunc.accept(destBase.getRecordType().cast( o));
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(final Collection<?> c )
	{
		//select all associated objects not in the other collection and remove association
		return stream().filter( (final T t )-> !c.contains( t)).peek( unsetAssociationFunc).
			//if there are any, the associations were changed
			count() > 0;
	}

	@Override
	public boolean removeAll(final Collection<?> c )
	{
		// select all associations, which are in the other collection and remove
		// the association
		return stream().filter( c::contains).peek( unsetAssociationFunc).
			//if there are any, associated set has changed
			count() > 0;
	}

	@Override
	public void clear()
	{
		stream().forEach( unsetAssociationFunc);
	}

	@Override
	public Stream<T> stream()
	{
		return destBase.findWithScope( new Scope(associationCond, order, Scope.NO_LIMIT));
	}

	@Override
	public Stream<T> findWithScope(final Scope scope)
	{
		final Scope newScope = new Scope(Conditions.and(associationCond, scope.getCondition()), scope.getOrder()!= null ? scope.getOrder() : order, scope.getLimit());
		return destBase.findWithScope(newScope );
	}

	@Override
	public T findFirstWithScope( final Scope scope )
	{
		final Scope newScope = new Scope(Conditions.and(associationCond, scope.getCondition()), scope.getOrder()!= null ? scope.getOrder() : order, scope.getLimit());
		return destBase.findFirstWithScope( newScope );
	}

	@Override
	public ReadOnlyRecordBase<T> getRecordBase()
	{
		return destBase;
	}

	@Override
	public RecordSet<T> getForCondition( final Condition cond, final Order order )
	{
		return new HasManyAssociationSet<T>(destBase, Conditions.and(associationCond, cond), order != null ? order : this.order, setAssociationFunc, unsetAssociationFunc);
	}

	@Override
	public <C, R> R aggregate( final AggregateFunction<T, C, ?, R> aggregateFunction, final Condition condition )
	{
		return ((RecordBase<T>)destBase).aggregate(aggregateFunction, Conditions.and( associationCond, condition) );
	}
}
