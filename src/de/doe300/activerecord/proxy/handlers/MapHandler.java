package de.doe300.activerecord.proxy.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;

/**
 *
 * @author doe300
 */
public class MapHandler implements ProxyHandler
{
	private final Map<ActiveRecord, HandlerMap> maps;

	public MapHandler()
	{
		maps = new TreeMap<>();
	}

	@Override
	public boolean handlesMethod( final ActiveRecord record, final Method method, final Object[] args ) throws IllegalArgumentException
	{
		return record instanceof Map && method.getDeclaringClass().equals( Map.class);
	}

	@Override
	public <T extends ActiveRecord> Object invoke( final ActiveRecord record, final RecordHandler<T> handler,
		final Method method, final Object[] args ) throws IllegalArgumentException
	{
		HandlerMap map = maps.get( record);
		if(map == null)
		{
			map = new HandlerMap(record.getBase(), record.getPrimaryKey(), record.getBase().getStore());
			maps.put( record, map );
		}
		try
		{
			return method.invoke( map, args );
		}
		catch ( IllegalAccessException | InvocationTargetException ex )
		{
			throw new RuntimeException("Failed to proxy Map-method",ex);
		}
	}

	private static class HandlerMap implements Map<String, Object>
	{
		private final RecordStore store;
		private final RecordBase<?> base;
		private final int primaryKey;
		private final String[] columnNames;

		HandlerMap(final RecordBase<?> base, final int primaryKey, final RecordStore store)
		{
			this.base = base;
			this.primaryKey = primaryKey;
			this.store = store;
			columnNames = store.getAllColumnNames( base.getTableName());
		}

		@Override
		public Set<Entry<String, Object>> entrySet()
		{
			return Arrays.stream( columnNames).map( (final String s)->
			{
				return new Entry<String, Object>()
					{
					@Override
					public String getKey()
					{
						return s.toLowerCase();
					}

					@Override
					public Object getValue()
					{
						return store.getValue(base, primaryKey, s );
					}

					@Override
					public Object setValue( final Object value )
					{
						final Object old = getValue();
						store.setValue(base, primaryKey, s, value);
						return old;
					}
					};
			}).collect( Collectors.toSet());
		}

		@Override
		public Object put( final String key, final Object value )
		{
			if(key.equalsIgnoreCase(base.getPrimaryColumn()))
			{
				return primaryKey;
			}
			final Object old = store.getValue( base, primaryKey, key );
			store.setValue( base, primaryKey, key, value );
			return old;
		}

		@Override
		public void putAll(final Map<? extends String, ? extends Object> m )
		{
			for(final Entry<? extends String, ? extends Object> e: m.entrySet())
			{
				if(e.getKey().equalsIgnoreCase(base.getPrimaryColumn()))
				{
					continue;
				}
				store.setValue( base, primaryKey, e.getKey(), e.getValue());
			}
		}

		@Override
		public int size()
		{
			return columnNames.length;
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}

		@Override
		public boolean containsKey( final Object key )
		{
			return Arrays.asList( columnNames).contains( key );
		}

		@Override
		public boolean containsValue( final Object value )
		{
			return values().contains( value );
		}

		@Override
		public Object get( final Object key )
		{
			return store.getValue( base, primaryKey, key.toString());
		}

		@Override
		public Object remove( final Object key )
		{
			throw new UnsupportedOperationException("Can't remove from mapped table");
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException( "Can't clear mapped table" );
		}

		@Override
		public Set<String> keySet()
		{
			return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList( columnNames )));
		}

		@Override
		public Collection<Object> values()
		{
			return keySet().stream().map( (final String s) -> store.getValue( base, primaryKey, s)).collect( Collectors.toList());
		}
	}
}
