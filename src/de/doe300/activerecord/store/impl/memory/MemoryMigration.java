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

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.record.ActiveRecord;

/**
 *
 * @author doe300
 * @since 0.3
 */
public class MemoryMigration extends AutomaticMigration
{
	private final MemoryRecordStore memoryStore;
	private MemoryColumn[] columns;
	private String tableName, primaryColumn;

	/**
	 * @param memoryStore
	 * @param recordType
	 */
	public MemoryMigration(@Nonnull final MemoryRecordStore memoryStore,
		@Nonnull final Class<? extends ActiveRecord> recordType)
	{
		super(recordType, null, new JDBCDriver());
		this.memoryStore = memoryStore;
		convertColumns();
	}

	/**
	 * @param memoryStore
	 * @param tableName
	 * @param columns
	 * @param primaryColumn
	 */
	public MemoryMigration(@Nonnull final MemoryRecordStore memoryStore, @Nonnull final String tableName, @Nonnull final MemoryColumn[] columns, @Nonnull final String primaryColumn)
	{
		super(null, null, new JDBCDriver());
		this.memoryStore = memoryStore;
		this.tableName = tableName;
		this.columns = columns;
		this.primaryColumn = primaryColumn;
	}

	private void convertColumns()
	{
		tableName = getTableName(recordType);
		final Map<String, String> sqlColumns = getColumnsFromModel( recordType );
		columns = new MemoryColumn[sqlColumns.size()];
		int index = 0;
		primaryColumn = null;
		for(final Map.Entry<String, String> column : sqlColumns.entrySet())
		{
			columns[index] = new MemoryColumn(column.getKey(), driver.getJavaType( column.getValue()));
			index++;
			if(column.getValue().contains( "PRIMARY"))
			{
				primaryColumn = column.getKey();
			}
		}
		if(primaryColumn == null)
		{
			throw new IllegalArgumentException("Couldn't determine primary key-column");
		}
	}

	@Override
	public boolean apply()
	{
		Logging.getLogger().info( recordType != null ? recordType.getSimpleName() : tableName, "Creating memory-table...");
		Logging.getLogger().info( recordType != null ? recordType.getSimpleName() : tableName, Arrays.toString( columns));
		return memoryStore.addTable( tableName, columns, primaryColumn );
	}

	@Override
	public boolean revert()
	{
		Logging.getLogger().info( recordType != null ? recordType.getSimpleName() : tableName, "Dropping memory-table...");
		return memoryStore.removeTable( tableName );
	}

	@Override
	public boolean update(final boolean dropColumns)
	{
		throw new UnsupportedOperationException( "Updating memory-tables is not supported." );
	}

}
