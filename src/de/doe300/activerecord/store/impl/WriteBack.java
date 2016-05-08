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

package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.util.Pair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Container for writing back single or multiple {@link RowCache caches}
 * 
 * @author doe300
 * @since 0.8
 * @see RowCache
 */
class WriteBack
{
	private final RecordBase<?> base;
	private final Map<Integer, Map<String, Object>> data;

	WriteBack(@Nonnull final RecordBase<?> base)
	{
		this.base = base;
		this.data = new HashMap<>(10);
	}
	
	synchronized void addRow(@Nonnegative final int primaryKey, @Nonnull final Map<String, Object> rowData)
	{
		data.put( primaryKey, new HashMap<>(rowData) );
	}
	
	@Nonnull
	synchronized Iterator<Pair<Integer, Map<String, Object>>> iterator()
	{
		return data.entrySet().stream().map(( Map.Entry<Integer, Map<String, Object>> t ) -> Pair.createPair( t.getKey(), t.getValue())).iterator();
	}

	/**
	 * @return the base
	 */
	@Nonnull
	public RecordBase<?> getBase()
	{
		return base;
	}
}
