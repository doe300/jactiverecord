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
package de.doe300.activerecord.store;

import de.doe300.activerecord.RecordBase;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.migration.Migration;
import de.doe300.activerecord.migration.constraints.IndexType;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Abstract super-type for all db-drivers
 *
 * @author doe300
 * @since 0.5
 * @see JDBCDriver
 */
@Immutable
public interface DBDriver
{
	/**
	 * This methods informs, whether the given java-type is supported be natively stored and read from this data-store
	 *
	 * @param javaType the java-type to check
	 * @return whether the given type is natively supported
	 */
	public boolean isTypeSupported(@Nonnull final Class<?> javaType);

	/**
	 * This method is used to create and return a implementation-specific
	 * migration to create/update/drop a data-store for the given record-type
	 *
	 * @param recordType
	 * @param store 
	 * @return a new automatic-migration for the given record-type
	 * @since 0.6
	 */
	@Nonnull
	public Migration createMigration(@Nonnull final Class<? extends ActiveRecord> recordType, @Nonnull final RecordStore store);
	
	/**
	 * This method is used to create and return a implementation-specific
	 * migration to create/update/drop a data-store for the given record-type.
	 * 
	 * NOTE: This method can be used to create migrations for {@link RecordBase#getShardBase(java.lang.String) shards}
	 *
	 * @param recordType
	 * @param storeName the name of the record-store
	 * @param store 
	 * @return a new automatic-migration for the given record-type
	 * @since 0.7
	 * @see #createMigration(java.lang.Class, de.doe300.activerecord.store.RecordStore) 
	 */
	@Nonnull
	public Migration createMigration(@Nonnull final Class<? extends ActiveRecord> recordType, @Nonnull final String storeName, @Nonnull final RecordStore store);
	
	/**
	 * Creates a new migration to create, update or drop a manually created table
	 * @param storeName the name of the record-store/table
	 * @param columns the names and types of the columns to create/drop/update
	 * @param store the record-store to apply this migration to
	 * @return a new migration with the given values
	 */
	@Nonnull
	public Migration createMigration(@Nonnull final String storeName, @Nonnull final Map<String, Class<?>> columns,
			 @Nonnull final RecordStore store);
	
	/**
	 * Creates a new migration which additionally changes the indices of a record-store
	 * @param storeName the name of the record-store/table
	 * @param columns the names and types of the columns to create/drop/update
	 * @param indices the indices (indexed attributes and type of index) to change
	 * @param store the record-store to apply this migration to
	 * @return the migration
	 * @throws UnsupportedOperationException if this method is not supported by this implementation
	 * @see #createMigration(java.lang.String, java.util.Map, de.doe300.activerecord.store.RecordStore) 
	 */
	@Nonnull
	public default Migration createMigration(@Nonnull final String storeName, @Nonnull final Map<String, Class<?>> columns, 
			@Nullable final Map<Set<String>, IndexType> indices,  @Nonnull final RecordStore store) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Operation not supported by default implementation");
	}
	
	/**
	 * Creates a completely manual migration specifying specific commands for the apply/update and revert actions
	 * @param applyCommand the command to execute for the apply-action
	 * @param updateCommand the command for the update-action
	 * @param revertCommand the command for the revert-method
	 * @param store the record-store to apply this migration to
	 * @return the newly created Migration
	 * @throws UnsupportedOperationException if this method is not supported by this implementation
	 */
	@Nonnull
	public default Migration createMigration(@Nullable final String applyCommand, @Nullable final String updateCommand, 
			@Nullable final String revertCommand,  @Nonnull final RecordStore store) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Operation not supported by default implementation");
	}
}
