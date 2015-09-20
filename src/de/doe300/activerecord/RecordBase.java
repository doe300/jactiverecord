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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.OrCondition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordCallbacks;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.association.ConditionSet;
import de.doe300.activerecord.record.association.RecordSet;
import de.doe300.activerecord.record.association.TableSet;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.validation.ValidatedRecord;
import de.doe300.activerecord.validation.ValidationFailed;
import javax.annotation.Nonnegative;

/**
 * Common base for mapped objects
 * @author doe300
 * @param <T>
 */
public abstract class RecordBase<T extends ActiveRecord> implements ReadOnlyRecordBase<T>
{
	@Nonnull
	protected final Class<T> recordType;
	@Nonnull
	protected final RecordCore core;
	@Nonnull
	protected final RecordStore store;
	@Nonnull
	protected final TreeMap<Integer, T> records;

	//caching variables
	private String[] defaultColumns;
	private String primaryColumn;
	private String tableName;
	private Order defaultOrder;

	/**
	 * @param recordType
	 * @param core
	 * @param store
	 */
	public RecordBase(@Nonnull final Class<T> recordType, @Nonnull final RecordCore core,
		@Nonnull final RecordStore store)
	{
		this.recordType = recordType;
		this.core = core;
		this.store = store;
		this.records = new TreeMap<>();
	}

	/**
	 * @return the store
	 */
	@Nonnull
	public RecordStore getStore()
	{
		return store;
	}

	/**
	 * @return the core
	 */
	@Nonnull
	public RecordCore getCore()
	{
		return core;
	}

	@Override
	public Class<T> getRecordType()
	{
		return recordType;
	}

	@Override
	public String getTableName()
	{
		if(tableName == null)
		{
			if(recordType.isAnnotationPresent(RecordType.class))
			{
				tableName = recordType.getAnnotation(RecordType.class).typeName();
			}
			else
			{
				tableName = recordType.getSimpleName();
			}
			Logging.getLogger().debug( recordType.getSimpleName(), "Using table-name: "+tableName);
		}
		return tableName;
	}

	@Override
	public String getPrimaryColumn()
	{
		if(primaryColumn==null)
		{
			if(recordType.isAnnotationPresent(RecordType.class))
			{
				primaryColumn =  recordType.getAnnotation(RecordType.class).primaryKey();
			}
			else
			{
				primaryColumn = ActiveRecord.DEFAULT_PRIMARY_COLUMN;
			}
			Logging.getLogger().debug( recordType.getSimpleName(), "Using primary-key: "+primaryColumn);
		}
		return primaryColumn;
	}

	/**
	 * Looks up the default columns in {@link RecordType#defaultColumns() }.
	 * If this value is not set, the {@link ActiveRecord#DEFAULT_PRIMARY_COLUMN id} is set as only default column.
	 * @return the defaultColumns
	 * @see RecordType#defaultColumns()
	 */
	@Nonnull
	public String[] getDefaultColumns()
	{
		if(defaultColumns==null)
		{
			if(recordType.isAnnotationPresent(RecordType.class))
			{
				defaultColumns =  recordType.getAnnotation(RecordType.class).defaultColumns();
			}
			else
			{
				defaultColumns = new String[]{ActiveRecord.DEFAULT_PRIMARY_COLUMN};
			}
			Logging.getLogger().debug( recordType.getSimpleName(), "Using default-columns: "+Arrays.toString(defaultColumns));
		}
		return defaultColumns;
	}

	@Override
	@Nonnull
	public Order getDefaultOrder()
	{
		if(defaultOrder == null)
		{
			if(recordType.isAnnotationPresent(RecordType.class))
			{
				defaultOrder = Order.fromSQLString(recordType.getAnnotation(RecordType.class).defaultOrder());
			}
			if(defaultOrder == null)
			{
				defaultOrder = new Order(getPrimaryColumn(), Order.OrderType.ASCENDING);
			}
			Logging.getLogger().debug( recordType.getSimpleName(), "Using default order: "+defaultOrder.toSQL(null, null));
		}
		return defaultOrder;
	}

