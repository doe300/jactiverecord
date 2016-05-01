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
public class NoSuchDataSetException extends IllegalArgumentException
{
	private static final long serialVersionUID = -2901926939394179548L;
	private final String dataSetName;

	/**
	 * @param dataSetName 
	 */
    public NoSuchDataSetException(@Nonnull final String dataSetName) {
        super("No such data-set: " + dataSetName);
		this.dataSetName = dataSetName;
    }

	/**
	 * @param dataSetName
	 * @param cause 
	 */
	public NoSuchDataSetException(@Nonnull final String dataSetName, @Nullable final Throwable cause)
	{
		super("No such data-set: " + dataSetName, cause );
		this.dataSetName = dataSetName;
	}

	/**
	 * @return the dataSetName
	 */
	public String getDataSetName()
	{
		return dataSetName;
	}
	
}
