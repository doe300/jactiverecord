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

package de.doe300.activerecord;

import javax.annotation.Nonnull;

import org.junit.Assert;

/**
 * Mixin adding assertion to throw exceptions
 * 
 * @author doe300
 * @since 0.8
 */
public interface AssertException
{
	public default void assertThrows(@Nonnull final Class<? extends Throwable> exceptionType, @Nonnull final Executable call)
	{
		try
		{
			call.call();
			Assert.fail( "Expected Exception not thrown: " + exceptionType.getCanonicalName());
		}
		catch(final Throwable e)
		{
			if(!exceptionType.isInstance( e))
			{
				Assert.fail("Unexpected Exception thrown: " + e.getClass().getCanonicalName());
			}
		}
	}

	public default void mayThrow(@Nonnull final Class<? extends Throwable> exceptionType, @Nonnull final Executable call)
	{
		try
		{
			call.call();
		}
		catch(final Throwable e)
		{
			if(!exceptionType.isInstance( e))
			{
				Assert.fail("Unexpected Exception thrown: " + e.getClass().getCanonicalName());
			}
		}
	}

	public interface Executable
	{
		public void call() throws Throwable;
	}
}
