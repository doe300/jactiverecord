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

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.lang.reflect.Method;

/**
 *
 * @author doe300
 */
public abstract class ValidationHandler implements ProxyHandler
{

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		return method.getDeclaringClass() == ValidatedRecord.class;
	}

	@Override
	public <T extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<T> handler,
			Method method, Object[] args ) throws IllegalArgumentException
	{
		try
		{
			if(method.equals( ValidatedRecord.class.getMethod( "isValid")))			
			{
				return isValid( record );
			}
			if(method.equals( ValidatedRecord.class.getMethod( "validate")))
			{
				validate( record );
			}
			throw new IllegalArgumentException("Method is not handled by this handler");
		}
		catch ( NoSuchMethodException | SecurityException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	public abstract boolean isValid(ActiveRecord record);
	
	public abstract void validate(ActiveRecord record) throws ValidationFailed;
}
