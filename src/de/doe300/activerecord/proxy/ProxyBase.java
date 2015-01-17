package de.doe300.activerecord.proxy;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;
import java.lang.reflect.InvocationHandler;

/**
 * Base for one table and all its records
 * @author doe300
 * @param <T> the main-type of the ActiveRecord
 */
public final class ProxyBase<T extends ActiveRecord> extends RecordBase<T>
{
	private final Class<? extends T> proxyType;
	private final ProxyHandler[] proxyHandlers;
	
	public ProxyBase(Class<? extends T> proxyType, Class<T> recordType, ProxyHandler[] proxyHandlers, RecordStore store, RecordCore core)
	{
		super(recordType, core, store);
		this.proxyType = proxyType;
		this.proxyHandlers = proxyHandlers;
	}
	
	@Override
	protected T createProxy(int primaryKey) throws RecordException
	{
		try
		{
			return proxyType.getConstructor( InvocationHandler.class).newInstance( new RecordHandler<T>(primaryKey, this, proxyHandlers ));
		}
		catch ( ReflectiveOperationException | SecurityException ex )
		{
			throw new RecordException(ex);
		}
	}
}
