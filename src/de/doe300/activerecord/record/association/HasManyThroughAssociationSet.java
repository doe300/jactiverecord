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
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * has-many-through association represented as modifiable Set writing all changes into the backing record-store
 * @author doe300
 * @param <T>
 */
public class HasManyThroughAssociationSet<T extends ActiveRecord> extends AbstractSet<T> implements AssociationSet<T>
{
	private final RecordBase<T> destBase;
	private final String mappingTableName, thisMappingKey, foreignMappingKey;
	private final int thisPrimaryKey;

	/**
	 * 
	 * @param destBase the RecordBase for the associated record
	 * @param thisPrimaryKey the primary-key to list the associations for
	 * @param mappingTableName the name of the mapping-table
	 * @param thisMappingKey the column of the mapping-table the primary key for the source object is stored
	 * @param foreignMappingKey the column of the mapping-table the primary key for the associated objects are stored
	 */
	public HasManyThroughAssociationSet( RecordBase<T> destBase, int thisPrimaryKey, String mappingTableName, String thisMappingKey,
			String foreignMappingKey )
	{
		this.destBase = destBase;
		this.thisPrimaryKey = thisPrimaryKey;
		this.mappingTableName = mappingTableName;
		this.thisMappingKey = thisMappingKey;
		this.foreignMappingKey = foreignMappingKey;
	}
	
	protected Stream<Integer> getAssocationKeys()
	{
		return destBase.getStore().getValues( mappingTableName, foreignMappingKey, thisMappingKey, thisPrimaryKey ).map( (Object o) -> (Integer)o);
	}

	@Override
	public int size()
	{
		return ( int ) getAssocationKeys().count();
	}

	@Override
	public boolean contains( Object o )
	{
		if(!destBase.getRecordType().isInstance(o))
		{
			return false;
		}
		T t = destBase.getRecordType().cast( o );
		return getAssocationKeys().anyMatch( (Integer i) -> i == t.getPrimaryKey() );
	}

	@Override
	public Iterator<T> iterator()
	{
		return getAssocationKeys().map( (Integer key ) -> destBase.getRecord( key)).iterator();
	}

	@Override
	public boolean add( T e )
	{
		if(contains( e ))
		{
			return false;
		}
		return destBase.getStore().addRow( mappingTableName, new String[]{thisMappingKey,foreignMappingKey}, new Object[]{thisPrimaryKey,e.getPrimaryKey()} );
	}
	
	protected boolean remove0(Integer key)
	{
		Condition cond = new AndCondition(
				new SimpleCondition(thisMappingKey, thisPrimaryKey, Comparison.IS),
				new SimpleCondition(foreignMappingKey, key, Comparison.IS)
		);
		return destBase.getStore().removeRow( mappingTableName, cond );
	}

	@Override
	public boolean remove( Object o )
	{
		if(!destBase.getRecordType().isInstance( o ) || !contains(o ))
		{
			return false;
		}
		return remove0( destBase.getRecordType().cast( o ).getPrimaryKey());
	}

	@Override
	public boolean retainAll(Collection<?> c )
	{
		return stream().filter( (T t ) -> !c.contains( t)).peek( (T t)->
		{
			remove0( t.getPrimaryKey() );
		} ).count() > 0;
	}

	@Override
	public boolean removeAll(Collection<?> c )
	{
		return stream().filter( c::contains).peek( (T t)->
		{
			remove0( t.getPrimaryKey() );
		} ).count() > 0;
	}

	@Override
	public void clear()
	{
		getAssocationKeys().forEach( (Integer i) -> {remove0( i );} );
	}

	@Override
	public Stream<T> stream()
	{
		return getAssocationKeys().map( destBase::getRecord );
	}

	@Override
	public Stream<T> find( Condition condition )
	{
		Set<Integer> keys = getAssocationKeys().collect( Collectors.toSet());
		return destBase.find( new AndCondition(new SimpleCondition(destBase.getPrimaryColumn(), keys, Comparison.IN), condition));
	}

	@Override
	public T findFirst( Condition condition )
	{
		Set<Integer> keys = getAssocationKeys().collect( Collectors.toSet());
		return destBase.findFirst( new AndCondition(new SimpleCondition(destBase.getPrimaryColumn(), keys, Comparison.IN), condition));
	}
}
