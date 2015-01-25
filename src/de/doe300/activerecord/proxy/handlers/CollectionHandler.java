package de.doe300.activerecord.proxy.handlers;

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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;

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
	public boolean handlesMethod( final ActiveRecord record, final Method method, final Object[] args ) throws IllegalArgumentException
	{
		return method.getDeclaringClass().isAssignableFrom( Set.class);
	}

	@Override
	public <T extends ActiveRecord> Object invoke( final ActiveRecord record, final RecordHandler<T> handler,final Method method, final Object[] args ) throws IllegalArgumentException
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

	private static class HandlerCollection implements Collection<Object>
	{
		private final RecordBase<?> base;
		private final RecordStore store;
		private final int primaryKey;
		private final Set<String> columnNames;

		HandlerCollection(final RecordBase<?> base, final int primaryKey, final RecordStore store)
		{
			this.base = base;
			this.primaryKey = primaryKey;
			this.store = store;
			columnNames = store.getAllColumnNames( base.getTableName());
		}

		@Override
		public int size()
		{
			return columnNames.size();
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
				private Iterator<String> it = columnNames.iterator();

				@Override
				public boolean hasNext()
				{
					return it.hasNext();
				}

				@Override
				public Object next()
				{
					return store.getValue(base, primaryKey, it.next());
				}
				};
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}

		@Override
		public boolean contains( final Object o )
		{
			for(final String column:columnNames)
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
			final Object[] arr = new Object[ columnNames.size()];
			Iterator<String> it = columnNames.iterator();
			for(int i=0;i<columnNames.size()&&it.hasNext();i++)
			{
				arr[i] = store.getValue( base, primaryKey, it.next());
			}
			return arr;
		}

		@Override
		public <T> T[] toArray( final T[] a )
		{
			final T[] res = a.length >= columnNames.size() ? a : Arrays.copyOf( a, columnNames.size());
			Iterator<String> it = columnNames.iterator();
			for(int i=0;i<columnNames.size()&&it.hasNext();i++)
			{
				res[i] = ( T ) store.getValue( base, primaryKey, it.next());
			}
			return res;
		}

		@Override
		public boolean add( final Object e )
		{
			throw new UnsupportedOperationException( "Can't add column to DB." );
		}

		@Override
		public boolean remove( final Object o )
		{
			throw new UnsupportedOperationException( "Can't remove column from DB." );
		}

		@Override
		public boolean containsAll(final Collection<?> c )
		{
			return c.stream().allMatch( (final Object o) -> contains( o));
		}

		@Override
		public boolean addAll(final Collection<? extends Object> c )
		{
			throw new UnsupportedOperationException( "Can't add colums to DB." );
		}

		@Override
		public boolean retainAll(final Collection<?> c )
		{
			throw new UnsupportedOperationException( "Can't remove columns from DB." );
		}

		@Override
		public boolean removeAll(final Collection<?> c )
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
