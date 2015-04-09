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
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Wrapper around another record-store profiling all method-calls
 * @author doe300
 */
public class ProfilingRecordStore implements RecordStore
{
	private final RecordStore store;
	private final Profiler profiler;


	public ProfilingRecordStore( RecordStore store )
	{
		this.store = store;
		this.profiler = new Profiler(30);
	}

	public Profiler getProfiler()
	{
		return profiler;
	}
	
	@Override
	public Connection getConnection()
	{
		return profiler.profile( "getConnection", () -> store.getConnection());
	}

	@Override
	public boolean exists( String tableName )
	{
		return profiler.profile( "exists", () -> store.exists( tableName ));
	}

	@Override
	public Set<String> getAllColumnNames( String tableName ) throws UnsupportedOperationException
	{
		return profiler.profile( "getAllColumnNames", () -> store.getAllColumnNames( tableName));
	}

	@Override
	public void setValue(RecordBase<?> base, int primaryKey, String name, Object value ) throws IllegalArgumentException
	{
		profiler.profile( "setValue", () -> store.setValue( base, primaryKey, name, value));
	}

	@Override
	public void setValues(RecordBase<?> base, int primaryKey, String[] names, Object[] values ) throws IllegalArgumentException
	{
		profiler.profile( "setValuesArray", () -> store.setValues( base, primaryKey, names, values));
	}

	@Override
	public void setValues(RecordBase<?> base, int primaryKey, Map<String, Object> values ) throws IllegalArgumentException
	{
		profiler.profile( "setValuesMap", () -> store.setValues( base, primaryKey, values ));
	}

	@Override
	public Object getValue(RecordBase<?> base, int primaryKey, String name ) throws IllegalArgumentException
	{
		return profiler.profile( "getValue", () -> store.getValue( base, primaryKey, name));
	}

	@Override
	public Map<String, Object> getValues(RecordBase<?> base, int primaryKey, String[] columns ) throws IllegalArgumentException
	{
		return profiler.profile( "getValuesMap", () -> store.getValues( base, primaryKey, columns));
	}

	@Override
	public Stream<Object> getValues( String tableName, String column, String condColumn, Object condValue ) throws
			IllegalArgumentException
	{
		return profiler.profile( "getValuesStream", () -> store.getValues( tableName, column, condColumn, condValue ));
	}

	@Override
	public boolean addRow( String tableName, String[] rows, Object[] values ) throws IllegalArgumentException
	{
		return profiler.profile( "addRow", () -> store.addRow( tableName, rows, values));
	}

	@Override
	public boolean removeRow( String tableName, Condition cond ) throws IllegalArgumentException
	{
		return profiler.profile( "removeRow", () -> store.removeRow( tableName, cond));
	}

	@Override
	public boolean save(RecordBase<?> base, int primaryKey )
	{
		return profiler.profile( "save", () -> store.save( base, primaryKey));
	}

	@Override
	public boolean saveAll(RecordBase<?> base )
	{
		return profiler.profile( "saveAll", () -> store.saveAll( base));
	}

	@Override
	public void clearCache(RecordBase<?> base, int primaryKey )
	{
		profiler.profile( "clearCache", () -> store.clearCache( base, primaryKey));
	}

	@Override
	public boolean isCached()
	{
		return profiler.profile( "isCached", () -> store.isCached());
	}

	@Override
	public int insertNewRecord(RecordBase<?> base, Map<String, Object> columnData )
	{
		return profiler.profile( "insertNewRecord", () -> store.insertNewRecord( base, columnData));
	}

	@Override
	public boolean isSynchronized(RecordBase<?> base, int primaryKey )
	{
		return profiler.profile( "isSynchronized", () -> store.isSynchronized( base, primaryKey));
	}

	@Override
	public boolean containsRecord(RecordBase<?> base, Integer primaryKey )
	{
		return profiler.profile( "containsRecord", () -> store.containsRecord( base, primaryKey));
	}

	@Override
	public void destroy(RecordBase<?> base, int primaryKey )
	{
		profiler.profile( "destroy", () -> store.destroy( base, primaryKey));
	}

	@Override
	public Map<String, Object> findFirstWithData(RecordBase<?> base, String[] columns, Scope scope )
	{
		return profiler.profile( "findFirstWithData", () -> store.findFirstWithData( base, columns, scope));
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData(RecordBase<?> base, String[] columns, Scope scope )
	{
		return profiler.profile( "streamAllWithData", () -> store.streamAllWithData( base, columns, scope));
	}

	@Override
	public void close() throws Exception
	{
		store.close();
	}

	//override even default methods
	
	@Override
	public void touch(RecordBase<?> base, int primaryKey)
	{
		profiler.profile( "touch", () -> store.touch( base, primaryKey));
	}
	
	@Override
	public Integer findFirst(RecordBase<?> base, Scope scope)
	{
		return profiler.profile( "findFirst", () -> store.findFirst( base, scope));
	}
	
	@Override
	public Set<Integer> findAll(RecordBase<?> base, Scope scope)
	{
		return profiler.profile( "findAll", () -> store.findAll( base, scope));
	}
	
	@Override
	public Stream<Integer> streamAll(RecordBase<?> base, Scope scope)
	{
		return profiler.profile( "streamAll", () -> store.streamAll( base, scope));
	}
	
	@Override
	public Map<Integer, Map<String, Object>> findAllWithData(RecordBase<?> base, String[] columns, Scope scope)
	{
		return profiler.profile( "findAllWithData", () -> store.findAllWithData( base, columns, scope));
	}
	
	@Override
	public int count(RecordBase<?> base, Condition condition)
	{
		return profiler.profile( "count", () -> store.count( base, condition));
	}
}
