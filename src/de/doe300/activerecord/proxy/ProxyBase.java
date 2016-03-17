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
package de.doe300.activerecord.proxy;

import java.lang.reflect.InvocationHandler;
import java.util.Map;

import javax.annotation.Nonnull;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;

/**
 * Base for one table and all its records
 * @author doe300
 * @param <T> the main-type of the ActiveRecord
 */
public final class ProxyBase<T extends ActiveRecord> extends RecordBase<T>
{
	private final Class<? extends T> proxyType;
	private final ProxyHandler[] proxyHandlers;

	/**
	 * Do not call this constructor!
	 * @param proxyType
	 * @param recordType
	 * @param proxyHandlers
	 * @param store
	 * @param core
	 */
	public ProxyBase(@Nonnull final Class<? extends T> proxyType, @Nonnull final Class<T> recordType,
		final ProxyHandler[] proxyHandlers, @Nonnull final RecordStore store, @Nonnull final RecordCore core)
	{
		super(recordType, core, store);
		this.proxyType = proxyType;
		this.proxyHandlers = proxyHandlers;
	}
	
	private ProxyBase(@Nonnull final ProxyBase<T> origBase, @Nonnull final String shardTable)
	{
		super(origBase, shardTable );
		this.proxyType = origBase.proxyType;
		this.proxyHandlers = origBase.proxyHandlers;
	}

	@Override
	protected T createProxy(final int primaryKey, final boolean newRecord, final Map<String, Object> recordData) throws RecordException
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

	@Override
	protected RecordBase<T> createShardBase( String shardTable )
	{
		return new ProxyBase<T>(this, shardTable);
	}
}
