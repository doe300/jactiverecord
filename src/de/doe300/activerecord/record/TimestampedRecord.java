package de.doe300.activerecord.record;

/**
 *
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