	/**
	 * Any auto-created RecordBase creates the used table, if it doesn't exists.
	 * @return whether the table for this record-type is automatically created
	 * @see RecordType#autoCreate()
	 */
	public boolean isAutoCreate()
	{
		if(recordType.isAnnotationPresent( RecordType.class))
		{
			return recordType.getAnnotation( RecordType.class).autoCreate();
		}
		return false;
	}

	/**
	 * Two records are considered equal, if they are mapped to the same RecordBase and have the same <code>primary-key</code>
	 * @param record1
	 * @param record2
	 * @return whether the two records are equals
	 */
	public static boolean equals( final ActiveRecord record1, final ActiveRecord record2 )
	{
		if ( record1.getBase() == record2.getBase() )
		{
			return record1.getPrimaryKey() == record2.getPrimaryKey();
		}
		return false;
	}

	@Override
	public T getRecord(final int primaryKey) throws RecordException
	{
		T record=records.get( primaryKey );
		if(record==null && store.containsRecord( this, primaryKey))
		{
			record = createProxy(primaryKey, false, null);
			records.put( primaryKey, record );
			if(hasCallbacks())
			{
				((RecordCallbacks)record).afterLoad();
			}
		}
		return record;
	}

	/**
	 * Returns a new record which is not yet stored to the underlying
	 * record-store
	 *
	 * @param primaryKey
	 * @return the newly created record or <code>null</code>
	 * @throws RecordException
	 * @throws IllegalArgumentException
	 *             if the record with this ID already exists
	 */
	@Nullable
	public T newRecord(final int primaryKey) throws RecordException
	{
		if(records.containsKey( primaryKey) || store.containsRecord(this, primaryKey))
		{
			Logging.getLogger().error( recordType.getSimpleName(), "Record with primary-key "+primaryKey+" already exists");
			throw new IllegalArgumentException("Record with primaryKey "+primaryKey+" already exists for table "+getTableName());
		}
		final T record = createProxy(primaryKey, true, null);
		records.put( primaryKey, record );
		if(hasCallbacks())
		{
			((RecordCallbacks)record).afterCreate();
		}
		core.fireRecordEvent( RecordListener.RecordEvent.RECORD_CREATED, this, record );
		return record;
	}

	/**
	 * Unlike {@link #newRecord(int)}, this method creates a new entry in the
	 * underlying record-store
	 *
	 * @return the newly created record
	 * @throws RecordException
	 */
	@Nonnull
	public T createRecord() throws RecordException
	{
		return createRecord(null, null );
	}

	/**
	 * This method creates a new record in the underlying Database
	 *
	 * @param data
	 * @return the newly created record
	 * @throws RecordException
	 * @see #createRecord()
	 */
	@Nonnull
	public T createRecord(@Nullable final Map<String,Object> data) throws RecordException
	{
		return createRecord( data, null );
	}

	/**
	 * Unlike {@link #newRecord(int)}, this method creates a new entry in the
	 * underlying record-store.
	 * <p>
	 * If <code>onCreation</code> is not <code>null</code>, it will be called with the newly created record
	 * before any {@link RecordCallbacks#afterCreate() callbacks} or {@link RecordListener} are called.
	 * Thus providing the caller an opportunity to initialize additional values or associated records for the record.
	 * </p>
	 * @param data the row-data to set
	 * @param onCreation the callback, may be <code>null</code>
	 * @return the newly created record
	 * @throws RecordException
	 * @see #createRecord()
	 */
	@Nonnull
	public T createRecord(@Nullable final Map<String,Object> data, @Nullable final Consumer<T> onCreation) throws RecordException
	{
		if(data != null)
		{
			//just to make sure, ID is not overridden
			data.remove( getPrimaryColumn());
		}
		final int key = store.insertNewRecord(this, data);
		final T record = createProxy(key, true, data);
		records.put( key, record );
		if(onCreation != null)
		{
			onCreation.accept( record );
		}
		if(hasCallbacks())
		{
			((RecordCallbacks)record).afterCreate();
		}
		core.fireRecordEvent( RecordListener.RecordEvent.RECORD_CREATED, this, record );
		return record;
	}

