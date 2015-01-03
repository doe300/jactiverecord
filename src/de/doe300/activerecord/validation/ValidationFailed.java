package de.doe300.activerecord.validation;

/**
 * One single error in validation
 */
public class ValidationFailed extends RuntimeException
{
	private final String column;
	private final String description;
	private final Object value;
	
	public ValidationFailed( String column, Object value)
	{
		this(column,value,"");
	}

	public ValidationFailed( String column, Object value, String description )
	{
		this.column = column;
		this.description = description;
		this.value = value;
	}

	/**
	 * @return the column the error is in
	 */
	public String getColumn()
	{
		return column;
	}

	/**
	 * @return an optional description of the error
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @return the erroneous value
	 */
	public Object getValue()
	{
		return value;
	}

	@Override
	public String getMessage()
	{
		return "Validation failed for attribute '"+column+"' and value '"+value+"'"+(description!=null?": "+description:"");
	}
}
