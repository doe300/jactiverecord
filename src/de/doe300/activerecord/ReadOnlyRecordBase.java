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
package de.doe300.activerecord;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.record.association.RecordSet;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.record.validation.ValidatedRecord;
import javax.annotation.Nonnegative;

/**
 * A RecordBase which can be read only
 * @author doe300
 * @param <T>
 */
public interface ReadOnlyRecordBase<T extends ActiveRecord> extends FinderMethods<T>, AggregateMethods<T>
{
	/**
	 * @return the data-type of the records
	 */
	@Nonnull
	public Class<T> getRecordType();

	/**
	 * The table name is retrieved from {@link RecordType#typeName() }.
	 * If this name is not set, the {@link Class#getSimpleName() simple-name} of the record-class is used.
	 * @return the tableName
	 * @see RecordType#typeName()
	 */
	@Nonnull
	public String getTableName();

	/**
	 * Uses the {@link RecordType#primaryKey() }. If this key is not set, {@link ActiveRecord#DEFAULT_PRIMARY_COLUMN id} is used.
	 * @return the name of the primary Column
	 * @see RecordType#primaryKey()
	 * @see ActiveRecord#DEFAULT_PRIMARY_COLUMN
	 */
	@Nonnull
	public String getPrimaryColumn();

	/**
	 * The default order is looked up in {@link RecordType#defaultOrder() }. If
	 * this value is not set, the records are ordered by
	 * {@link #getPrimaryColumn() primary-key}
	 * {@link de.doe300.activerecord.dsl.SimpleOrder.OrderType#ASCENDING ascending}.
	 *
	 * @return the default ordering of records
	 * @see RecordType#defaultOrder()
	 * @see de.doe300.activerecord.dsl.SimpleOrder.OrderType
	 */
	@Nonnull
	public Order getDefaultOrder();

	/**
	 * @param primaryKey
	 * @return the record, if it exists or <code>null</code>
	 * @throws RecordException
	 */
	@Nullable
	public T getRecord(@Nonnegative final int primaryKey) throws RecordException;

	/**
	 * @param primaryKey
	 * @return whether the record is stored in the underlying record-store
	 */
	public boolean hasRecord(@Nonnegative final int primaryKey);

	/**
	 * @param record
	 * @return whether the attributes of the record are in sync with the underlying store
	 */
	public boolean isSynchronized(@Nonnull final ActiveRecord record);

	/**
	 * @param condition
	 * @return the result for this query
	 */
	@Nonnull
	public QueryResult<T> where(@Nullable final Condition condition );

	/**
	 *
	 * @param scope
	 * @return the result of this query
	 */
	@Nonnull
	public QueryResult<T> withScope(@Nonnull final Scope scope);

	/**
	 * @return whether this record is searchable
	 * @see Searchable
	 */
	public boolean isSearchable();

	/**
	 * @param term
	 * @return the matching records
	 * @throws UnsupportedOperationException if the record-type is not annotated with {@link Searchable}
	 * @see Searchable
	 */
	@Nonnull
	public Stream<T> search(@Nonnull final String term);

	/**
	 * @param term
	 * @return the first matching record
	 * @throws UnsupportedOperationException if the record-type is not annotated with {@link Searchable}
	 * @see Searchable
	 */
	@Nullable
	public T searchFirst(@Nonnull final String term);

	/**
	 * @return whether the record supports creation and update timestamps
	 */
	public boolean isTimestamped();

	/**
	 * @return whether the record-type is {@link ValidatedRecord validated}
	 */
	public boolean isValidated();

	/**
	 * A non-validated record type always returns true for {@link #isValid(de.doe300.activerecord.record.ActiveRecord) }
	 * @param record
	 * @return whether the record is valid
	 * @see ValidatedRecord#isValid()
	 */
	public boolean isValid(@Nonnull final ActiveRecord record);

	/**
	 * NOTE: this result-set will be immutable
	 * @return a ResultSet containing all records in this base
	 */
	@Nonnull
	public RecordSet<T> getAll();

	/**
	 * NOTE: this result-set will be immutable
	 * @param cond
	 * @param order
	 * @return a ResultSet for the given condition and order
	 */
	@Nonnull
	public RecordSet<T> getForCondition(@Nullable final Condition cond, @Nullable final Order order);
}
