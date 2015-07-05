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
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A RecordSet containing of records matching the given Condition
 * @author doe300
 * @param <T>
 */
public class ConditionSet<T extends ActiveRecord> extends AbstractSet<T> implements RecordSet<T>
{
	private final RecordBase<T> base;
	private final Condition condition;
	private final Consumer<T> setConditionFunc, unsetConditionFunc;

	/**
	 * 
	 * @param base the record-base for this record-type
	 * @param condition the Condition to match
	 * @param setCondFunc a function manipulating the records to match the condition, used for add-operations
	 * @param removeCondFunc a function changing the record to not match the condition, used for remove-operations
	 */
	public ConditionSet( RecordBase<T> base, Condition condition, Consumer<T> setCondFunc, Consumer<T> removeCondFunc )
	{
		this.base = base;
		this.condition = condition;
		this.setConditionFunc = setCondFunc;
		this.unsetConditionFunc = removeCondFunc;
	}

	@Override
	public RecordBase<T> getRecordBase()
	{
		return base;
	}

	@Override
	public int size()
	{
		return base.count( condition );
	}

	@Override
	public boolean contains( Object o )
	{
		if(o == null || !base.getRecordType().isInstance( o))
		{
			return false;
		}
		T otherRecord = base.getRecordType().cast( o );
		return condition.test( otherRecord );
	}

	@Override
	public Iterator<T> iterator()
	{
		return stream().iterator();
	}

	@Override
	public boolean add( T e )
	{
		if(contains( e ))
		{
			return false;
		}
		setConditionFunc.accept( e );
		return contains( e );
	}

	@Override
	public boolean remove( Object o )
	{
		if(contains( o ))
		{
			unsetConditionFunc.accept( base.getRecordType().cast( o ));
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c )
	{
		//select all records not in the other collection and remove association
		return stream().filter( (T t )-> !c.contains( t)).peek( unsetConditionFunc).
				//if there are any, the recors were changed
				count() > 0;
	}

	@Override
	public boolean removeAll(Collection<?> c )
	{
		//select all records, which are in the other collection and remove the record
		return stream().filter( c::contains).peek( unsetConditionFunc).
				//if there are any, records set have changed
				count() > 0;
	}

	@Override
	public void clear()
	{
		stream().forEach( unsetConditionFunc);
	}

	@Override
	public Stream<T> stream()
	{
		return base.find( condition );
	}

	@Override
	public Stream<T> findWithScope( Scope scope )
	{
		Scope newScope = new Scope(new AndCondition(condition, scope.getCondition()), scope.getOrder(), scope.getLimit());
		return base.findWithScope(newScope );
	}

	@Override
	public T findFirstWithScope( Scope scope )
	{
		Scope newScope = new Scope(new AndCondition(condition, scope.getCondition()), scope.getOrder(), scope.getLimit());
		return base.findFirstWithScope( newScope );
	}

	@Override
	public SortedSet<T> headSet( T toElement )
	{
		return new ConditionSet<T>(base, new AndCondition(condition, new SimpleCondition(base.getPrimaryColumn(), toElement.getPrimaryKey(), Comparison.SMALLER)), setConditionFunc, unsetConditionFunc);
	}

	@Override
	public SortedSet<T> tailSet( T fromElement )
	{
		return new ConditionSet<T>(base, new AndCondition(condition, new SimpleCondition(base.getPrimaryColumn(), fromElement.getPrimaryKey(), Comparison.LARGER)), setConditionFunc, unsetConditionFunc);
	}

	@Override
	public SortedSet<T> subSet( T fromElement, T toElement )
	{
		return new ConditionSet<T>(base, new AndCondition(condition, 
				new SimpleCondition(base.getPrimaryColumn(), fromElement.getPrimaryKey(), Comparison.LARGER),
				new SimpleCondition(base.getPrimaryColumn(), toElement.getPrimaryKey(), Comparison.SMALLER)
		), setConditionFunc, unsetConditionFunc);
	}
}
