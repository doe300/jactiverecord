package de.doe300.activerecord.validation;

import de.doe300.activerecord.record.ActiveRecord;

/**
 * An {@link ActiveRecord} which runs validations on its attributes.
 * Both validation methods should use the same validation-algorithm
 * @author doe300
 */
public interface ValidatedRecord extends ActiveRecord
{
	/**
	 * @return whether this record is valid
	 */
	public default boolean isValid()
	{
		return false;
	}

	/**
	 * This method is called before {@link #save()}
	 * @throws ValidationFailed the validation-error
	 */
	public default void validate() throws ValidationFailed
	{
		throw new ValidationFailed(null, null, "Validation not implemented" );
	}
}
