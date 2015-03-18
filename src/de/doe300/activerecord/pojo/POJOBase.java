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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;

/**
 * Any POJO-based ActiveRecord is required to have a constructor accepting the primaryKey (int) and this POJOBase as parameters
 * @author doe300
 * @param <T>
 */
public class POJOBase<T extends ActiveRecord> extends RecordBase<T>
{
	//TODO Test (run all, or at least most tests with TestInterface and TestPOJO)
	//Test Validations, Callbacks, Timestamps, ...
	//ToString, hashCode, ....

	/**
	 * @param recordType
	 * @param core
	 * @param store
	 */
	public POJOBase( final Class<T> recordType, final RecordCore core, final RecordStore store )
	{
		super( recordType, core, store );
	}

	@Override
	protected T createProxy( final int primaryKey ) throws RecordException
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

	/**
	 * Sets the given property in the record-store
	 * @param primaryKey
	 * @param name
	 * @param value
	 */
	public void setProperty(final int primaryKey, final String name, final Object value)
	{
		store.setValue( this, primaryKey, name, value );
	}

	/**
	 * @param primaryKey
	 * @param name
	 * @return the value for the given property from the underlying store
	 */
	public Object getProperty(final int primaryKey, final String name)
	{
		return store.getValue( this, primaryKey, name );
	}
}
