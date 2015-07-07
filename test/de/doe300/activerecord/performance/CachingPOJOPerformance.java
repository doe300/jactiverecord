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
package de.doe300.activerecord.performance;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.pojo.POJOBase;
import de.doe300.activerecord.record.association.AssociationHelper;

/**
 *
 * @author doe300
 */
public class CachingPOJOPerformance implements ProxyPerformance
{
	private final POJOBase<?> base;
	private final int primaryKey;
	
	private String cacheName;
	private ProxyPerformance cacheOther;

	public CachingPOJOPerformance(int primaryKey, POJOBase<?> base )
	{
		this.base = base;
		this.primaryKey = primaryKey;
	}

	@Override
	public String getName()
	{
		if(cacheName == null)
		{
			cacheName = base.getProperty( primaryKey, "name", String.class);
		}
		return cacheName;
	}

	@Override
	public void setName( String newName )
	{
		cacheName = newName;
		base.setProperty( primaryKey, "name", newName );
	}

	@Override
	public ProxyPerformance getOther()
	{
		if(cacheOther == null)
		{
			cacheOther = AssociationHelper.getBelongsTo( this, CachingPOJOPerformance.class, "other");
		}
		return cacheOther;
	}

	@Override
	public void setOther( ProxyPerformance other )
	{
		cacheOther = other;
		AssociationHelper.setBelongsTo( this, other, "other");
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
}
