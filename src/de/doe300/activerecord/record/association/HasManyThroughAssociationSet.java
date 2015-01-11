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
import java.util.stream.Stream;

/**
 * has-many-through association represented as modifiable Set writing all changes into the backing record-store
 * @author doe300
 * @param <T>
 */
public class HasManyThroughAssociationSet<T extends ActiveRecord> extends AbstractSet<T> implements Set<T>
{
	private final RecordBase<T> destBase;
	private final String mappingTableName, thisMappingKey, foreignMappingKey;
	private final int thisPrimaryKey;

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
	
	protected boolean remove0(T t)
	{
		Condition cond = new AndCondition(
				new SimpleCondition(thisMappingKey, thisPrimaryKey, Comparison.IS),
				new SimpleCondition(foreignMappingKey, t.getPrimaryKey(), Comparison.IS)
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
		return remove0( destBase.getRecordType().cast( o ));
	}

	@Override
	public boolean retainAll(Collection<?> c )
	{
		return getAssocationKeys().map( destBase::getRecord ).filter( (T t ) -> !c.contains( t)).peek( (T t)->
		{
			remove0( t );
		} ).count() > 0;
	}

	@Override
	public boolean removeAll(Collection<?> c )
	{
		return getAssocationKeys().map( destBase::getRecord ).filter( c::contains).peek( (T t)->
		{
			remove0( t );
		} ).count() > 0;
	}

	@Override
	public void clear()
	{
		getAssocationKeys().forEach( null );
	}

}
