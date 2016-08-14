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

import de.doe300.activerecord.migration.Migration;
import de.doe300.activerecord.migration.constraints.IndexType;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.DBDriver;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.diagnostics.Diagnostics;
import de.doe300.activerecord.util.Pair;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author doe300
 * @since 0.5
 */
public enum MemoryDBDriver implements DBDriver
{
	/**
	 * The singleton instance
	 */
	INSTANCE;

	@Override
	public boolean isTypeSupported(final Class<?> javaType )
	{
		return true;
	}

	@Override
	public Migration createMigration(final Class<? extends ActiveRecord> recordType, final RecordStore store)
	{
		return new MemoryMigration((MemoryRecordStore) store, recordType);
	}

	@Override
	public Migration createMigration(Class<? extends ActiveRecord> recordType, String storeName, RecordStore store )
	{
		return new MemoryMigration((MemoryRecordStore) store, recordType, storeName);
	}
	
	@Override
	public Migration createMigration( String storeName, Map<String, Class<?>> columns, RecordStore store )
	{
		return new MemoryMigration((MemoryRecordStore)store, storeName, columns.entrySet().stream().map(
				(Map.Entry<String, Class<?>> e) -> 
						new MemoryColumn(e.getKey(), e.getValue())).collect( Collectors.toList()).toArray( new MemoryColumn[columns.size()]) ,
				ActiveRecord.DEFAULT_PRIMARY_COLUMN);
	}

	@Override
	public Migration createMigration( String storeName, Map<String, Class<?>> columns, Map<Set<String>, IndexType> indices, RecordStore store ) 
			throws UnsupportedOperationException
	{
		return createMigration( storeName, columns, store );
	}

	@Override
	public Diagnostics<Pair< String, Scope>> createDiagnostics( RecordStore store )
	{
		return new Diagnostics<Pair< String, Scope>>(store, (Pair< String, Scope> t, Long u) -> new MemoryQuery(( MemoryRecordStore ) store, t, t.getFirst(), u));
	}
}
