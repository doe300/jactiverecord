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

package de.doe300.activerecord.jdbc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Maps a custom type to a type understood by the active JDBC-driver.
 *
 * @author doe300
 * @param <T> the type to map from/to
 * @since 0.9
 * @see DBMappable
 * @see TypeMappings
 */
public interface DBMapper<T>
{
	/**
	 * Converts the given DB-value to the mapped type
	 * @param dbValue the object read from the underlying data-store
	 * @return the converted/mapped object
	 * @throws IllegalArgumentException 
	 */
	@Nullable
	public T readFromDBValue(@Nullable final Object dbValue) throws IllegalArgumentException;
	
	/**
	 * Converts the given value to be stored in the underlying store
	 * @param userValue the value to be converted
	 * @param dbType the type to convert to
	 * @return the converted object
	 */
	@Nullable
	public Object toDBValue(@Nullable final T userValue, @Nonnull final Class<?> dbType);
	
	/**
	 * @return the mapped type
	 */
	@Nonnull
	public Class<T> getMappedType();
}
