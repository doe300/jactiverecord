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
package de.doe300.activerecord;

import de.doe300.activerecord.jdbc.VendorSpecific;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.pojo.POJOBase;
import de.doe300.activerecord.pojo.SingleInheritanceBase;
import de.doe300.activerecord.proxy.ProxyBase;
import de.doe300.activerecord.proxy.handlers.CollectionHandler;
import de.doe300.activerecord.proxy.handlers.MapHandler;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.SingleTableInheritance;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryMigration;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import de.doe300.activerecord.record.validation.Validate;
import de.doe300.activerecord.record.validation.ValidatedRecord;
import de.doe300.activerecord.record.validation.Validates;
import de.doe300.activerecord.record.validation.ValidationHandler;
import java.util.Collection;

/**
 * Core class for the active record API
 * @author doe300
 */
public final class RecordCore implements AutoCloseable
{
	private final static Map<String, RecordCore> cores = Collections.synchronizedMap( new HashMap<String, RecordCore>(5));

	@Nonnull
	private final RecordStore store;
	private final Map<Class<? extends ActiveRecord>, RecordBase<?>> bases;
	private final Map<Class<? extends ActiveRecord>, Set<RecordListener>> recordListeners;

	private RecordCore(@Nonnull final RecordStore store)
	{
		this.store = store;
		this.bases = new HashMap<Class<? extends ActiveRecord>, RecordBase<?>>(10);
		this.recordListeners = Collections.synchronizedMap(new HashMap<>(10));
	}

	/**
	 * Returns an existing or a new RecordCore with the underlying database as source
	 * @param dbConnection
	 * @param cached whether the used {@link RecordStore} should be cached
	 * @return the existing or newly created RecordCore
	 * @throws SQLException
	 */
	@Nonnull
	public static RecordCore fromDatabase(@Nonnull final Connection dbConnection, final boolean cached) throws SQLException
	{
		final String cat = dbConnection.getCatalog();
		RecordCore core = RecordCore.cores.get( cat );
		if(core==null)
		{
			core = new RecordCore(cached ? new CachedJDBCRecordStore(dbConnection ): new SimpleJDBCRecordStore(dbConnection ));
			RecordCore.cores.put( cat, core );
			Logging.getLogger().info( "RecordCore", "registered new"+(cached? " cached ":" ")+"record-core for DB-connection: "+cat);
		}
		return core;
	}

	/**
	 * @param name
	 * @return the existing or newly created RecordCore
	 */
	@Nonnull
	public static RecordCore newMemoryStore(@Nonnull final String name)
	{
		RecordCore core = RecordCore.cores.get( name );
		if(core==null)
		{
			core = new RecordCore(new MemoryRecordStore());
			RecordCore.cores.put( name, core );
			Logging.getLogger().info( "RecordCore", "registered new record-core for memory-store: "+name);
		}
		return core;
	}

	/**
	 * @param name
	 * @param store
	 * @return a new or existing RecordCore on top of the given RecordStore
	 */
	@Nonnull
	public static RecordCore fromStore(@Nonnull final String name, @Nonnull final RecordStore store)
	{
		RecordCore core = RecordCore.cores.get( name );
		if(core==null)
		{
			core = new RecordCore(store );
			RecordCore.cores.put( name, core );
			Logging.getLogger().info( "RecordCore", "registered new record-core for record-store: "+name);
		}
		return core;
	}

	/**
	 * @param name
	 * @return the existing RecordCore or <code>null</code>
	 */
	@Nullable
	public static RecordCore getCore(@Nonnull final String name)
	{
		return RecordCore.cores.get( name );
	}

	@Override
	public void close() throws Exception
	{
		store.close();
		Logging.getLogger().info( "RecordCore", "RecordCore closed");
		//remove Core from mappings
		for(final Map.Entry<String, RecordCore> entry:RecordCore.cores.entrySet())
		{
			if(entry.getValue().equals( this ))
			{
				RecordCore.cores.remove( entry.getKey());
				return;
			}
		}
	}

	/**
	 * @return whether the underlying store maintains a cache
	 */
	public boolean isCached()
	{
		return store.isCached();
	}

	/**
	 * @return the underlying record-store
	 */
	@Nonnull
	public RecordStore getStore()
	{
		return store;
	}

	/**
	 * Creates a new base for this type if none already exists.
	 * @param <T>
	 * @param type
	 * @param additionalHandlers
	 * @return the base for this type
	 */
	@Nonnull
	public <T extends ActiveRecord> RecordBase<T> getBase(@Nonnull final Class<T> type, 
			@Nullable final ProxyHandler... additionalHandlers)
	{
		RecordBase<T> base = ( RecordBase<T> ) bases.get( type );
		if(base==null)
		{
			if(type.isInterface())
			{
				base = new ProxyBase<T>(Proxy.getProxyClass( type.getClassLoader(), type).asSubclass( type ), type, mergeHandlers( type, additionalHandlers ), store, this);
			}
			else if(type.isAnnotationPresent( SingleTableInheritance.class))
			{
				return new SingleInheritanceBase<T>(type, this, store );
			}
			else
			{
				base = new POJOBase<T>(type, this, store );
			}
			bases.put( type, base );
			Logging.getLogger().debug( "RecordCore", "Created new record-base for "+type.getCanonicalName());
		}
		return base;
	}

