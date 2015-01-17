package de.doe300.activerecord.record;

import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.store.RecordStore;

/**
 * Timestamped ActiveRecords automatically maintain {@link RecordStore#COLUMN_CREATED_AT created_at} and  {@link RecordStore#COLUMN_UPDATED_AT updated_at}
 * values (of type java.sql.Timestamp). The <code>updated_at</code> will be updated every time, a attribute of the record is changed.
 * {@link AutomaticMigration} will generate this two columns automatically.
 * @author doe300
 */
public interface TimestampedRecord extends ActiveRecord
{
	/**
	 * @return the creation date of this entry
	 */
	public long getCreatedAt();
	
	/**
	 * @return the timestamp of the last update
	 */
	public long getUpdatedAt();
	
	/**
	 * Sets the {@link #getUpdatedAt()} timestamp to this instant
	 */
	public void touch();
}
