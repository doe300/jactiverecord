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
