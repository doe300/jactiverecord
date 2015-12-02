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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import javax.annotation.Nullable;

/**
 * has-many-through association represented as modifiable Set writing all changes into the backing record-store
 * @author doe300
 * @param <T>
 */
public class HasManyThroughAssociationSet<T extends ActiveRecord> extends AbstractSet<T> implements RecordSet<T>
{
	@Nonnull
	final RecordBase<T> destBase;
	@Nonnull
	private final Order order;
	@Nonnull
	private final String mappingTableName, thisMappingKey, foreignMappingKey;
	private final int thisPrimaryKey;

	/**
	 *
	 * @param destBase the RecordBase for the associated record
	 * @param thisPrimaryKey the primary-key to list the associations for
	 * @param order the order of the records
	 * @param mappingTableName the name of the mapping-table
	 * @param thisMappingKey the column of the mapping-table the primary key for the source object is stored
	 * @param foreignMappingKey the column of the mapping-table the primary key for the associated objects are stored
	 */
	public HasManyThroughAssociationSet(@Nonnull final RecordBase<T> destBase, final int thisPrimaryKey,
			final Order order, @Nonnull final String mappingTableName, @Nonnull final String thisMappingKey,
		@Nonnull final String foreignMappingKey)
	{
		this.destBase = destBase;
		this.order = order != null ? order : destBase.getDefaultOrder();
		this.thisPrimaryKey = thisPrimaryKey;
		this.mappingTableName = mappingTableName;
		this.thisMappingKey = thisMappingKey;
		this.foreignMappingKey = foreignMappingKey;
	}

	protected Stream<Integer> getAssocationKeys()
	{
		return destBase.getStore().getValues( mappingTableName, foreignMappingKey, thisMappingKey, thisPrimaryKey ).map( (final Object o) -> (Integer)o);
	}

	@Override
	public int size()
	{
		return ( int ) getAssocationKeys().count();
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
		return getAssocationKeys().anyMatch( (final Integer i) -> i == otherRecord.getPrimaryKey() );
	}

	@Override
	public Iterator<T> iterator()
	{
		return getAssocationKeys().map( (final Integer key ) -> destBase.getRecord( key)).sorted( order).iterator();
	}

	@Override
	public boolean add( final T e )
	{
		if(contains( e ))
		{
			return false;
		}
		return destBase.getStore().addRow( mappingTableName, new String[]{thisMappingKey,foreignMappingKey}, new Object[]{thisPrimaryKey,e.getPrimaryKey()} );
	}

	boolean remove0(final Integer key)
	{
		final Condition cond = AndCondition.andConditions(
			new SimpleCondition(thisMappingKey, thisPrimaryKey, Comparison.IS),
			new SimpleCondition(foreignMappingKey, key, Comparison.IS)
			);
		return destBase.getStore().removeRow( mappingTableName, cond );
	}

	@Override
	public boolean remove( final Object o )
	{
		if(!destBase.getRecordType().isInstance( o ) || !contains(o ))
		{
			return false;
		}
		return remove0( destBase.getRecordType().cast( o ).getPrimaryKey());
	}

	@Override
	public boolean retainAll(final Collection<?> c )
	{
		return stream().filter( (final T t ) -> !c.contains( t)).peek( (final T t)->
		{
			remove0( t.getPrimaryKey() );
		} ).count() > 0;
	}

	@Override
	public boolean removeAll(final Collection<?> c )
	{
		return stream().filter( c::contains).peek( (final T t)->
		{
			remove0( t.getPrimaryKey() );
		} ).count() > 0;
	}

	@Override
	public void clear()
	{
		getAssocationKeys().forEach( (final Integer i) -> {remove0( i );} );
	}

	@Override
	public Stream<T> stream()
	{
		return getAssocationKeys().map( destBase::getRecord ).sorted( order);
	}

	@Override
	public Stream<T> findWithScope( final Scope scope)
	{
		final Set<Integer> keys = getAssocationKeys().collect( Collectors.toSet());
		final Scope newScope = new Scope(AndCondition.andConditions(new SimpleCondition(destBase.getPrimaryColumn(), keys, Comparison.IN), scope.getCondition()), scope.getOrder() != null ? scope.getOrder() : order, scope.getLimit());
		return destBase.findWithScope( newScope );
	}

