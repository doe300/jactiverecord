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

import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import javax.annotation.Nullable;

/**
 * A RecordSet containing of records matching the given Condition.
 * This set can optionally be made immutable by passing <code>null</code> for the set- and unset-functions.
 * @author doe300
 * @param <T>
 */
public class ConditionSet<T extends ActiveRecord> extends AbstractSet<T> implements RecordSet<T>
{
	@Nonnull
	private final ReadOnlyRecordBase<T> base;
	private final Condition condition;
	private final Consumer<T> setConditionFunc, unsetConditionFunc;

	/**
	 * This constructor creates a immutable or read-only ConditionSet throwing UnsupportedOperationExcpetion on any mutating methods
	 *
	 * @param base the RecordBase for this record-type
	 * @param condition the Condition to match
	 */
	public ConditionSet(@Nonnull final ReadOnlyRecordBase<T> base, final Condition condition)
	{
		this(base, condition, null, null);
	}

	/**
	 * If the <code>setCondFunc</code> or <code>removeCondFunc</code> is <code>null</code>, this set will be immutable
	 *
	 * @param base the record-base for this record-type
	 * @param condition the Condition to match
	 * @param setCondFunc a function manipulating the records to match the condition, used for add-operations
	 * @param removeCondFunc a function changing the record to not match the condition, used for remove-operations
	 */
	public ConditionSet(@Nonnull final ReadOnlyRecordBase<T> base, final Condition condition,
		final Consumer<T> setCondFunc, final Consumer<T> removeCondFunc)
	{
		this.base = base;
		this.condition = condition;
		this.setConditionFunc = setCondFunc;
		this.unsetConditionFunc = removeCondFunc;
	}

	private void checkFunctions() throws UnsupportedOperationException
	{
		if(setConditionFunc == null || unsetConditionFunc == null)
		{
			throw new UnsupportedOperationException("This ConditionSet is immutable");
		}
	}

	@Override
	public ReadOnlyRecordBase<T> getRecordBase()
	{
		return base;
	}

	@Override
	public int size()
	{
		return base.count( condition );
	}

	@Override
	public boolean contains( final Object o )
	{
		if(o == null || !base.getRecordType().isInstance( o))
		{
			return false;
		}
		final T otherRecord = base.getRecordType().cast( o );
		return condition.test( otherRecord );
	}

	@Override
	public Iterator<T> iterator()
	{
		return stream().iterator();
	}

	@Override
	public boolean add( final T e )
	{
		checkFunctions();
		if(contains( e ))
		{
			return false;
		}
		setConditionFunc.accept( e );
		return contains( e );
	}

	@Override
	public boolean remove( final Object o )
	{
		checkFunctions();
		if(contains( o ))
		{
			unsetConditionFunc.accept( base.getRecordType().cast( o ));
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(final Collection<?> c )
	{
		checkFunctions();
		//select all records not in the other collection and remove association
		return stream().filter( (final T t )-> !c.contains( t)).peek( unsetConditionFunc).
				//if there are any, the recors were changed
				count() > 0;
	}

	@Override
	public boolean removeAll(final Collection<?> c )
	{
		checkFunctions();
		//select all records, which are in the other collection and remove the record
		return stream().filter( c::contains).peek( unsetConditionFunc).
				//if there are any, records set have changed
				count() > 0;
	}

	@Override
	public void clear()
	{
		checkFunctions();
		stream().forEach( unsetConditionFunc);
	}

	@Override
	public Stream<T> stream()
	{
		return base.find( condition );
	}

	@Override
	public Stream<T> findWithScope( final Scope scope )
	{
		final Scope newScope = new Scope(AndCondition.andConditions(condition, scope.getCondition()), scope.getOrder(), scope.getLimit());
		return base.findWithScope(newScope );
	}

	@Override
	public T findFirstWithScope( final Scope scope )
	{
		final Scope newScope = new Scope(AndCondition.andConditions(condition, scope.getCondition()), scope.getOrder(), scope.getLimit());
		return base.findFirstWithScope( newScope );
	}

	@Override
	public RecordSet<T> getForCondition( final Condition cond )
	{
		return new ConditionSet<T>(base, AndCondition.andConditions(cond, condition), setConditionFunc, unsetConditionFunc);
	}

	@Override
	public <C, R> R aggregate( AggregateFunction<T, C, R> aggregateFunction, @Nullable final Condition condition )
	{
		return ((RecordBase<T>) base).aggregate( aggregateFunction, AndCondition.andConditions( this.condition, condition));
	}
}
