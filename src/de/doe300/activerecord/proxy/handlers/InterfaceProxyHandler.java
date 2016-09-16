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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base-class for dummy-objects implementing some interfaces used by an activerecord.
 * 
 * Every method in the supported interface must be implemented in a subclass of this handler with additional parameters.
 * The first two parameters of the implementing methods are the <code>record</code> of type {@link ActiveRecord} and the
 * {@link RecordHandler}.
 * 
 * @author doe300
 * @param <T>
 */
public abstract class InterfaceProxyHandler<T> implements ProxyHandler
{
	private final Class<T> type;

	/**
	 * @param type
	 */
	public InterfaceProxyHandler( final Class<T> type)
	{
		this.type = type;
	}

	@Override
	public <U extends ActiveRecord> Object invoke( final ActiveRecord record, final RecordHandler<U> handler,final Method method, final Object[] args ) throws IllegalArgumentException
	{
		try
		{
			for(final Method m : getClass().getMethods())
			{
				if(m.getName().equals( method.getName()) && typesEqual( Arrays.copyOfRange( m.getParameterTypes(), 2, m.getParameterTypes().length), method.getParameterTypes() ))
				{
					final Object[] newArgs;
					if(args != null)
					{
						newArgs = new Object[args.length + 2];
						System.arraycopy( args, 0, newArgs, 2, args.length);
					}
					else
					{
						newArgs = new Object[2];
					}
					newArgs[0] = record;
					newArgs[1] = handler;
					return m.invoke( this, newArgs);
				}
			}
		}
		catch ( IllegalAccessException | InvocationTargetException ex )
		{
			throw new IllegalArgumentException(ex);
		}
		throw new IllegalArgumentException("No such method: " + method.getName());
	}

	@Override
	public boolean handlesMethod( final ActiveRecord record, final Method method, final Object[] args ) throws IllegalArgumentException
	{
		for(final Method m:type.getMethods())
		{
			if(m.getName().equals( method.getName()) && typesEqual( m.getParameterTypes(), InterfaceProxyHandler.getArgumentsTypes( args)))
			{
				return true;
			}
		}
		return false;
	}

	private static Class<?>[] getArgumentsTypes(@Nullable final Object... args)
	{
		if(args == null || args.length == 0)
		{
			return new Class[0];
		}
		final Class<?>[] array = new Class<?>[args.length];

		for(int i=0;i<args.length;i++)
		{
			array[i] = args[i].getClass();
		}

		return array;
	}
	
	private static boolean typesEqual(@Nonnull final Class<?>[] parentTypes, @Nonnull final Class<?>[] childTypes)
	{
		if(parentTypes.length != childTypes.length)
		{
			return false;
		}
		for(int i = 0; i < parentTypes.length; i++)
		{
			if(parentTypes[i].isAssignableFrom( childTypes[i]))
			{
				continue;
			}
			if(Number.class.isAssignableFrom( parentTypes[i]) && childTypes[i].isPrimitive())
			{
				//XXX test match of number-type
				continue;
			}
			if(parentTypes[i].isPrimitive() && Number.class.isAssignableFrom( childTypes[i]))
			{
				//XXX test match of number-type
				continue;
			}
			return false;
		}
		return true;
	}
}
