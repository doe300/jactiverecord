/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.doe300.activerecord.store;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author doe300
 * @since 0.8
 */
public class NoSuchAttributeException extends IllegalArgumentException
{
	private static final long serialVersionUID = -2901926939394179548L;
	private final String dataSetName;
	private final String attributeName;

	/**
	 * @param dataSetName 
	 * @param attributeName 
	 */
    public NoSuchAttributeException(@Nonnull final String dataSetName, @Nonnull final String attributeName) {
        super("Data-set '" + dataSetName+"' has no such attribute: " + attributeName);
		this.dataSetName = dataSetName;
		this.attributeName = attributeName;
    }

	/**
	 * @param dataSetName
	 * @param attributeName
	 * @param cause 
	 */
	public NoSuchAttributeException(@Nonnull final String dataSetName, @Nonnull final String attributeName, @Nullable final Throwable cause)
	{
		super("Data-set '" + dataSetName+"' has no such attribute: " + attributeName, cause );
		this.dataSetName = dataSetName;
		this.attributeName = attributeName;
	}

	/**
	 * @return the dataSetName
	 */
	public String getDataSetName()
	{
		return dataSetName;
	}

	/**
	 * @return the attributeName
	 */
	public String getAttributeName()
	{
		return attributeName;
	}
	
}
