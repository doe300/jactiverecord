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

package de.doe300.activerecord.store.diagnostics;

import de.doe300.activerecord.store.RecordStore;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A representation of an executed and logged query
 * 
 * @author doe300
 * @param <T> the type of the originating query
 * @since 0.8
 */
@Immutable
public abstract class Query<T>
{
	protected final RecordStore store;
	protected final T source;
	private final String storeName;
	private final long duration;

	/**
	 * @param store
	 * @param source the raw source, e.g. the executed SQL
	 * @param storeName
	 * @param duration the duration in milliseconds
	 */
	protected Query(@Nonnull final RecordStore store, @Nonnull final T source, @Nullable final String storeName, @Nonnegative final long duration )
	{
		this.store = store;
		this.source = source;
		this.storeName = storeName;
		this.duration = duration;
	}

	/**
	 * @return the source in the raw format
	 */
	@Nonnull
	public T getSource()
	{
		return source;
	}

	/**
	 * @return the duration in milliseconds
	 */
	@Nonnegative
	public long getDuration()
	{
		return duration;
	}
	
	/**
	 * @return the storeName
	 */
	@Nullable
	public String getStoreName()
	{
		return storeName;
	}
	
	/**
	 * Runs the storage-specific explain command and returns the result
	 * 
	 * @return the output of the explain command
	 * @throws java.lang.Exception
	 */
	@Nonnull
	//TODO needs improvement or drop?
	public abstract Iterable<String> explainQuery() throws Exception, UnsupportedOperationException;
	
	/**
	 * This method returns comments/remarks about performance or other issues extracted from the explain command
	 * @return a collection of remarks about the query
	 * @throws Exception 
	 */
	@Nonnull
	public abstract Iterable<QueryRemark<T>> getRemarks() throws Exception, UnsupportedOperationException;

}
