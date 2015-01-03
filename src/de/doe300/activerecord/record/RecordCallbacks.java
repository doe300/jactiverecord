package de.doe300.activerecord.record;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.validation.ValidatedRecord;

/**
 * Callbacks are executed at before/after specific actions
 * @author doe300
 */
public interface RecordCallbacks extends ActiveRecord
{
	/**
	 * This method is called right after a new record was created.
	 * Use this callback to initialize default-values.
	 * @see RecordBase#createRecord()
	 * @see RecordBase#createRecord(java.util.Map) 
	 * @see RecordBase#newRecord(int) 
	 */
	public default void afterCreate()
	{
		
	}
	
	/**
	 * This callback is called after the first load of a record (form the underlying record-store)
	 * @see RecordBase#getRecord(int) 
	 */
	public default void afterLoad()
	{
		
	}
	
	/**
	 * This callback is executed before saving a record.
	 * This callback is called before {@link ValidatedRecord#validate()}, if the record-type is {@link RecordBase#isValidated() validated}
	 * @see #save()
	 * @see RecordBase#save(de.doe300.activerecord.record.ActiveRecord) 
	 * @see RecordBase#saveAll() 
	 */
	public default void beforeSave()
	{
		
	}
	
	/**
	 * This callback is called before removing the record from the record-store.
	 * Purpose of this callback is i.e. to clear associated records or entries from association-tables
	 * @see #destroy() 
	 * @see RecordBase#destroy(int) 
	 */
	public default void onDestroy()
	{
		
	}
}