	/**
	 * Note: it is not the job of the {@link #createProxy(int, boolean, java.util.Map) }-method to store the recordData.
	 * @param primaryKey
	 * @param newRecord whether the proxy is for a new record (<code>true</code>) or an existing one (<code>false</code>)
	 * @param recordData the data to create the record with, may be <code>null</code>
	 * @return the proxy object mapped to the underlying record-store
	 */
	@Nonnull
	protected abstract T createProxy(@Nonnegative int primaryKey, boolean newRecord, @Nullable Map<String, Object> recordData) throws RecordException;

	@Override
	public boolean hasRecord(final int primaryKey)
	{
		return getStore().containsRecord( this, primaryKey);
	}

	/**
	 * Saves all cached data to the record-store
	 * @return whether data was changed
	 * @throws ValidationFailed if the record-type is {@link ValidatedRecord} and any of the validations failed
	 */
	@CheckReturnValue
	public boolean saveAll() throws ValidationFailed
	{
		Logging.getLogger().debug( recordType.getSimpleName(), "Saving all records...");
		for(final T record : records.values())
		{
			if(hasCallbacks())
			{
				((RecordCallbacks)record).beforeSave();
			}
			if(isValidated())
			{
				((ValidatedRecord)record).validate();
			}
		}
		return store.saveAll(this);
	}

	/**
	 * @return whether the underlying record-store exists, i.e. the table in an SQL database
	 */
	public boolean recordStoreExists()
	{
		return store.exists(getTableName());
	}

	/**
	 * Saves the record to the record-store
	 * @param record
	 * @return whether data was changed
	 * @throws ValidationFailed if the record is {@link ValidatedRecord} and the validation failed
	 */
	@CheckReturnValue
	public boolean save(@Nonnull final ActiveRecord record) throws ValidationFailed
	{
		if(hasCallbacks())
		{
			((RecordCallbacks)record).beforeSave();
		}
		if(isValidated())
		{
			((ValidatedRecord)record).validate();
		}
		return store.save(this, record.getPrimaryKey() );
	}

	@Override
	public boolean isSynchronized(@Nonnull final ActiveRecord record)
	{
		return store.isSynchronized(this, record.getPrimaryKey());
	}

	/**
	 * Discards all changes made to this record and reloads it from the database
	 * @param record
	 */
	public void reload(@Nonnull final ActiveRecord record)
	{
		store.clearCache(this,record.getPrimaryKey());
	}

	/**
	 * Removes the record with this primaryKey from the record-store and all cache
	 * @param primaryKey
	 */
	public void destroy(@Nonnegative final int primaryKey)
	{
		final T record = records.remove( primaryKey);
		if (record != null)
		{
			if (hasCallbacks())
			{
				((RecordCallbacks) record).onDestroy();
			}
			core.fireRecordEvent(RecordListener.RecordEvent.RECORD_DESTROYED, this, record);
		}
		getStore().destroy(this, primaryKey );
	}

	/**
	 * Creates a duplicate of the given <code>record</code> which only differs in the primary key
	 * @param record
	 * @return the duplicate record
	 */
	@Nonnull
	public T duplicate(@Nonnull final T record)
	{
		final Collection<String> columnNames = store.getAllColumnNames( getTableName());
		final Map<String, Object> copyValues = store.getValues( this, record.getPrimaryKey(), columnNames.toArray( new String[columnNames.size()]));
		return createRecord( copyValues );
	}

	////
	// Finder-Methods
	////

	@Override
	@Nonnull
	public Stream<T> findWithScope(@Nonnull final Scope scope)
	{
		return getStore().streamAll(this, scope ).map( (final Integer i) ->
		{
			try
			{
				return getRecord( i);
			}
			catch ( final Exception ex )
			{
				return null;
			}
		}).filter( (final T t) -> t!= null);
	}