	@Override
	public T findFirstWithScope( final Scope scope)
	{
		final Set<Integer> keys = getAssocationKeys().collect( Collectors.toSet());
		final Scope newScope = new Scope(AndCondition.andConditions(new SimpleCondition(destBase.getPrimaryColumn(), keys, Comparison.IN), scope.getCondition()), scope.getOrder() != null ? scope.getOrder() : order, scope.getLimit());
		return destBase.findFirstWithScope( newScope);
	}

	@Override
	public ReadOnlyRecordBase<T> getRecordBase()
	{
		return destBase;
	}

	@Override
	public RecordSet<T> getForCondition( final Condition cond, final Order order)
	{
		return new HasManyThroughSubSet(cond, order != null ? order : this.order);
	}

	@Override
	public <C, R> R aggregate( AggregateFunction<T, C, ?, R> aggregateFunction, @Nullable final Condition condition )
	{
		return stream().filter((T record) -> condition == null || condition.test( record )).collect( aggregateFunction );
	}

	private class HasManyThroughSubSet extends AbstractSet<T> implements RecordSet<T>
	{
		private final Condition subCondition;
		private final Order order;

		HasManyThroughSubSet(final Condition subCondition, final Order order)
		{
			this.subCondition = subCondition;
			this.order = order;
		}

		@Override
		public Stream<T> stream()
		{
			return getAssocationKeys().map( destBase::getRecord).filter( subCondition).sorted( order);
		}

		@Override
		public int size()
		{
			return ( int ) stream().count();
		}

		@Override
		public Order getOrder()
		{
			return order;
		}
		
		@Override
		public boolean contains( final Object o )
		{
			return o != null && HasManyThroughAssociationSet.this.contains(o) && subCondition.test((ActiveRecord) o);
		}

		@Override
		public Iterator<T> iterator()
		{
			return stream().iterator();
		}

		@Override
		public boolean add( final T e )
		{
			if (e == null || !subCondition.test(e))
			{
				return false;
			}
			return HasManyThroughAssociationSet.this.add( e );
		}

		@Override
		public boolean remove( final Object o )
		{
			return contains( o ) && HasManyThroughAssociationSet.this.remove( o );
		}

		@Override
		public boolean retainAll(final Collection<?> c )
		{
			return stream().filter( (final T t ) -> !c.contains( t)).peek( (final T t)->
			{
				remove0( t.getPrimaryKey() );
			} ).count() > 0;
		}

		@Override
		public boolean removeAll(final Collection<?> c )
		{
			return stream().filter( c::contains).peek( (final T t)->
			{
				remove0( t.getPrimaryKey() );
			} ).count() > 0;
		}

		@Override
		public void clear()
		{
			stream().forEach( (final T t) -> remove0( t.getPrimaryKey()));
		}

		@Override
		public ReadOnlyRecordBase<T> getRecordBase()
		{
			return destBase;
		}

		@Override
		public Stream<T> findWithScope( final Scope scope)
		{
			final Scope newScope = new Scope(
				AndCondition.andConditions(subCondition, scope.getCondition()),
				scope.getOrder() != null ? scope.getOrder() : order, scope.getLimit());
			return destBase.findWithScope( newScope );
		}

		@Override
		public T findFirstWithScope( final Scope scope)
		{
			final Scope newScope = new Scope(
				AndCondition.andConditions(subCondition,scope.getCondition()),
				scope.getOrder() != null ? scope.getOrder() : order, scope.getLimit());
			return destBase.findFirstWithScope( newScope);
		}

		@Override
		public RecordSet<T> getForCondition( final Condition cond, final Order order )
		{
			return new HasManyThroughSubSet(AndCondition.andConditions(subCondition,cond), order != null ? order : this.order);
		}

		@Override
		public <C, R> R aggregate( AggregateFunction<T, C, ?, R> aggregateFunction, @Nullable final Condition condition )
		{
			return stream().filter((T record) -> condition == null || condition.test( record )).collect( aggregateFunction );
		}
	}
}
