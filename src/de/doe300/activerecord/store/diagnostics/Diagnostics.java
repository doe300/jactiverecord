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
import de.doe300.activerecord.util.ThrowingFunctions.ThrowingRunnable;
import de.doe300.activerecord.util.ThrowingFunctions.ThrowingSupplier;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation-dependent diagnostics for {@link RecordStore record-stores} to analyze execution times and plans.
 * 
 * NOTE: Any diagnostics-method is allowed to be {@link UnsupportedOperationException unsupported}, if not supported by the underlying storage-engine
 * 
 * @author doe300
 * @param <T> the type of entries for the query-logs
 * @since 0.8
 */
public class Diagnostics<T>
{
	/**
	 * A value to disable the slow query timeout
	 */
	public static final long THRESHOLD_DISABLE = Long.MAX_VALUE;
	
	@Nonnull
	protected final RecordStore store;
	protected long slowQueryThreshold;
	protected final Deque<LoggedQuery<T>> slowQueryLog;
	private final BiFunction<T, Long, ? extends LoggedQuery<T>> logCreator;
	private SlowQueryListener listener;
	
	/**
	 * @param store 
	 * @param logCreator 
	 */
	public Diagnostics(@Nonnull final RecordStore store, @Nonnull final BiFunction<T, Long, ? extends LoggedQuery<T>> logCreator)
	{
		this.store = store;
		slowQueryThreshold = THRESHOLD_DISABLE;
		slowQueryLog = new LinkedBlockingDeque<>();
		this.logCreator = logCreator;
	}
	
	/**
	 * Sets the value of the slow-query-threshold. Any query taking longer than the defined threshold will be logged
	 * 
	 * @param timeout the timeout in milliseconds (ms)
	 */
	public void setSlowQueryThreshold(final long timeout)
	{
		slowQueryThreshold = timeout;
	}
	
	/**
	 * @return the threshold for logging slow queries, in milliseconds (ms)
	 */
	public long getSlowQueryThreshold()
	{
		return slowQueryThreshold;
	}
	
	/**
	 * @return whether the logging of slow queries is enabled
	 */
	public boolean isSlowQueryLogEnabled()
	{
		return slowQueryThreshold < THRESHOLD_DISABLE && slowQueryThreshold > 0;
	}
	
	public <R, E extends Exception> ThrowingSupplier<R, E> profileQuery(@Nonnull final ThrowingSupplier<R, E> sup, @Nonnull final Supplier<T> entry)
	{
		if(!isSlowQueryLogEnabled())
		{
			return sup;
		}
		return () -> 
		{
			final long start = System.currentTimeMillis();
			final R result = sup.get();
			final long end = System.currentTimeMillis();
			if((end - start) > slowQueryThreshold)
			{
				logSlowQuery( entry.get(), (end - start));
			}
			return result;
		};
	}
	
	public <E extends Exception> ThrowingRunnable<E> profileQuery(@Nonnull final ThrowingRunnable<E> run, @Nonnull final Supplier<T> entry)
	{
		if(!isSlowQueryLogEnabled())
		{
			return run;
		}
		return () -> 
		{
			final long start = System.currentTimeMillis();
			run.run();
			final long end = System.currentTimeMillis();
			if((end - start) > slowQueryThreshold)
			{
				logSlowQuery( entry.get(), (end - start));
			}
		};
	}
	
	/**
	 * Logs a query in the slow-query log
	 * @param entry the entry to log
	 * @param duration the duration of the execution, in milliseconds (ms)
	 */
	protected void logSlowQuery(@Nonnull final T entry, @Nonnegative final long duration)
	{
		final LoggedQuery<T> query = logCreator.apply( entry, duration );
		slowQueryLog.add( query);
		if(listener != null)
		{
			listener.onSlowQuery( query );
		}
	}
	
	/**
	 * Grants read-access to the slow query log, allowing to retrieve the slow queries in-order as well as
	 * accessing the latest entry.
	 * @return the slow-query log
	 */
	@Nonnull
	public Deque<LoggedQuery<T>> getSlowQueryLog()
	{
		return slowQueryLog;
	}

	/**
	 * @return the listener
	 */
	@Nullable
	public SlowQueryListener getSlowQueryListener()
	{
		return listener;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setSlowQueryListener(@Nullable final SlowQueryListener listener )
	{
		this.listener = listener;
	}
}
