package de.doe300.activerecord.record;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.store.RecordStore;

/**
 *
 * @author doe300
 */
public interface ActiveRecord extends Comparable<ActiveRecord>
{
	/**
	 * Saves all cached values to the record-store
	 * @return whether this record was saved and data was changed
	 * @see #isSynchronized() 
	 */
	public default boolean save()
	{
		return getBase().save( this );
	}
	
	/**
	 * @return the unique primary key for this record
	 */
	public int getPrimaryKey();
	
	/**
	 * @return whether the attributes in this object are synchronized with the underlying {@link RecordStore}
	 */
	public default boolean isSynchronized()
	{
		return getBase().hasRecord( getPrimaryKey()) && getBase().isSynchronized( this );
	}

	@Override
	public default int compareTo( ActiveRecord o )
	{
		return Integer.compare( getPrimaryKey(), o.getPrimaryKey());
	}

	/**
	 * @return the base for this record
	 */
	public RecordBase<?> getBase();
	
	/**
	 * Destroys this record and its storage-entity
	 */
	public default void destroy()
	{
		getBase().destroy( getPrimaryKey() );
	}
	
	/**
	 * @return whether this record is backed by an entry in the {@link RecordStore}
	 */
	public default boolean inRecordStore()
	{
		return getBase().hasRecord( getPrimaryKey());
	}
	
	/**
	 * Discards all changes made to this record so.
	 * @see #save()
	 * @see #isSynchronized() 
	 */
	public default void reload()
	{
		getBase().reload( this );
	}
}