	@Override
	@Nullable
	public T findFirstWithScope(@Nonnull final Scope scope)
	{
		final Integer key = getStore().findFirst(this, scope );
		if(key!=null)
		{
			try
			{
				return getRecord(key );
			}
			catch ( final Exception ex )
			{
				return null;
			}
		}
		return null;
	}

	@Override
	public int count(@Nullable final Condition condition)
	{
		return store.count( this, condition);
	}

	////
	// Query-Methods
	////

	@Override
	@Nonnull
	public QueryResult<T> where(@Nullable final Condition condition )
	{
		return new QueryResult<T>(find( condition), count( condition ) ,getDefaultOrder());
	}

	@Override
	@Nonnull
	public QueryResult<T> withScope(@Nonnull final Scope scope)
	{
		return new QueryResult<T>(findWithScope( scope), Math.min(scope.getLimit(), count( scope.getCondition())), scope.getOrder()!= null ? scope.getOrder() : getDefaultOrder());
	}

	////
	// Searchable
	////

	@Override
	public boolean isSearchable()
	{
		return recordType.isAnnotationPresent( Searchable.class);
	}

	@Override
	@Nonnull
	public Stream<T> search(@Nonnull final String term)
	{
		if(!isSearchable())
		{
			Logging.getLogger().error( recordType.getSimpleName(), "record-type is not searchable");
			throw new UnsupportedOperationException("Called 'search' for non seachable record-type" );
		}
		final String[] columns = recordType.getAnnotation( Searchable.class).searchableColumns();
		return find( RecordBase.toSearchClause( term, columns ) );
	}

	@Override
	@Nullable
	public T searchFirst(@Nonnull final String term)
	{
		if(!isSearchable())
		{
			Logging.getLogger().error( recordType.getSimpleName(), "record-type is not searchable");
			throw new UnsupportedOperationException("Called 'searchFirst' for non seachable record-type" );
		}
		final String[] columns = recordType.getAnnotation( Searchable.class).searchableColumns();
		return findFirst( RecordBase.toSearchClause( term, columns ) );
	}

	private static Condition toSearchClause(@Nonnull final String term, @Nonnull final String[] columns)
	{
		final Condition[] conds = new Condition[columns.length];
		for(int i=0;i<columns.length;i++)
		{
			conds[i] = new SimpleCondition(columns[i], "%" + term + "%", Comparison.LIKE);
		}
		return OrCondition.orConditions(conds );
	}

	////
	// TimestampedRecord
	////

	@Override
	public boolean isTimestamped()
	{
		return TimestampedRecord.class.isAssignableFrom( recordType );
	}

	////
	//	ValidatedRecord
	////

	@Override
	public boolean isValidated()
	{
		return ValidatedRecord.class.isAssignableFrom( recordType);
	}

	@Override
	public boolean isValid(@Nonnull final ActiveRecord record)
	{
		if(isValidated())
		{
			return ((ValidatedRecord)record).isValid();
		}
		return true;
	}

	/**
	 * @param record
	 * @throws ValidationFailed if the validation failed
	 * @see ValidatedRecord#validate()
	 */
	public void validate(@Nonnull final ActiveRecord record)
	{
		if(isValidated())
		{
			((ValidatedRecord)record).validate();
		}
	}

	////
	//	Callbacks
	////

	/**
	 * @return whether this record-type has callbacks
	 * @see RecordCallbacks
	 */
	public boolean hasCallbacks()
	{
		return RecordCallbacks.class.isAssignableFrom( recordType);
	}

	////
	// Record-Sets
	////

	@Override
	@Nonnull
	public RecordSet<T> getAll()
	{
		return new TableSet<T>(this );
	}

	@Override
	@Nonnull
	public RecordSet<T> getForCondition(@Nullable final Condition cond)
	{
		return new ConditionSet<T>(this, cond, null, null );
	}
}
