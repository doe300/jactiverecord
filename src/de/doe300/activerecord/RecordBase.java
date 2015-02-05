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

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.OrCondition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordCallbacks;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.validation.ValidatedRecord;
import de.doe300.activerecord.validation.ValidationFailed;

/**
 * Common base for mapped objects
 * @author doe300
 * @param <T>
 */
public abstract class RecordBase<T extends ActiveRecord> implements FinderMethods<T>
{
	protected final Class<T> recordType;
	protected final RecordCore core;
	protected final RecordStore store;
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
	public RecordBase( final Class<T> recordType, final RecordCore core, final RecordStore store )
	{
		this.recordType = recordType;
		this.core = core;
		this.store = store;
		this.records = new TreeMap<>();
	}

	/**
	 * @return the store
	 */
	public RecordStore getStore()
	{
		return store;
	}

	/**
	 * @return the core
	 */
	public RecordCore getCore()
	{
		return core;
	}

	/**
	 * @return the data-type of the records
	 */
	public Class<T> getRecordType()
	{
		return recordType;
	}

	/**
	 * The table name is retrieved from {@link RecordType#typeName() }.
	 * If this name is not set, the {@link Class#getSimpleName() simple-name} of the record-class is used.
	 * @return the tableName
	 * @see RecordType#typeName()
	 */
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
		}
		return tableName;
	}

	/**
	 * Uses the {@link RecordType#primaryKey() }. If this key is not set, {@link RecordStore#DEFAULT_COLUMN_ID id} is used.
	 * @return the name of the primary Column
	 * @see RecordType#primaryKey()
	 * @see RecordStore#DEFAULT_COLUMN_ID
	 */
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
				primaryColumn = RecordStore.DEFAULT_COLUMN_ID;
			}
		}
		return primaryColumn;
	}

	/**
	 * Looks up the default columns in {@link RecordType#defaultColumns() }.
	 * If this value is not set, the {@link RecordStore#DEFAULT_COLUMN_ID id} is set as only default column.
	 * @return the defaultColumns
	 * @see RecordType#defaultColumns()
	 */
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
				defaultColumns = new String[]{RecordStore.DEFAULT_COLUMN_ID};
			}
		}
		return defaultColumns;
	}

	/**
	 * The default order is looked up in {@link RecordType#defaultOrder() }. If
	 * this value is not set, the records are ordered by
	 * {@link #getPrimaryColumn() primary-key}
	 * {@link de.doe300.activerecord.dsl.Order.OrderType#ASCENDING ascending}.
	 * 
	 * @return the default ordering of records
	 * @see RecordType#defaultOrder()
	 * @see de.doe300.activerecord.dsl.Order.OrderType
	 */
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

	/**
	 * @param primaryKey
	 * @return the record, if it exists or <code>null</code>
	 * @throws RecordException
	 */
	public T getRecord(final int primaryKey) throws RecordException
	{
		T record=records.get( primaryKey );
		if(record==null && store.containsRecord( this, primaryKey))
		{
			record = createProxy(primaryKey);
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
	public T newRecord(final int primaryKey) throws RecordException
	{
		if(records.containsKey( primaryKey) || store.containsRecord(this, primaryKey))
		{
			throw new IllegalArgumentException("Record with primaryKey "+primaryKey+" already exists for table "+getTableName());
		}
		final T record = createProxy(primaryKey);
		records.put( primaryKey, record );
		if(hasCallbacks())
		{
			((RecordCallbacks)record).afterCreate();
		}
		return record;
	}

	/**
	 * Unlike {@link #newRecord(int)}, this method creates a new entry in the
	 * underlying record-store
	 * 
	 * @return the newly created record
	 * @throws RecordException
	 */
	public T createRecord() throws RecordException
	{
		final int key = store.insertNewRecord(this);
		final T record = createProxy(key);
		records.put( key, record );
		if(hasCallbacks())
		{
			((RecordCallbacks)record).afterCreate();
		}
		return record;
	}

	/**
	 * This method creates a new record in the underlying Database
	 * 
	 * @param data
	 * @return the newly created record
	 * @throws RecordException
	 * @see #createRecord()
	 */
	public T createRecord(final Map<String,Object> data) throws RecordException
	{
		final int key = store.insertNewRecord(this);
		final T record = createProxy(key);
		//just to make sure, no duplicate IDs are stored
		data.remove( getPrimaryColumn());
		store.setValues( this, key, data );
		records.put( key, record );
		if(hasCallbacks())
		{
			((RecordCallbacks)record).afterCreate();
		}
		return record;
	}

	/**
	 * @param primaryKey
	 * @return the proxy object mapped to the underlying record-store
	 */
	protected abstract T createProxy(int primaryKey) throws RecordException;

	/**
	 * @param primaryKey
	 * @return whether the record is stored in the underlying record-store
	 */
	public boolean hasRecord(final int primaryKey)
	{
		return getStore().containsRecord( this, primaryKey);
	}

	/**
	 * Saves all cached data to the record-store
	 * @return whether data was changed
	 * @throws ValidationFailed if the record-type is {@link ValidatedRecord} and any of the validations failed
	 */
	public boolean saveAll() throws ValidationFailed
	{
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
	public boolean save(final ActiveRecord record) throws ValidationFailed
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

	/**
	 * @param record
	 * @return whether the attributes of the record are in sync with the underlying store
	 */
	public boolean isSynchronized(final ActiveRecord record)
	{
		return store.isSynchronized(this, record.getPrimaryKey());
	}

	/**
	 * Discards all changes made to this record and reloads it from the database
	 * @param record
	 */
	public void reload(final ActiveRecord record)
	{
		store.clearCache(this,record.getPrimaryKey());
	}

	/**
	 * Removes the record with this primaryKey from the record-store and all cache
	 * @param primaryKey
	 */
	public void destroy(final int primaryKey)
	{
		final T record = records.remove( primaryKey);
		if(record != null && hasCallbacks())
		{
			((RecordCallbacks)record).onDestroy();
		}
		getStore().destroy(this, primaryKey );
	}

	/**
	 * Creates a duplicate of the given <code>record</code> which only differs in the primary key
	 * @param record
	 * @return the duplicate record
	 */
	public T duplicate(final T record)
	{
		return createRecord( store.getValues( this, record.getPrimaryKey(), store.getAllColumnNames( getTableName()).toArray( new String[0])) );
	}

	////
	// Finder-Methods
	////

	@Override
	public Stream<T> findWithScope(final Scope scope)
	{
		return getStore().findAll(this, scope ).stream().map( (final Integer i) ->
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
	public T findFirstWithScope(final Scope scope)
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

	/**
	 * @param condition
	 * @return the number of records matching these conditions
	 */
	public int count(final Condition condition)
	{
		return store.count( this, condition);
	}

	////
	// Query-Methods
	////

	/**
	 * @param condition
	 * @return the result for this query
	 */
	public QueryResult<T> where( final Condition condition )
	{
		return new QueryResult<T>(find( condition), count( condition ) ,getDefaultOrder());
	}
	
	/**
	 * 
	 * @param scope
	 * @return the result of this query
	 */
	public QueryResult<T> withScope(Scope scope)
	{
		return new QueryResult<T>(findWithScope( scope), Math.min(scope.getLimit(), count( scope.getCondition())), scope.getOrder()!= null ? scope.getOrder() : getDefaultOrder());
	}

	////
	// Searchable
	////

	/**
	 * @return whether this record is searchable
	 * @see Searchable
	 */
	public boolean isSearchable()
	{
		return recordType.isAnnotationPresent( Searchable.class);
	}

	/**
	 * @param term
	 * @return the matching records
	 * @throws UnsupportedOperationException if the record-type is not annotated with {@link Searchable}
	 * @see Searchable
	 */
	public Stream<T> search(final String term)
	{
		if(!isSearchable())
		{
			throw new UnsupportedOperationException("Called 'search' for non seachable record-type" );
		}
		final String[] columns = recordType.getAnnotation( Searchable.class).searchableColumns();
		return find( RecordBase.toSearchClause( term, columns ) );
	}

	/**
	 * @param term
	 * @return the first matching record
	 * @throws UnsupportedOperationException if the record-type is not annotated with {@link Searchable}
	 * @see Searchable
	 */
	public T searchFirst(final String term)
	{
		if(!isSearchable())
		{
			throw new UnsupportedOperationException("Called 'searchFirst' for non seachable record-type" );
		}
		final String[] columns = recordType.getAnnotation( Searchable.class).searchableColumns();
		return findFirst( RecordBase.toSearchClause( term, columns ) );
	}

	private static Condition toSearchClause(final String term, final String[] columns)
	{
		final Condition[] conds = new Condition[columns.length];
		for(int i=0;i<columns.length;i++)
		{
			conds[i] = new SimpleCondition(columns[i], "%"+term+"%", Comparison.LIKE);
		}
		return new OrCondition(conds );
	}

	////
	// TimestampedRecord
	////

	/**
	 * @return whether the record supports creation and update timestamps
	 */
	public boolean isTimestamped()
	{
		return TimestampedRecord.class.isAssignableFrom( recordType );
	}

	////
	//	ValidatedRecord
	////

	/**
	 * @return whether the record-type is {@link ValidatedRecord validated}
	 */
	public boolean isValidated()
	{
		return ValidatedRecord.class.isAssignableFrom( recordType);
	}

	/**
	 * A non-validated record type always returns true for {@link #isValid(de.doe300.activerecord.record.ActiveRecord) }
	 * @param record
	 * @return whether the record is valid
	 * @see ValidatedRecord#isValid()
	 */
	public boolean isValid(final ActiveRecord record)
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
	public void validate(final ActiveRecord record)
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
}
