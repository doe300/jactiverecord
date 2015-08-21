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

import java.util.Map;
import java.util.stream.Stream;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.validation.ValidationFailed;

/**
 * A wrapper around another RecordBase profiling all method-calls
 * @author doe300
 * @param <T>
 */
public class ProfilingRecordBase<T extends ActiveRecord> extends RecordBase<T>
{
	private final RecordBase<T> otherBase;
	private final Profiler profiler;

	public ProfilingRecordBase( final RecordBase<T> otherBase )
	{
		super( null, null, null );
		this.otherBase = otherBase;
		this.profiler = new Profiler(40);
	}

	public Profiler getProfiler()
	{
		return profiler;
	}

	@Override
	protected T createProxy( final int primaryKey, final boolean newRecord, final Map<String, Object> recordData ) throws RecordException
	{
		//not used
		throw new UnsupportedOperationException();
	}

	@Override
	public int count( final Condition condition )
	{
		return profiler.profileInt("count", () -> otherBase.count(condition));
	}

	@Override
	public T createRecord() throws RecordException
	{
		return profiler.profile("createRecord", () -> otherBase.createRecord());
	}

	@Override
	public T createRecord( final Map<String, Object> data ) throws RecordException
	{
		return profiler.profile("createRecord Map", () -> otherBase.createRecord( data ));
	}

	@Override
	public void destroy( final int primaryKey )
	{
		profiler.profile("destroy", () -> otherBase.destroy( primaryKey ));
	}

	@Override
	public T duplicate( final T record )
	{
		return profiler.profile("duplicate", () -> otherBase.duplicate( record ));
	}

	@Override
	public Stream<T> find( final Condition condition )
	{
		return profiler.profile("find", () -> otherBase.find( condition ));
	}

	@Override
	public Stream<T> findAll()
	{
		return profiler.profile("findAll", () -> otherBase.findAll());
	}

	@Override
	public T findFirst( final Condition condition )
	{
		return profiler.profile("findFirst", () -> otherBase.findFirst( condition ));
	}

	@Override
	public T findFirstFor( final Map<String, Object> data )
	{
		return profiler.profile("findFirstFor Map", () -> otherBase.findFirstFor( data ));
	}

	@Override
	public T findFirstFor( final String column, final Object value )
	{
		return profiler.profile("findFirstFor", () -> otherBase.findFirstFor( column, value ));
	}

	@Override
	public T findFirstWithScope( final Scope scope )
	{
		return profiler.profile("findFirstWithScope", () -> otherBase.findFirstWithScope( scope ));
	}

	@Override
	public Stream<T> findFor( final Map<String, Object> data )
	{
		return profiler.profile("findFor Map", () -> otherBase.findFor( data ));
	}

	@Override
	public Stream<T> findFor( final String column, final Object value )
	{
		return profiler.profile("findFor", () -> otherBase.findFor( column, value ));
	}

	@Override
	public Stream<T> findWithScope( final Scope scope )
	{
		return profiler.profile("findWithScope", () -> otherBase.findWithScope( scope ));
	}

	@Override
	public RecordCore getCore()
	{
		return profiler.profile("getCore", () -> otherBase.getCore());
	}

	@Override
	public String[] getDefaultColumns()
	{
		return profiler.profile("getDefaultColumns", () -> otherBase.getDefaultColumns());
	}

	@Override
	public Order getDefaultOrder()
	{
		return profiler.profile("getDefaultOrder", () -> otherBase.getDefaultOrder());
	}

	@Override
	public String getPrimaryColumn()
	{
		return profiler.profile("getPrimaryColumn", () -> otherBase.getPrimaryColumn());
	}

	@Override
	public T getRecord( final int primaryKey ) throws RecordException
	{
		return profiler.profile("getRecord", () -> otherBase.getRecord( primaryKey ));
	}

	@Override
	public Class<T> getRecordType()
	{
		return profiler.profile("getRecordType", () -> otherBase.getRecordType());
	}

	@Override
	public RecordStore getStore()
	{
		return profiler.profile("getStore", () -> otherBase.getStore());
	}

	@Override
	public String getTableName()
	{
		return profiler.profile("getTableName", () -> otherBase.getTableName());
	}

	@Override
	public boolean hasCallbacks()
	{
		return profiler.profileBoolean("hasCallbacks", () -> otherBase.hasCallbacks());
	}

	@Override
	public boolean hasRecord( final int primaryKey )
	{
		return profiler.profileBoolean("hasRecord", () -> otherBase.hasRecord(primaryKey));
	}

	@Override
	public boolean isAutoCreate()
	{
		return profiler.profileBoolean("isAutoCreate", () -> otherBase.isAutoCreate());
	}

	@Override
	public boolean isSearchable()
	{
		return profiler.profileBoolean("isSearchable", () -> otherBase.isSearchable());
	}

	@Override
	public boolean isSynchronized( final ActiveRecord record )
	{
		return profiler.profileBoolean("isSynchronized", () -> otherBase.isSynchronized(record));
	}

	@Override
	public boolean isTimestamped()
	{
		return profiler.profileBoolean("isTimestamped", () -> otherBase.isTimestamped());
	}

	@Override
	public boolean isValid( final ActiveRecord record )
	{
		return profiler.profileBoolean("isValid", () -> otherBase.isValid(record));
	}

	@Override
	public boolean isValidated()
	{
		return profiler.profileBoolean("isValidated", () -> otherBase.isValidated());
	}

	@Override
	public T newRecord( final int primaryKey ) throws RecordException
	{
		return profiler.profile("newRecord", ()-> otherBase.newRecord( primaryKey ));
	}

	@Override
	public boolean recordStoreExists()
	{
		return profiler.profileBoolean("recordStoreExists", () -> otherBase.recordStoreExists());
	}

	@Override
	public void reload( final ActiveRecord record )
	{
		profiler.profile("reload", () -> otherBase.reload( record ));
	}

	@Override
	public boolean save( final ActiveRecord record ) throws ValidationFailed
	{
		return profiler.profileBoolean("save", () -> otherBase.save(record));
	}

	@Override
	public Stream<T> search( final String term )
	{
		return profiler.profile("search", () -> otherBase.search( term ));
	}

	@Override
	public T searchFirst( final String term )
	{
		return profiler.profile("searchFirst", () -> otherBase.searchFirst( term ));
	}

	@Override
	public void validate( final ActiveRecord record )
	{
		profiler.profile("validate", () -> otherBase.validate( record ));
	}

	@Override
	public QueryResult<T> where( final Condition condition )
	{
		return profiler.profile("where", () -> otherBase.where( condition ));
	}

	@Override
	public QueryResult<T> withScope( final Scope scope )
	{
		return profiler.profile("withScope", () -> otherBase.withScope( scope ));
	}
}
