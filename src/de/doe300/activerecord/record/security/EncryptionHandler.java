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
			if(method.equals( EncryptedRecord.class.getMethod( "encryptValue", String.class)))
			{
				return new String(algorithm.encryptValue( ((String)args[0]).getBytes()));
			}
			if(method.equals( EncryptedRecord.class.getMethod( "decryptValue", String.class)))
			{
				return new String(algorithm.decryptValue(((String)args[0]).getBytes()));
			}
			if(method.isAnnotationPresent( EncryptedAttribute.class))
			{
				//FIXME fails, because it contains non-printable characters
				String attributeName = method.getAnnotation( EncryptedAttribute.class).attribute();
				if(Attributes.isGetter( method, true ))
				{
					return new String(algorithm.decryptValue( ((String)record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), attributeName)).getBytes()));
				}
				if(Attributes.isSetter( method, args[0].getClass(), true ))
				{
					record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), attributeName, new String(algorithm.encryptValue( ((String)args[0]).getBytes())));
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
