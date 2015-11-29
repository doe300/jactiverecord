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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import javax.annotation.Nullable;

/**
 * A CachedRecordSet caches the records for successive calls, but will NOT be notified about changes in the underling DB.
 *
 * NOTE: CachedRecordSet is unmodifiable
 *
 * @author doe300
 * @param <T>
 */
public class CachedRecordSet<T extends ActiveRecord> extends AbstractSet<T> implements RecordSet<T>
{
	@Nonnull
	private final RecordSet<T> source;
	@Nonnull
	private final SortedSet<T> cache;

	/**
	 * @param source
	 */
	public CachedRecordSet(@Nonnull final RecordSet<T> source)
	{
		this.source = source;
		this.cache = new TreeSet<T>();
	}

	@Override
	public Stream<T> stream()
	{
		if(cache.isEmpty())
		{
			source.stream().forEach(cache::add);
		}
		return cache.stream();
	}

	@Override
	public ReadOnlyRecordBase<T> getRecordBase()
	{
		return source.getRecordBase();
	}

	@Override
	public RecordSet<T> getForCondition( final Condition cond, @Nullable final Order order )
	{
		return new CachedRecordSet<T>(source.getForCondition( cond, order));
	}

	@Override
	public Order getOrder()
	{
		return source.getOrder();
	}

	@Override
	public T first()
	{
		if(!cache.isEmpty())
		{
			return cache.first();
		}
		return source.first();
	}

	@Override
	public T last()
	{
		if(!cache.isEmpty())
		{
			return cache.last();
		}
		return source.last();
	}

	@Override
	public int size()
	{
		if(!cache.isEmpty())
		{
			return cache.size();
		}
		return source.size();
	}

	@Override
	public boolean isEmpty()
	{
		if(!cache.isEmpty())
		{
			return false;
		}
		return source.isEmpty();
	}

	@Override
	public boolean contains( final Object o )
	{
		if(!cache.isEmpty())
		{
			return cache.contains( o );
		}
		return source.contains( o);
	}

	@Override
	public Iterator<T> iterator()
	{
		return stream().iterator();
	}

	@Override
	public boolean add( final T e )
	{
		throw new UnsupportedOperationException("CachedRecordSets are unmodifiable");
	}

	@Override
	public boolean remove( final Object o )
	{
		throw new UnsupportedOperationException("CachedRecordSets are unmodifiable");
	}

	@Override
	public boolean containsAll(final Collection<?> c )
	{
		return c.stream().allMatch( this::contains);
	}

	@Override
	public boolean addAll(final Collection<? extends T> c )
	{
		throw new UnsupportedOperationException("CachedRecordSets are unmodifiable");
	}

	@Override
	public boolean retainAll(final Collection<?> c )
	{
		throw new UnsupportedOperationException("CachedRecordSets are unmodifiable");
	}

	@Override
	public boolean removeAll(final Collection<?> c )
	{
		throw new UnsupportedOperationException("CachedRecordSets are unmodifiable");
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("CachedRecordSets are unmodifiable");
	}

	@Override
	public Stream<T> findWithScope( final Scope scope )
	{
		return source.findWithScope( scope );
	}

	@Override
	public T findFirstWithScope( final Scope scope )
	{
		return source.findFirstWithScope( scope );
	}

	@Override
	public <C, R> R aggregate( AggregateFunction<T, C, ?, R> aggregateFunction, @Nullable final Condition condition )
	{
		return stream().filter( (T record) -> condition == null || condition.test( record)).collect( aggregateFunction );
	}

	@Override
	public RecordSet<T> cached()
	{
		return this;
	}
}
