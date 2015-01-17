package de.doe300.activerecord;

import de.doe300.activerecord.record.ActiveRecord;

/**
 * 
 * @author doe300
 */
public class RecordException extends RuntimeException
{
	private final ActiveRecord record;
	
	public RecordException( String message )
	{
		super( message );
		this.record = null;
	}

	public RecordException( Throwable cause )
	{
		this( (ActiveRecord)null, cause );
	}

	public RecordException( String message, Throwable cause )
	{
		this( null, message, cause );
	}

	public RecordException( ActiveRecord record, String message )
	{
		super( message );
		this.record = record;
	}

	public RecordException( ActiveRecord record, Throwable cause )
	{
		super( cause );
		this.record = record;
	}

	public RecordException( ActiveRecord record, String message, Throwable cause )
	{
		super( message, cause );
		this.record = record;
	}
	
	/**
	 * @return the causative record, may be <code>null</code>
	 */
	public ActiveRecord getRecord()
	{
		return record;
	}
}
