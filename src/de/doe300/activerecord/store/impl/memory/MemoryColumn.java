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
package de.doe300.activerecord.store.impl.memory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 *
 * @author doe300
 * @since 0.3
 */
@Immutable
public class MemoryColumn implements Comparable<MemoryColumn>
{
	private final String name;
	private final Class<?> type;

	/**
	 * @param name
	 * @param type
	 */
	public MemoryColumn(@Nonnull final String name, @Nonnull final Class<?> type )
	{
		this.name = name;
		this.type = type;
	}

	/**
	 * @param value
	 * @return the checked value
	 */
	@Nullable
	public Object checkValue(final Object value)
	{
		if(value == null)
		{
			return null;
		}
		if(type.isInstance( value ))
		{
			return value;
		}
		if(String.class.equals( type))
		{
			return value.toString();
		}
		throw new IllegalArgumentException("Invalid type: '"+value.getClass().getSimpleName()+"' is not "+type.getSimpleName());
	}

	/**
	 * @return the name
	 */
	@Nonnull
	public String getName()
	{
		return name;
	}

	/**
	 * @return the type
	 */
	@Nonnull
	public Class<?> getType()
	{
		return type;
	}

	@Override
	public int compareTo( final MemoryColumn o )
	{
		return getName().compareToIgnoreCase( o.getName());
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		return obj instanceof MemoryColumn && getName() == ((MemoryColumn)obj).getName() && getType() == ((MemoryColumn)obj).getType();
	}

	@Override
	public String toString()
	{
		return "MemoryColumn{"+name+": "+type.getSimpleName()+"}";
	}
}