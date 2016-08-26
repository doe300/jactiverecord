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

package de.doe300.activerecord.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Container for functional interfaces allowed to throw exceptions
 *
 * @author doe300
 * @since 0.8
 */
public interface ThrowingFunctions
{
	/**
	 * @param <E> the exception-type
	 * @see Runnable
	 */
	@FunctionalInterface
	public interface ThrowingRunnable<E extends Exception>
	{

		/**
		 *
		 * @throws ThrowingRunnable.E
		 */
		public void run() throws E;
	}
	
	/**
	 * @param <T> the consumed type
	 * @param <E> the exception-type
	 * @see Consumer
	 */
	@FunctionalInterface
	public interface ThrowingConsumer<T, E extends Exception>
	{

		/**
		 *
		 * @param value
		 * @throws ThrowingConsumer.E
		 */
		public void accept(T value) throws E;
	}
	
	/**
	 * @param <R> the return-type
	 * @param <E> the exception-type
	 * @see Supplier
	 */
	@FunctionalInterface
	public interface ThrowingSupplier<R, E extends Exception>
	{

		/**
		 *
		 * @return
		 * @throws ThrowingSupplier.E
		 */
		public R get() throws E;
	}
	
	/**
	 * 
	 * @param <T> the consumed type
	 * @param <R> the return-type
	 * @param <E> the exception-type
	 * @see Function
	 */
	@FunctionalInterface
	public interface ThrowingFunction<T, R, E extends Exception>
	{

		/**
		 *
		 * @param value
		 * @return
		 * @throws ThrowingFunction.E
		 */
		public R apply(T value) throws E;
	}
}
