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
package de.doe300.activerecord.record.validation;

/**
 * One single error in validation
 */
public class ValidationException extends RuntimeException
{
	private static final long serialVersionUID = 5985957524527711273L;
	private final String column;
	private final String description;
	private final Object value;

	/**
	 * @param column
	 * @param value
	 */
	public ValidationException( final String column, final Object value)
	{
		this(column,value,"");
	}

	/**
	 * @param column
	 * @param value
	 * @param description
	 */
	public ValidationException( final String column, final Object value, final String description )
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

	/**
	 * @return the composed validation-message
	 * @see Exception#getMessage() 
	 */
	@Override
	public String getMessage()
	{
		return "Validation failed for attribute '"+column+"' and value '"+value+"'"+(description!=null?": "+description:"");
	}
}
