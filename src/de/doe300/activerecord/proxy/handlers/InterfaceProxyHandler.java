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
package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Base-class for dummy-objects implementing some interfaces used by an activerecord
 * @author doe300
 * @param <T>
 */
public abstract class InterfaceProxyHandler<T extends ActiveRecord> implements ProxyHandler
{
	private final Class<T> type;

	public InterfaceProxyHandler( Class<T> type)
	{
		this.type = type;
	}

	@Override
	public <U extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<U> handler,Method method, Object[] args ) throws IllegalArgumentException
	{
		try
		{
			return method.invoke( this, record, handler, args);
		}
		catch ( IllegalAccessException | InvocationTargetException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		for(Method m:type.getMethods())
		{
			if(m.getName().equals( method.getName()) && Arrays.equals( m.getParameterTypes(), getArgumentsTypes( args)))
			{
				return true;
			}
		}
		return false;
	}
	
	private static Class<?>[] getArgumentsTypes(Object... args)
	{
		Class<?>[] array = new Class<?>[args.length+2];
		array[0] = ActiveRecord.class;
		array[1] = RecordHandler.class;
		
		for(int i=0;i<args.length;i++)
		{
			array[i+2] = args.getClass();
		}
		
		return array;
	}
}
