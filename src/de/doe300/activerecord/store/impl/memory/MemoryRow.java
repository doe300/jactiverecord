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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author doe300
 * @since 0.3
 */
class MemoryRow
{
	private final Map<String, Object> rowMap;

	MemoryRow(@Nonnegative int numColumns)
	{
		this.rowMap = new HashMap<>(numColumns);
	}

	MemoryRow(@Nonnegative int numColumns, @Nonnull final String[] rows, @Nonnull final Object[] rowData)
	{
		this.rowMap = new HashMap<>(numColumns);
		for(int i=0;i<rows.length; i++)
		{
			rowMap.put( rows[i], rowData[i]);
		}
	}

	MemoryRow(@Nonnegative int numColumns, @Nonnull final String primaryColumn, @Nonnegative final int primaryKey)
	{
		this.rowMap = new HashMap<>(numColumns);
		rowMap.put( primaryColumn, primaryKey);
	}
	
	public void putRowValue(@Nonnull final String column, @Nullable final Object value)
	{
		rowMap.put( column, value );
	}
	
	@Nullable
	public Object getRowValue(@Nonnull final String column)
	{
		return rowMap.get( column );
	}
	
	@Nonnull
	public Map<String, Object> getRowMap()
	{
		return rowMap;
	}
}
