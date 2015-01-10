package de.doe300.activerecord;

import de.doe300.activerecord.pojo.POJOBase;
import de.doe300.activerecord.proxy.ProxyBase;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.MapRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core class for the active record API
 * @author doe300
 */
public final class RecordCore implements AutoCloseable
{
	private final static Map<String, RecordCore> cores = new HashMap<>(5);
	
	private final RecordStore store;
	private final Map<Class<? extends ActiveRecord>, RecordBase<?>> bases;
	private Map<Class<? extends ActiveRecord>, ProxyHandler[]> handlers;

	private RecordCore( RecordStore store )
	{
		this.store = store;
		this.bases = new HashMap<Class<? extends ActiveRecord>, RecordBase<?>>(10);
	}

	/**
	 * Returns an existing or a new RecordCore with the underlying database as source
	 * @param dbConnection
	 * @param cached whether the used {@link RecordStore} should be cached
	 * @return the existing or newly created RecordCore
	 * @throws SQLException 
	 */
	public static RecordCore fromDatabase(Connection dbConnection, boolean cached) throws SQLException
	{
		String cat = dbConnection.getCatalog();
		RecordCore core = cores.get( cat );
		if(core==null)
		{
			core = new RecordCore(cached ? new CachedJDBCRecordStore(dbConnection ): new SimpleJDBCRecordStore(dbConnection ));
			cores.put( cat, core );
		}
		return core;
	}
	
	/**
	 * @param name
	 * @return the existing or newly created RecordCore
	 */
	public static RecordCore newMemoryStore(String name)
	{
		RecordCore core = cores.get( name );
		if(core==null)
		{
			core = new RecordCore(new MapRecordStore());
			cores.put( name, core );
		}
		return core;
	}
	
	/**
	 * @param name
	 * @param store
	 * @return a new or existing RecordCore on top of the given RecordStore
	 */
	public static RecordCore fromStore(String name, RecordStore store)
	{
		RecordCore core = cores.get( name );
		if(core==null)
		{
			core = new RecordCore(store );
			cores.put( name, core );
		}
		return core;
	}
	
	/**
	 * @param name
	 * @return the existing RecordCore or <code>null</code>
	 */
	public static RecordCore getCore(String name)
	{
		return cores.get( name );
	}

	@Override
	public void close() throws Exception
	{
		store.close();
	}
	
	/**
	 * @return whether the underlying store maintains a cache
	 */
	public boolean isCached()
	{
		return store.isCached();
	}
	
	/**
	 * Creates a new base for this type if none already exists.
	 * @param <T>
	 * @param type
	 * @param handlers
	 * @return the base for this type
	 */
	public <T extends ActiveRecord> RecordBase<T> buildBase(Class<T> type, ProxyHandler... handlers)
	{
		RecordBase<T> base = ( RecordBase<T> ) bases.get( type );
		if(base==null)
		{
			if(type.isInterface())
			{
				base = new ProxyBase<T>(( Class<? extends T> ) Proxy.getProxyClass( type.getClassLoader(), type), type, mergeHandlers( type, handlers ), store, this);
			}
			else
			{
				base = new POJOBase<T>(type, this, store );
			}
			bases.put( type, base );
		}
		return base;
	}
	
	/**
	 * If the {@link #getHandlers() handlers} are set, this method will create a new base if none for this type exists.
	 * @param <T>
	 * @param type
	 * @return the base for this type or <code>null</code> if none was yet created
	 */
	public <T extends ActiveRecord> RecordBase<T> getBase(Class<T> type)
	{
		RecordBase<T> base = ( RecordBase<T> ) bases.get( type );
		if(base==null)
		{
			if(type.isInterface() && handlers!=null)
			{
				base = new ProxyBase<T>(( Class<? extends T> ) Proxy.getProxyClass( type.getClassLoader(), type), type, mergeHandlers( type, null ), store, this);
			}
			else if(!type.isInterface())
			{
				base = new POJOBase<T>(type, this, store );
			}
			if(base != null)
			{
				bases.put( type, base );
			}
		}
		return base;
	}
	
	private ProxyHandler[] mergeHandlers(Class<? extends ActiveRecord> type, ProxyHandler[] custom)
	{
		if(this.handlers==null)
		{
			return custom;
		}
		if(custom == null)
		{
			return this.handlers.get( type );
		}
		List<ProxyHandler> proxies = Arrays.asList( custom );
		if(handlers.containsKey( type ))
		{
			for(ProxyHandler h: handlers.get( type ))
			{
				if(!proxies.contains( h ))
				{
					proxies.add( h );
				}
			}
		}
		return proxies.toArray( new ProxyHandler[proxies.size()]);
	}

	/**
	 * @return the handlers
	 */
	public Map<Class<? extends ActiveRecord>, ProxyHandler[]> getHandlers()
	{
		return handlers;
	}

	/**
	 * @param handlers the handlers to set
	 */
	public void setHandlers(Map<Class<? extends ActiveRecord>, ProxyHandler[]> handlers )
	{
		this.handlers = handlers;
	}
}
