/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.doe300.activerecord.pojo;

import de.doe300.activerecord.jdbc.TypeMappings;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * abstract active record which caches all columns values locally.
 * 
 * The cache of this record is write-through, so it is only used for read-access
 * 
 * @author doe300
 * @since 0.9
 */
public class CachingActiveRecord extends AbstractActiveRecord
{
	private final Map<String, Object> cache;

	/**
	 * 
	 * @param primaryKey
	 * @param base 
	 */
	public CachingActiveRecord( @Nonnegative int primaryKey, @Nonnull POJOBase<?> base )
	{
		super( primaryKey, base );
		cache = new HashMap<>(16);
	}

	@Override
	protected void setProperty(@Nonnull final String name, @Nullable final Object value)
	{
		super.setProperty( name, value );
		cache.put( name, value );
	}

	@Override
	protected <U> U getProperty(@Nonnull final String name, @Nonnull final Class<U> type) throws ClassCastException
	{
		if(cache.containsKey( name))
		{
			return TypeMappings.coerceToType( cache.get( name), type );
		}
		return super.getProperty( name, type );
	}
}
