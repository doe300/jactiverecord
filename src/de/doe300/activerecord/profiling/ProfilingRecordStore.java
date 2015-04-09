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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.RecordStore;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Wrapper around another record-store profiling all method-calls
 * @author doe300
 */
public class ProfilingRecordStore implements RecordStore
{
	private final RecordStore store;
	private final Map<String, Long> runtimes;
	private final Map<String, Integer> numberOfRuns;


	public ProfilingRecordStore( RecordStore store )
	{
		this.store = store;
		runtimes = new HashMap<>(20);
		numberOfRuns = new HashMap<>(20);
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
	
	private <T> T profile(final String name, final Supplier<T> sup)
	{
		increaseRuns( name );
		long time = System.nanoTime();
		T res = sup.get();
		increaseRuntime( name, System.nanoTime()- time);
		return res;
	}
	
	private void profile(final String name, Runnable run)
	{
		increaseRuns( name );
		long time = System.nanoTime();
		run.run();
		increaseRuntime( name, System.nanoTime()- time);
	}
	
	public void printStatistics()
	{
		System.err.flush();
		System.out.flush();
		System.out.printf( "%30s|%10s|%16s|%16s%n", "Method", "# of Runs", "Time (in ms)", "Time per run" );
		double totalTime = 0;
		for(Map.Entry<String, Integer> entry : numberOfRuns.entrySet())
		{
			double time = runtimes.get( entry.getKey())/1000_000.0;
			totalTime+= time;
			double timePerRun = time/entry.getValue();
			System.out.printf( "%30s|%10d|%16.3f|%16.3f%n", entry.getKey(), entry.getValue(), time, timePerRun );
		}
		System.out.printf( "Total Time (in ms): %10.3f%n", totalTime );
	}

	@Override
	public Connection getConnection()
	{
		return profile( "getConnection", () -> store.getConnection());
	}

	@Override
	public boolean exists( String tableName )
	{
		return profile( "exists", () -> store.exists( tableName ));
	}

	@Override
	public Set<String> getAllColumnNames( String tableName ) throws UnsupportedOperationException
	{
		return profile( "getAllColumnNames", () -> store.getAllColumnNames( tableName));
	}

	@Override
	public void setValue(RecordBase<?> base, int primaryKey, String name, Object value ) throws IllegalArgumentException
	{
		profile( "setValue", () -> store.setValue( base, primaryKey, name, value));
	}

	@Override
	public void setValues(RecordBase<?> base, int primaryKey, String[] names, Object[] values ) throws IllegalArgumentException
	{
		profile( "setValuesArray", () -> store.setValues( base, primaryKey, names, values));
	}

	@Override
	public void setValues(RecordBase<?> base, int primaryKey, Map<String, Object> values ) throws IllegalArgumentException
	{
		profile( "setValuesMap", () -> store.setValues( base, primaryKey, values ));
	}

	@Override
	public Object getValue(RecordBase<?> base, int primaryKey, String name ) throws IllegalArgumentException
	{
		return profile( "getValue", () -> store.getValue( base, primaryKey, name));
	}

	@Override
	public Map<String, Object> getValues(RecordBase<?> base, int primaryKey, String[] columns ) throws IllegalArgumentException
	{
		return profile( "getValuesMap", () -> store.getValues( base, primaryKey, columns));
	}

	@Override
	public Stream<Object> getValues( String tableName, String column, String condColumn, Object condValue ) throws
			IllegalArgumentException
	{
		return profile( "getValuesStream", () -> store.getValues( tableName, column, condColumn, condValue ));
	}

	@Override
	public boolean addRow( String tableName, String[] rows, Object[] values ) throws IllegalArgumentException
	{
		return profile( "addRow", () -> store.addRow( tableName, rows, values));
	}

	@Override
	public boolean removeRow( String tableName, Condition cond ) throws IllegalArgumentException
	{
		return profile( "removeRow", () -> store.removeRow( tableName, cond));
	}

	@Override
	public boolean save(RecordBase<?> base, int primaryKey )
	{
		return profile( "save", () -> store.save( base, primaryKey));
	}

	@Override
	public boolean saveAll(RecordBase<?> base )
	{
		return profile( "saveAll", () -> store.saveAll( base));
	}

	@Override
	public void clearCache(RecordBase<?> base, int primaryKey )
	{
		profile( "clearCache", () -> store.clearCache( base, primaryKey));
	}

	@Override
	public boolean isCached()
	{
		return profile( "isCached", () -> store.isCached());
	}

	@Override
	public int insertNewRecord(RecordBase<?> base, Map<String, Object> columnData )
	{
		return profile( "insertNewRecord", () -> store.insertNewRecord( base, columnData));
	}

	@Override
	public boolean isSynchronized(RecordBase<?> base, int primaryKey )
	{
		return profile( "isSynchronized", () -> store.isSynchronized( base, primaryKey));
	}

	@Override
	public boolean containsRecord(RecordBase<?> base, Integer primaryKey )
	{
		return profile( "containsRecord", () -> store.containsRecord( base, primaryKey));
	}

	@Override
	public void destroy(RecordBase<?> base, int primaryKey )
	{
		profile( "destroy", () -> store.destroy( base, primaryKey));
	}

	@Override
	public Map<String, Object> findFirstWithData(RecordBase<?> base, String[] columns, Scope scope )
	{
		return profile( "findFirstWithData", () -> store.findFirstWithData( base, columns, scope));
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData(RecordBase<?> base, String[] columns, Scope scope )
	{
		return profile( "streamAllWithData", () -> store.streamAllWithData( base, columns, scope));
	}

	@Override
	public void close() throws Exception
	{
		printStatistics();
		store.close();
	}

	//override even default methods
	
	@Override
	public void touch(RecordBase<?> base, int primaryKey)
	{
		profile( "touch", () -> store.touch( base, primaryKey));
	}
	
	@Override
	public Integer findFirst(RecordBase<?> base, Scope scope)
	{
		return profile( "findFirst", () -> store.findFirst( base, scope));
	}
	
	@Override
	public Set<Integer> findAll(RecordBase<?> base, Scope scope)
	{
		return profile( "findAll", () -> store.findAll( base, scope));
	}
	
	@Override
	public Stream<Integer> streamAll(RecordBase<?> base, Scope scope)
	{
		return profile( "streamAll", () -> store.streamAll( base, scope));
	}
	
	@Override
	public Map<Integer, Map<String, Object>> findAllWithData(RecordBase<?> base, String[] columns, Scope scope)
	{
		return profile( "findAllWithData", () -> store.findAllWithData( base, columns, scope));
	}
	
	@Override
	public int count(RecordBase<?> base, Condition condition)
	{
		return profile( "count", () -> store.count( base, condition));
	}
}
