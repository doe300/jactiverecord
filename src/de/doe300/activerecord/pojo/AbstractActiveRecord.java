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

import javax.annotation.Nonnull;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.record.ActiveRecord;
import javax.annotation.Nullable;

/**
 *
 * @author doe300
 */
public abstract class AbstractActiveRecord implements ActiveRecord
{
	protected final int primaryKey;
	@Nonnull
	private final POJOBase<?> base;

	/**
	 * @param primaryKey
	 * @param base
	 */
	public AbstractActiveRecord(final int primaryKey, @Nonnull final POJOBase<?> base)
	{
		this.primaryKey = primaryKey;
		this.base = base;
	}

	@Override
	public int getPrimaryKey()
	{
		return primaryKey;
	}

	@Override
	public RecordBase<?> getBase()
	{
		return base;
	}
	
	/**
	 * @param name the property-name
	 * @param value the property-value
	 */
	protected void setProperty(@Nonnull final String name, @Nullable final Object value)
	{
		base.setProperty( primaryKey, name, value );
	}
	
	protected <U> U getProperty(@Nonnull final String name, @Nonnull final Class<U> type) throws ClassCastException
	{
		return base.getProperty( primaryKey, name, type );
	}
}
