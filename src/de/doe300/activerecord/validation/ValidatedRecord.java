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
package de.doe300.activerecord.validation;

import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.record.ActiveRecord;

/**
 * An {@link ActiveRecord} which runs validations on its attributes.
 * Both validation methods should use the same validation-algorithm
 * @author doe300
 * @see Validate
 */
public interface ValidatedRecord extends ActiveRecord
{
	/**
	 * @return whether this record is valid
	 */
	public default boolean isValid()
	{
		Logging.getLogger().info( getBase().getRecordType().getSimpleName(), "Default implementation of isValid() called");
		return false;
	}

	/**
	 * This method is called before {@link #save()}
	 * @throws ValidationFailed the validation-error
	 */
	public default void validate() throws ValidationFailed
	{
		Logging.getLogger().info( getBase().getRecordType().getSimpleName(), "Default implementation of validate() called");
		throw new ValidationFailed(null, null, "Validation not implemented" );
	}
}