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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Mapping of the has-many association into a modifiable Set writing all changes into the backing record-store
 * @author doe300
 * @param <T>
 */
public class HasManyAssociationSet<T extends ActiveRecord> extends AbstractSet<T> implements RecordSet<T>
{
	private final RecordBase<T> destBase;
	private final Condition associationCond;
	private final Consumer<T> setAssociationFunc, unsetAssociationFunc;

	/**
	 * @param destBase the RecordBase for the containing records
	 * @param associationCondition the condition for the association
	 * @param setAssociationFunction the function to set the association
	 * @param unsetAssociationFunction the function to unset the association
	 */
	public HasManyAssociationSet(RecordBase<T> destBase, Condition associationCondition, Consumer<T> setAssociationFunction, Consumer<T> unsetAssociationFunction)
	{
		this.destBase = destBase;
		this.associationCond = associationCondition;
		this.setAssociationFunc = setAssociationFunction;
		this.unsetAssociationFunc = unsetAssociationFunction;
	}

	@Override
	public int size()
	{
		return destBase.count( associationCond );
	}

	@Override
	public boolean contains( Object o )
	{
		return stream().anyMatch( (T t)-> t.equals( o) );
	}

	@Override
	public Iterator<T> iterator()
	{
		return stream().iterator();
	}

	@Override
	public boolean add( T e )
	{
		if(associationCond.test( e ))
		{
			return false;
		}
		setAssociationFunc.accept( e );
		return associationCond.test( e );
	}

	@Override
	public boolean remove( Object o )
	{
		if(destBase.getRecordType().isInstance( o ) && associationCond.test(( ActiveRecord ) o))
		{
			unsetAssociationFunc.accept(destBase.getRecordType().cast( o));
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c )
	{
		return c.stream().allMatch( (Object o) -> destBase.getRecordType().isInstance( o ) && associationCond.test(( ActiveRecord ) o));
	}

	@Override
	public boolean addAll(Collection<? extends T> c )
	{
		//select all object not already added as association
		return c.stream().filter( (T t) -> !associationCond.test( t) ).
				//add assocation
				peek( setAssociationFunc).
				//if there are any, associations was changed
				count() > 0;
				
	}

	@Override
	public boolean retainAll(Collection<?> c )
	{
		//select all associated objects not in the other collection and remove association
		return stream().filter( (T t )-> !c.contains( t)).peek( unsetAssociationFunc).
				//if there are any, the associations were cahnged
				count() > 0;
	}

	@Override
	public boolean removeAll(Collection<?> c )
	{
		//select all associations, which are in the other collection and remove the assoication
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
		return destBase.find( associationCond );
	}

	@Override
	public Stream<T> findWithScope(final Scope scope)
	{
		Scope newScope = new Scope(new AndCondition(associationCond, scope.getCondition()), scope.getOrder(), scope.getLimit());
		return destBase.findWithScope(newScope );
	}

	@Override
	public T findFirstWithScope( final Scope scope )
	{
		Scope newScope = new Scope(new AndCondition(associationCond, scope.getCondition()), scope.getOrder(), scope.getLimit());
		return destBase.findFirstWithScope( newScope );
	}

	@Override
	public RecordBase<T> getRecordBase()
	{
		return destBase;
	}
}
