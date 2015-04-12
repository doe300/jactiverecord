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

import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.SingleTableInheritance;
import de.doe300.activerecord.store.RecordStore;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * The record-base for records using the single-table inheritance
 * @author doe300
 * @param <T>
 * @see SingleTableInheritance
 */
public class SingleInheritanceBase<T extends ActiveRecord> extends POJOBase<T>
{
	private final SingleTableInheritance inheritance;
	private final Method recordFactoryMethod;

	public SingleInheritanceBase( Class<T> recordType, RecordCore core, RecordStore store ) throws RecordException
	{
		super( recordType, core, store );
		this.inheritance = Objects.requireNonNull( recordType.getAnnotation( SingleTableInheritance.class));		
		this.recordFactoryMethod = getRecordFactoryMethod( inheritance );
	}
	
	private static Method getRecordFactoryMethod(SingleTableInheritance inheritance)
	{
		try
		{
			return inheritance.factoryClass().getMethod( inheritance.factoryMethod(), POJOBase.class, Integer.TYPE, Object.class);
		}
		catch ( NoSuchMethodException | SecurityException ex )
		{
			throw new RecordException("Failed to find factory-method", ex);
		}
	}

	@Override
	protected T createProxy( int primaryKey, boolean newRecord, Map<String, Object> recordData ) throws RecordException
	{
		if(newRecord && recordData == null)
		{
			//can't determine type - maybe super-type can create record
			return super.createProxy( primaryKey, newRecord, recordData );
		}
		Object typeKey = null;
		if(recordData != null)
		{
			//try to get type from recordData
			typeKey = recordData.get( inheritance.typeColumnName());
		}
		if(!newRecord && typeKey == null)
		{
			//no new record - read type from storage
			typeKey = getProperty( primaryKey, inheritance.typeColumnName());
		}
		if(typeKey == null)
		{
			//no type found - maybe the super-type can create the record
			return super.createProxy( primaryKey, newRecord, recordData );
		}
		try
		{
			return getRecordType().cast( recordFactoryMethod.invoke( null, new Object[]{this, primaryKey, typeKey}));
		}
		catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
		{
			throw new RecordException("Error while invoking factory-method", ex);
		}
	}
}
