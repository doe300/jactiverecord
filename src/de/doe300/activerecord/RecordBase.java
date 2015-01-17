package de.doe300.activerecord;

import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.OrCondition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.RecordCallbacks;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.validation.ValidatedRecord;
import de.doe300.activerecord.validation.ValidationFailed;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

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

	public RecordBase( Class<T> recordType, RecordCore core, RecordStore store )
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
	 * @return the tableName
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
	 * @return the name of the primary Column
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
	 * @return the defaultColumns
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
	 * @param record1
	 * @param record2
	 * @return whether the two records are equals
	 */
	public static boolean equals( ActiveRecord record1, ActiveRecord record2 )
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
	 */
	public T getRecord(int primaryKey) throws RecordException
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
	 * Returns a new record which is not yet stored to the underlying record-store
	 * @param primaryKey
	 * @return the newly created record or <code>null</code>
	 * @throws IllegalArgumentException if the record with this ID already exists
	 */
	public T newRecord(int primaryKey) throws RecordException
	{
		if(records.containsKey( primaryKey) || store.containsRecord(this, primaryKey))
		{
			throw new IllegalArgumentException("Record with primaryKey "+primaryKey+" already exists for table "+getTableName());
		}
		T record = createProxy(primaryKey);
		records.put( primaryKey, record );
		if(hasCallbacks())
		{
			((RecordCallbacks)record).afterCreate();
		}
		return record;
	}
	
	/**
	 * Unlike {@link #newRecord(int)}, this method creates a new entry in the underlying record-store
	 * @return the newly created record
	 */
	public T createRecord() throws RecordException
	{
		int key = store.insertNewRecord(this);
		T record = createProxy(key);
		records.put( key, record );
		if(hasCallbacks())
		{
			((RecordCallbacks)record).afterCreate();
		}
		return record;
	}
	
	/**
	 * This method creates a new record in the underlying Database
	 * @param data
	 * @return the newly created record
	 * @see #createRecord() 
	 */
	public T createRecord(Map<String,Object> data) throws RecordException
	{
		int key = store.insertNewRecord(this);
		T record = createProxy(key);
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
	public boolean hasRecord(int primaryKey)
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
		for(T record : records.values())
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
	public boolean save(ActiveRecord record) throws ValidationFailed
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
	public boolean isSynchronized(ActiveRecord record)
	{
		return store.isSynchronized(this, record.getPrimaryKey());
	}
	
	/**
	 * Discards all changes made to this record and reloads it from the database
	 * @param record 
	 */
	public void reload(ActiveRecord record)
	{
		store.clearCache(this,record.getPrimaryKey());
	}
	
	/**
	 * Removes the record with this primaryKey from the record-store and all cache
	 * @param primaryKey 
	 */
	public void destroy(int primaryKey)
	{
		T record = records.remove( primaryKey);
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
	public T duplicate(T record)
	{
		return createRecord( store.getValues( this, record.getPrimaryKey(), store.getAllColumnNames( getTableName())) );
	}

	////
	// Finder-Methods
	////
	
	@Override
	public Stream<T> find(Condition condition)
	{
		return getStore().findAll(this, condition ).stream().map( (Integer i) ->
		{
			try
			{
				return getRecord( i);
			}
			catch ( Exception ex )
			{
				return null;
			}
		}).filter( (T t) -> t!= null);
	}
	
	@Override
	public T findFirst(Condition condition)
	{
		Integer key = getStore().findFirst(this, condition );
		if(key!=null)
		{
			try
			{
				return getRecord(key );
			}
			catch ( Exception ex )
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
	public int count(Condition condition)
	{
		return store.count( this, condition);
	}
	
	////
	// Query-Methods
	////
	
	public QueryResult<T> where( Condition condition )
	{
		return new QueryResult<T>(find( condition), count( condition ) ,getDefaultOrder());
	}
	
	////
	// Searchable
	////
	
	/**
	 * @return whether this record is searchable
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
	public Stream<T> search(String term)
	{
		if(!isSearchable())
		{
			throw new UnsupportedOperationException("Called 'search' for non seachable record-type" );
		}
		String[] columns = recordType.getAnnotation( Searchable.class).searchableColumns();
		return find( toSearchClause( term, columns ) );
	}
	
	/**
	 * @param term
	 * @return the first matching record
	 * @throws UnsupportedOperationException if the record-type is not annotated with {@link Searchable}
	 * @see Searchable
	 */
	public T searchFirst(String term)
	{
		if(!isSearchable())
		{
			throw new UnsupportedOperationException("Called 'searchFirst' for non seachable record-type" );
		}
		String[] columns = recordType.getAnnotation( Searchable.class).searchableColumns();
		return findFirst( toSearchClause( term, columns ) );
	}
	
	private static Condition toSearchClause(String term, String[] columns)
	{
		Condition[] conds = new Condition[columns.length];
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
	public boolean isValid(ActiveRecord record)
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
	public void validate(ActiveRecord record)
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
