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
package de.doe300.activerecord.record.security;

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.attributes.Attributes;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import javax.annotation.Nonnull;

/**
 * RecordHandler for {@link EncryptedRecord}
 * @author doe300
 * @since 0.3
 */
public class EncryptionHandler implements ProxyHandler
{
	private final EncryptionAlgorithm algorithm;

	/**
	 * @param algorithm the underlying encryption-algorithm
	 */
	public EncryptionHandler(@Nonnull final EncryptionAlgorithm algorithm)
	{
		this.algorithm = algorithm;
	}

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		return record instanceof EncryptedRecord && 
				(method.getDeclaringClass() == EncryptedRecord.class || method.isAnnotationPresent( EncryptedAttribute.class));
	}

	@Override
	public <T extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<T> handler, Method method,
			Object[] args ) throws IllegalArgumentException
	{
		try
		{
			if(method.equals( EncryptedRecord.class.getMethod( "getEncryptionAlgorithm")))			
			{
				return algorithm;
			}
			//TODO currently both setter and getter must be annotated
			if(method.isAnnotationPresent( EncryptedAttribute.class))
			{
				String attributeName = method.getAnnotation( EncryptedAttribute.class).attribute();
				if(Attributes.isGetter( method, true ))
				{
					final Object dbValue = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), attributeName);
					final byte[] decryptedValue;
					if(dbValue == null)
					{
						return null;
					}
					else if(dbValue instanceof String)
					{
						decryptedValue = algorithm.decryptValue( ((String)dbValue).getBytes());
					}
					else if(dbValue.getClass() == byte[].class)
					{
						decryptedValue = algorithm.decryptValue( (byte[])dbValue );
					}
					else
					{
						throw new IllegalArgumentException("Illegal DB-type for encrypted value");
					}
					if(String.class.isAssignableFrom( method.getReturnType()))
					{
						if(decryptedValue == null)
						{
							return null;
						}
						//if we return as string, remove all trailing '\0' (zero-bytes) since they most likely are padding
						int lastIndex = decryptedValue.length;
						while(lastIndex > 0 && decryptedValue[lastIndex-1] == '\0')
						{
							--lastIndex;
						}
						return new String(decryptedValue, 0, lastIndex);
					}
					else if(method.getReturnType() == byte[].class)
					{
						return decryptedValue;
					}
					throw new IllegalArgumentException("Invalid return-type for encrypted getter");
				}
				if(Attributes.isSetter( method, null, true ))
				{
					final Class<?> dbType = record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( attributeName);
					if(dbType == null || !(String.class.isAssignableFrom( dbType ) || dbType == byte[].class))
					{
						throw new IllegalArgumentException("Illegal DB-type for encrypted value");
					}
					final Object dbValue;
					if(args[0] == null)
					{
						dbValue = null;
					}
					else if(args[0] instanceof String)
					{
						if(String.class.isAssignableFrom( dbType))
						{
							dbValue = new String(algorithm.encryptValue( ((String)args[0]).getBytes()));
						}
						else
						{
							dbValue = algorithm.encryptValue( ((String)args[0]).getBytes());
						}
					}
					else
					{
						if(String.class.isAssignableFrom( dbType))
						{
							dbValue = new String(algorithm.encryptValue( ((byte[])args[0])));
						}
						else
						{
							dbValue = algorithm.encryptValue( ((byte[])args[0]));
						}
					}
					record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), attributeName, dbValue);
					return null;
				}
			}
			throw new IllegalArgumentException("Method is not handled by this handler");
		}
		catch ( NoSuchMethodException | SecurityException | GeneralSecurityException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

}
