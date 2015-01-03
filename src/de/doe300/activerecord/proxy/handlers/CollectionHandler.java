package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;

/**
 *
 * @author doe300
 */
public class CollectionHandler implements ProxyHandler
{
	private final Map<ActiveRecord, HandlerCollection> collections;

	public CollectionHandler()
	{
		this.collections = new TreeMap<>();
	}

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		return method.getDeclaringClass().isAssignableFrom( Set.class);
	}

	@Override
	public <T extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<T> handler,Method method, Object[] args ) throws IllegalArgumentException
	{
		HandlerCollection col = collections.get( record);
		if(col == null)
		{
			col = new HandlerCollection(record.getBase(), record.getPrimaryKey(), record.getBase().getStore());
			collections.put( record, col );
		}
		try
		{
			return method.invoke( col, args );
		}
		catch ( IllegalAccessException | InvocationTargetException ex )
		{
			throw new RuntimeException("Failed to proxy collection-call",ex);
		}
	}
	
	private class HandlerCollection implements Collection<Object>
	{
		private final RecordBase<?> base;
		private final RecordStore store;
		private final int primaryKey;
		private final String[] columnNames;
		
		HandlerCollection(RecordBase<?> base, int primaryKey, RecordStore store)
		{
			this.base = base;
			this.primaryKey = primaryKey;
			this.store = store;
			columnNames = store.getAllColumnNames( base.getTableName());
		}

		@Override
		public int size()
		{
			return columnNames.length;
		}

		@Override
		public Spliterator<Object> spliterator()
		{
			return Spliterators.spliterator( this, Spliterator.IMMUTABLE|Spliterator.ORDERED|Spliterator.SIZED);
		}

		@Override
		public Iterator<Object> iterator()
		{
			return new Iterator<Object>()
			{
				private int index = -1;

				@Override
				public boolean hasNext()
				{
					index++;
					return index<columnNames.length;
				}

				@Override
				public Object next()
				{
					return store.getValue(base, primaryKey, columnNames[index]);
				}
			};
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}

		@Override
		public boolean contains( Object o )
		{
			for(String column:columnNames)
			{
				if(Objects.equals( store.getValue( base, primaryKey, column ), o))
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public Object[] toArray()
		{
			Object[] arr = new Object[ columnNames.length];
			for(int i=0;i<columnNames.length;i++)
			{
				arr[i] = store.getValue( base, primaryKey, columnNames[i]);
			}
			return arr;
		}

		@Override
		public <T> T[] toArray( T[] a )
		{
			T[] res = a.length >= columnNames.length ? a : Arrays.copyOf( a, columnNames.length);
			for(int i=0;i<columnNames.length;i++)
			{
				res[i] = ( T ) store.getValue( base, primaryKey, columnNames[i]);
			}
			return res;
		}

		@Override
		public boolean add( Object e )
		{
			throw new UnsupportedOperationException( "Can't add column to DB." );
		}

		@Override
		public boolean remove( Object o )
		{
			throw new UnsupportedOperationException( "Can't remove column from DB." );
		}

		@Override
		public boolean containsAll(Collection<?> c )
		{
			return c.stream().allMatch( (Object o) -> contains( o));
		}

		@Override
		public boolean addAll(Collection<? extends Object> c )
		{
			throw new UnsupportedOperationException( "Can't add colums to DB." );
		}

		@Override
		public boolean retainAll(Collection<?> c )
		{
			throw new UnsupportedOperationException( "Can't remove columns from DB." );
		}

		@Override
		public boolean removeAll(Collection<?> c )
		{
			throw new UnsupportedOperationException( "Can't remove columns from DB." );
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException( "Can't clear mapped table." );
		}
	}
}
