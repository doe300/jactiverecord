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
package de.doe300.activerecord.profiling;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author doe300
 */
public class Profiler
{
	private final Map<String, Integer> numberOfRuns;
	private final Map<String, Long> runtimes;

	public Profiler(int numberOfMethods)
	{
		this.numberOfRuns = new HashMap<>(numberOfMethods);
		this.runtimes = new HashMap<>(numberOfMethods);
	}
	
	/**
	 * 
	 * @param ignoreTrivial whether to ignore times under 0.1 ms per run
	 */
	public void printStatistics(boolean ignoreTrivial)
	{
		System.err.flush();
		System.out.flush();
		System.out.printf( "%30s|%10s|%16s|%16s%n", "Method", "# of Runs", "Time (in ms)", "Time per run" );
		double totalTime = 0;
		Set<Map.Entry<String, Integer>> runs = new TreeSet<>((Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) -> {
			int comp = -Integer.compare( e1.getValue(), e2.getValue());
			return comp != 0 ? comp : e1.getKey().compareTo( e2.getKey());
		});
		//sort by number of usages
		runs.addAll( numberOfRuns.entrySet());
		for(Map.Entry<String, Integer> entry : runs)
		{
			double time = runtimes.get( entry.getKey())/1000_000.0;
			totalTime+= time;
			double timePerRun = time/entry.getValue();
			if(!ignoreTrivial || timePerRun >= 0.1)
			{
				System.out.printf( "%30s|%10d|%16.3f|%16.3f%n", entry.getKey(), entry.getValue(), time, timePerRun );
			}
		}
		System.out.printf( "Total Time (in ms): %10.3f%n",totalTime );
	}
	
	private void increaseRuns(String name)
	{
		numberOfRuns.putIfAbsent( name, 0);
		numberOfRuns.put( name, numberOfRuns.get( name)+1);
	}
	
	private void increaseRuntime(String name, Long time)
	{
		runtimes.putIfAbsent( name, 0L);
		runtimes.put( name, runtimes.get( name)+time);
	}
	
//	public <T> T profile(final String name, final Supplier<T> sup)
//	{
//		increaseRuns( name );
//		long time = System.nanoTime();
//		T res = sup.get();
//		increaseRuntime( name, System.nanoTime()- time);
//		return res;
//	}
//	
//	public void profile(final String name, Runnable run)
//	{
//		increaseRuns( name );
//		long time = System.nanoTime();
//		run.run();
//		increaseRuntime( name, System.nanoTime()- time);
//	}
	
	public <T, E extends Throwable> T profile(final String name, final ThrowingSupplier<T, E> sup) throws E
	{
		increaseRuns( name );
		long time = System.nanoTime();
		T res = sup.get();
		increaseRuntime( name, System.nanoTime()- time);
		return res;
	}
	
	public <E extends Throwable> void profile(final String name, ThrowingRunnable<E> run) throws E
	{
		increaseRuns( name );
		long time = System.nanoTime();
		run.run();
		increaseRuntime( name, System.nanoTime()- time);
	}
	
	@FunctionalInterface
	public static interface ThrowingSupplier<T, E extends Throwable>
	{
		public T get() throws E;
	}
	
	@FunctionalInterface
	public static interface ThrowingRunnable<E extends Throwable>
	{
		public void run() throws E;
	}
}
