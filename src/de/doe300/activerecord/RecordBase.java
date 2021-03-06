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

import de.doe300.activerecord.dsl.AggregateFunction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.dsl.Orders;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.dsl.functions.CastType;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
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
import de.doe300.activerecord.record.validation.ValidatedRecord;
import de.doe300.activerecord.record.validation.ValidationException;
import de.doe300.activerecord.store.JDBCRecordStore;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.function.Function;
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
	protected final SortedMap<Integer, T> records;
	@Nullable
	protected final SortedMap<String, RecordBase<T>> shards;

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
		this.records = Collections.synchronizedSortedMap( new TreeMap<>());
		this.shards = new TreeMap<>();
	}
	
	/**
	 * Constructor for creating table-shards
	 * @param origBase
	 * @param tableName
	 * @since 0.7
	 */
	protected RecordBase(@Nonnull final RecordBase<T> origBase, @Nonnull final String tableName)
	{
		this.recordType = origBase.recordType;
		this.core = origBase.core;
		this.store = origBase.store;
		this.tableName = tableName;
		this.records = Collections.synchronizedSortedMap( new TreeMap<>());
		//disallow creating shards from shards
		this.shards = null;
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
	
	/**
	 * @param shardTable the name of the table-shard
	 * @return the record-base for the shard
	 * @since 0.7
	 */
	@Nonnull
	public RecordBase<T> getShardBase(@Nonnull final String shardTable)
	{
		if(shards == null)
		{
			throw new IllegalStateException("Can't create table-shard from table-shard!");
		}
		if(!shards.containsKey( shardTable))
		{
			//TODO somehow, for Single inheritance base, new shard is created every time (for same base)
			//see output of RecordBaseTest
			Logging.getLogger().info( recordType.getSimpleName(), "Created new table-shard: " + shardTable);
			final RecordBase<T> shardBase = createShardBase( shardTable );
			shards.put( shardTable, shardBase);
		}
		return shards.get( shardTable);
	}
	
	/**
	 * @param shardTable
	 * @return a new instance of this record-base implementation for the table-shard
	 * @since 0.7
	 */
	@Nonnull
	protected abstract RecordBase<T> createShardBase(@Nonnull final String shardTable);

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
				defaultOrder = Orders.fromSQLString(recordType.getAnnotation(RecordType.class).defaultOrder());
			}
			if(defaultOrder == null)
			{
				defaultOrder = Orders.sortAscending( getPrimaryColumn());
			}
			Logging.getLogger().debug( recordType.getSimpleName(), "Using default order: "+defaultOrder.toSQL(JDBCDriver.DEFAULT, null));
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
	public static boolean equals( @Nullable final ActiveRecord record1, @Nullable final ActiveRecord record2 )
	{
		if(record1 == null || record2 == null)
		{
			return false;
		}
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
	 * This method creates a new entry in the underlying record-store
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
	 * This method creates a new entry in the underlying record-store.
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
			try
			{
				onCreation.accept( record );
			}
			catch(final Exception e)
			{
				record.destroy();
				throw new RecordException(e);
			}
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

	/**
	 * @param primaryKey
	 * @return whether the record is stored in the underlying {@link RecordStore}
	 */
	@Override
	public boolean hasRecord(final int primaryKey)
	{
		return getStore().containsRecord( this, primaryKey);
	}

	/**
	 * Saves all cached data to the record-store
	 * @return whether data was changed
	 * @throws ValidationException if the record-type is {@link ValidatedRecord} and any of the validations failed
	 */
	@CheckReturnValue
	public boolean saveAll() throws ValidationException
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
	 * @throws ValidationException if the record is {@link ValidatedRecord} and the validation failed
	 */
	@CheckReturnValue
	public boolean save(@Nonnull final ActiveRecord record) throws ValidationException
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
	
	/**
	 * Clears the stored record-objects. 
	 * Use this method to clean up memory after requiring a large amount of records from this record.
	 * Any subsequent calls to this method must re-create the records from the storage-data.
	 * 
	 * NOTE: This method does not delete the entries from the storage, but simply discards the internal cache of record-objects.
	 * 
	 * WARNING: Using this method may result in duplicate record-object for the same storage-entry! 
	 * Make sure, there are no active record-objects for this record-type when using this method!
	 * 
	 * @since 0.9
	 */
	public void clearRecords()
	{
		records.clear();
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

	@Override
	public <C, R> R aggregate( AggregateFunction<T, C, ?, R> aggregateFunction, @Nullable final Condition condition )
	{
		return getStore().aggregate( this, aggregateFunction, condition);
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
		return find( toSearchClause( term, columns ) );
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
		return findFirst( toSearchClause( term, columns ) );
	}

	private Condition toSearchClause(@Nonnull final String term, @Nonnull final String[] columns)
	{
		final Map<String, Class<?>> columnTypes = store.getAllColumnTypes( getTableName());
		final Condition[] conds = new Condition[columns.length];
		for(int i=0;i<columns.length;i++)
		{
			final String column = columns[i];
			if(store instanceof JDBCRecordStore && !CharSequence.class.isAssignableFrom( columnTypes.get( column.toLowerCase())))
			{
				//search for non character-based columns
				Logging.getLogger().debug( recordType.getSimpleName(), "Converting column '"+column+"' to string-type for searching");
				final Function<T, ?> valueFunc = (final T record) -> getStore().getValue( this, record.getPrimaryKey(), column);
				//XXX converter-function #toString() is not very specific -> improve. Is it even used?
				conds[i] = Conditions.is( new CastType<>(column, valueFunc, String.class, Object::toString), "%" + term + "%");
			}
			else
			{
				conds[i] = Conditions.isLike( column, "%" + term + "%");
			}
		}
		return Conditions.or(conds );
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
	 * @throws ValidationException if the validation failed
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
		return new TableSet<T>(this, getDefaultOrder() );
	}

	@Override
	@Nonnull
	public RecordSet<T> getForCondition(@Nullable final Condition cond, @Nullable final Order order)
	{
		return new ConditionSet<T>(this, cond, order, null, null );
	}

	@Override
	public boolean equals( Object o )
	{
		//two record-bases are the same if they represent the same table(shard) in the same core
		return (o instanceof RecordBase) 
				&& core.equals( ((RecordBase<?>)o).core)
				&& recordType.equals( ((RecordBase<?>)o).recordType)
				&& getTableName().equals( ((RecordBase<?>)o).getTableName());
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 17 * hash + Objects.hashCode( this.recordType );
		hash = 17 * hash + Objects.hashCode( this.core );
		hash = 17 * hash + Objects.hashCode( this.getTableName() );
		return hash;
	}
}