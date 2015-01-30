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

/**
 * The type of validation to run
 * @author doe300
 * @see Validate
 */
public enum ValidationType
{
	/**
	 * Runs the validation specified in {@link Validate#customClass() } and  {@link Validate#customMethod()}
	 */
	CUSTOM,
	/**
	 * Validates that the given value is <code>null</code>
	 * @see #NOT_NULL
	 */
	IS_NULL,
	/**
	 * Validates that the given value is empty.
	 * Validation for empty is currently only supported for String, Array, Collections and Maps.
	 * Additionally, any Number with the value 0 is considered empty.
	 * @see #NOT_EMPTY
	 */
	IS_EMPTY,
	/**
	 * Validates that the given value is not <code>null</code>
	 * @see #IS_NULL
	 */
	NOT_NULL,
	/**
	 * Validates that the given value is not empty.
	 * Validation for empty is currently only supported for String, Array, Collections and Maps
	 * Additionally, any Number with the value 0 is considered empty.
	 * @see #IS_EMPTY
	 */
	NOT_EMPTY,
	/**
	 * Validates that the value is a positive number
	 * @see #NEGATIVE
	 */
	POSITIVE,
	/**
	 * Validates the value to be a negative number
	 * @see #POSITIVE
	 */
	NEGATIVE
}
