package de.doe300.activerecord.pojo;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;

/**
 * Any POJO-based ActiveRecord is required to have a constructor accepting the primaryKey (int) and this POJOBase as parameters
 * @author doe300
 * @param <T>
 */
public class POJOBase<T extends ActiveRecord> extends RecordBase<T>
{
	//TODO Test (run all, or at least most tests with TestInterface and TestPOJO)
	//Test Validations, Callbacks, Timestamps, ...
	//ToString, hashCode, ....
	
	public POJOBase( Class<T> recordType, RecordCore core, RecordStore store )
	{
		super( recordType, core, store );
	}

	@Override
	protected T createProxy( int primaryKey ) throws RecordException
	{
		try
		{
			return recordType.getConstructor( Integer.TYPE, POJOBase.class).newInstance( primaryKey, this);
		}
		catch ( ReflectiveOperationException | SecurityException ex )
		{
			throw new RecordException(ex);
		}
	}
	
	public void setProperty(int primaryKey, String name, Object value)
	{
		store.setValue( this, primaryKey, name, value );
	}
	
	public Object getProperty(int primaryKey, String name)
	{
		return store.getValue( this, primaryKey, name );
	}
}
