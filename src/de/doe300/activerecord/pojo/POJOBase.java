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
package de.doe300.activerecord.pojo;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.jdbc.TypeMappings;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;

/**
 * Any POJO-based ActiveRecord is required to have a constructor accepting the primaryKey (int) and this POJOBase as parameters
 * @author doe300
 * @param <T>
 */
public class POJOBase<T extends ActiveRecord> extends RecordBase<T>
{
	/**
	 * @param recordType
	 * @param core
	 * @param store
	 */
	public POJOBase(@Nonnull final Class<T> recordType, @Nonnull final RecordCore core,
		@Nonnull final RecordStore store)
	{
		super( recordType, core, store );
	}
	
	protected POJOBase(@Nonnull final POJOBase<T> origBase, @Nonnull final String shardTable)
	{
		super(origBase, shardTable );
	}

	@Override
	protected T createProxy( final int primaryKey, final boolean newRecord, final Map<String, Object> recordData ) throws RecordException
	{
		try
		{
			return recordType.getConstructor( Integer.TYPE, POJOBase.class).newInstance( primaryKey, this);
		}
		catch ( ReflectiveOperationException | SecurityException ex )
		{
			throw new RecordException(ex);
		}
	}

	@Override
	protected RecordBase<T> createShardBase( String shardTable )
	{
		return new POJOBase<T>(this, shardTable );
	}

	/**
	 * Sets the given property in the record-store
	 * @param primaryKey
	 * @param name
	 * @param value
	 */
	public void setProperty(final int primaryKey, @Nonnull final String name, @Nullable final Object value)
	{
		store.setValue( this, primaryKey, name, value );
	}

	/**
	 * @param <U>
	 *            the type of the return-value
	 * @param primaryKey
	 * @param name
	 * @param type
	 *            the expected type
	 * @return the value of the given type
	 * @throws ClassCastException
	 *             if the return-value could not be casted
	 * @see TypeMappings#coerceToType(Object, Class)
	 */
	@Nullable
	public <U> U getProperty(final int primaryKey, @Nonnull final String name, @Nonnull final Class<U> type)
		throws ClassCastException
	{
		final Object obj = store.getValue( this, primaryKey, name );
		return TypeMappings.coerceToType( obj, type );
	}
}
