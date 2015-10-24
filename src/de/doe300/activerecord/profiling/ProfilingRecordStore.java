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

import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.DBDriver;
import de.doe300.activerecord.store.RecordStore;

/**
 * Wrapper around another record-store profiling all method-calls
 * @author doe300
 */
public class ProfilingRecordStore implements RecordStore
{
	private final RecordStore store;
	private final Profiler profiler;


	public ProfilingRecordStore( final RecordStore store )
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
	public DBDriver getDriver()
	{
		return profiler.profile( "getDriver", () -> store.getDriver());
	}

	@Override
	public boolean exists( final String tableName )
	{
		return profiler.profileBoolean("exists", () -> store.exists(tableName));
	}

	@Override
	public Set<String> getAllColumnNames( final String tableName ) throws UnsupportedOperationException
	{
		return profiler.profile( "getAllColumnNames", () -> store.getAllColumnNames( tableName));
	}

	@Override
	public Map<String, Class<?>> getAllColumnTypes( String tablename ) throws IllegalArgumentException
	{
		return profiler.profile( "getAllColumnTypes", () -> store.getAllColumnTypes( tablename));
	}

	@Override
	public void setValue(final RecordBase<?> base, final int primaryKey, final String name, final Object value ) throws IllegalArgumentException
	{
		profiler.profile( "setValue", () -> store.setValue( base, primaryKey, name, value));
	}

	@Override
	public void setValues(final RecordBase<?> base, final int primaryKey, final String[] names, final Object[] values ) throws IllegalArgumentException
	{
		profiler.profile( "setValuesArray", () -> store.setValues( base, primaryKey, names, values));
	}

	@Override
	public void setValues(final RecordBase<?> base, final int primaryKey, final Map<String, Object> values ) throws IllegalArgumentException
	{
		profiler.profile( "setValuesMap", () -> store.setValues( base, primaryKey, values ));
	}

	@Override
	public Object getValue(final RecordBase<?> base, final int primaryKey, final String name ) throws IllegalArgumentException
	{
		return profiler.profile( "getValue", () -> store.getValue( base, primaryKey, name));
	}

	@Override
	public Map<String, Object> getValues(final RecordBase<?> base, final int primaryKey, final String[] columns ) throws IllegalArgumentException
	{
		return profiler.profile( "getValuesMap", () -> store.getValues( base, primaryKey, columns));
	}

	@Override
	public Stream<Object> getValues( final String tableName, final String column, final String condColumn, final Object condValue ) throws
	IllegalArgumentException
	{
		return profiler.profile( "getValuesStream", () -> store.getValues( tableName, column, condColumn, condValue ));
	}

	@Override
	public boolean addRow( final String tableName, final String[] rows, final Object[] values ) throws IllegalArgumentException
	{
		return profiler.profileBoolean("addRow", () -> store.addRow(tableName, rows, values));
	}

	@Override
	public boolean removeRow( final String tableName, final Condition cond ) throws IllegalArgumentException
	{
		return profiler.profileBoolean("removeRow", () -> store.removeRow(tableName, cond));
	}

	@Override
	public boolean save(final RecordBase<?> base, final int primaryKey )
	{
		return profiler.profileBoolean("save", () -> store.save(base, primaryKey));
	}

	@Override
	public boolean saveAll(final RecordBase<?> base )
	{
		return profiler.profileBoolean("saveAll", () -> store.saveAll(base));
	}

	@Override
	public void clearCache(final RecordBase<?> base, final int primaryKey )
	{
		profiler.profile( "clearCache", () -> store.clearCache( base, primaryKey));
	}

	@Override
	public boolean isCached()
	{
		return profiler.profileBoolean("isCached", () -> store.isCached());
	}

	@Override
	public int insertNewRecord(final RecordBase<?> base, final Map<String, Object> columnData )
	{
		return profiler.profileInt("insertNewRecord", () -> store.insertNewRecord(base, columnData));
	}

	@Override
	public boolean isSynchronized(final RecordBase<?> base, final int primaryKey )
	{
		return profiler.profileBoolean("isSynchronized", () -> store.isSynchronized(base, primaryKey));
	}

	@Override
	public boolean containsRecord(final RecordBase<?> base, final int primaryKey )
	{
		return profiler.profileBoolean("containsRecord", () -> store.containsRecord(base, primaryKey));
	}

	@Override
	public void destroy(final RecordBase<?> base, final int primaryKey )
	{
		profiler.profile( "destroy", () -> store.destroy( base, primaryKey));
	}

	@Override
	public Map<String, Object> findFirstWithData(final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		return profiler.profile( "findFirstWithData", () -> store.findFirstWithData( base, columns, scope));
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData(final RecordBase<?> base, final String[] columns, final Scope scope )
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
	public void touch(final RecordBase<?> base, final int primaryKey)
	{
		profiler.profile( "touch", () -> store.touch( base, primaryKey));
	}

	@Override
	public Integer findFirst(final RecordBase<?> base, final Scope scope)
	{
		return profiler.profile( "findFirst", () -> store.findFirst( base, scope));
	}

	@Override
	public Set<Integer> findAll(final RecordBase<?> base, final Scope scope)
	{
		return profiler.profile( "findAll", () -> store.findAll( base, scope));
	}

	@Override
	public Stream<Integer> streamAll(final RecordBase<?> base, final Scope scope)
	{
		return profiler.profile( "streamAll", () -> store.streamAll( base, scope));
	}

	@Override
	public Map<Integer, Map<String, Object>> findAllWithData(final RecordBase<?> base, final String[] columns, final Scope scope)
	{
		return profiler.profile( "findAllWithData", () -> store.findAllWithData( base, columns, scope));
	}

	@Override
	public int count(final RecordBase<?> base, final Condition condition)
	{
		return profiler.profileInt("count", () -> store.count(base, condition));
	}

	@Override
	public <R> R aggregate(RecordBase<?> base, AggregateFunction<?, ?, R> aggregateFunction, Condition condition )
	{
		return profiler.profile( "aggregate", () -> store.aggregate( base, aggregateFunction, condition));
	}
	
	
}