	private static ProxyHandler[] mergeHandlers(@Nonnull final Class<? extends ActiveRecord> type, @Nullable final ProxyHandler[] custom)
	{
		
		final List<ProxyHandler> proxies = new ArrayList<>(custom != null ? custom.length : 5);
		proxies.addAll( Arrays.asList( custom));
		//add handlers by default, e.g. ValidatedHandler if recordType is ValidatedRecord and annotated with Validate
		if(ValidatedRecord.class.isAssignableFrom( type ) && (type.isAnnotationPresent( Validate.class) || type.isAnnotationPresent( Validates.class)))
		{
			if(!proxies.stream().anyMatch( (ProxyHandler h) -> ValidationHandler.class.isAssignableFrom( h.getClass()) ))
			{
				proxies.add( new ValidationHandler());
			}
		}
		if(Collection.class.isAssignableFrom( type ))
		{
			if(!proxies.stream().anyMatch( (ProxyHandler h) -> CollectionHandler.class.isAssignableFrom( h.getClass()) ))
			{
				proxies.add( new CollectionHandler());
			}
		}
		if(Map.class.isAssignableFrom( type ))
		{
			if(!proxies.stream().anyMatch( (ProxyHandler h) -> MapHandler.class.isAssignableFrom( h.getClass()) ))
			{
				proxies.add( new MapHandler());
			}
		}
		return proxies.toArray( new ProxyHandler[proxies.size()]);
	}
	
	/**
	 * Adds a new RecordListener for this record-type
	 * @param recordType
	 * @param l
	 */
	public void addRecordListener(final Class<? extends ActiveRecord> recordType, final RecordListener l)
	{
		if(!this.recordListeners.containsKey( recordType))
		{
			this.recordListeners.put( recordType, new HashSet<>(5));
		}
		this.recordListeners.get( recordType).add( l );
	}

	/**
	 * Removes a record-listener for this record-type
	 * @param recordType
	 * @param l
	 */
	public void removeRecordListener(final Class<? extends ActiveRecord> recordType, final RecordListener l)
	{
		if(this.recordListeners.containsKey( recordType))
		{
			this.recordListeners.get( recordType).remove( l );
		}
	}

	/**
	 * Fires a RecordEvent to all RecordListeners registered to the base's {@link RecordBase#getRecordType() record-type}
	 * @param eventType
	 * @param base
	 * @param record
	 */
	public void fireRecordEvent(@Nonnull final RecordListener.RecordEvent eventType, @Nonnull final RecordBase<?> base,
		@Nonnull final ActiveRecord record)
	{
		if(recordListeners.containsKey( base.getRecordType()))
		{
			for(final RecordListener l: recordListeners.get( base.getRecordType()))
			{
				l.notifyRecordEvent( eventType, base, record );
			}
		}
	}

	/**
	 * Saves all records for all RecordBases in this core
	 */
	public void saveAllRecords()
	{
		for(final RecordBase<?> b : bases.values())
		{
			if(b.saveAll())
			{
				Logging.getLogger().debug( "RecordCore", "Records saved for: "+b.getRecordType().getSimpleName());
			}
		}
	}
	
	/**
	 * Creates the data-store for the given record-type
	 * @param recordType
	 * @return whether the data-store was created
	 * @throws java.sql.SQLException
	 * @sicne 0.4
	 */
	public boolean createTable(@Nonnull final Class<? extends ActiveRecord> recordType) throws SQLException
	{
		if(MemoryRecordStore.class.isInstance( store))
		{
			return new MemoryMigration((MemoryRecordStore)store, recordType, false).apply( null );
		}
		else if (SimpleJDBCRecordStore.class.isInstance( store) || store.getConnection() != null)
		{
			return new AutomaticMigration(recordType, false, VendorSpecific.guessDatabaseVendor( store.getConnection())).apply( store.getConnection());
		}
		throw new IllegalArgumentException("No automatic migration supported for this record-store: " + store.getClass());
	}
	
	/**
	 * Deletes the data-store for the given record-type
	 * @param recordType
	 * @return whether the data-store was deleted
	 * @throws java.sql.SQLException
	 * @since 0.4
	 */
	public boolean dropTable(@Nonnull final Class<? extends ActiveRecord> recordType) throws SQLException
	{
		if(MemoryRecordStore.class.isInstance( store))
		{
			return new MemoryMigration((MemoryRecordStore)store, recordType, false).revert(null );
		}
		else if (SimpleJDBCRecordStore.class.isInstance( store) || store.getConnection() != null)
		{
			return new AutomaticMigration(recordType, false, VendorSpecific.guessDatabaseVendor( store.getConnection())).revert(store.getConnection());
		}
		throw new IllegalArgumentException("No automatic migration supported for this record-store: " + store.getClass());
	}
}
