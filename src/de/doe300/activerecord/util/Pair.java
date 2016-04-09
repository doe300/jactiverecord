/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 doe300
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
package de.doe300.activerecord.util;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author doe300
 * @param <T>
 * @param <U>
 * @since 0.7
 */
public class Pair<T, U>
{
	public static final Pair EMPTY = new Pair<>(null, null );
	
	private final T first;
	private final U second;

	private Pair(@Nullable final T first, @Nullable final  U second )
	{
		this.first = first;
		this.second = second;
	}
	
	@Nonnull
	public static <T,U> Pair<T,U> createPair(@Nullable final T first, @Nullable final  U second )
	{
		if(first == null && second == null)
		{
			return EMPTY;
		}
		return new Pair<>(first,second);
	}

	/**
	 * @return the first
	 */
	@Nullable
	public T getFirst()
	{
		return first;
	}

	/**
	 * @return the second
	 */
	@Nullable
	public U getSecond()
	{
		return second;
	}
	
	public boolean hasFirst()
	{
		return first != null;
	}
	
	public boolean hasSecond()
	{
		return second != null;
	}
	
	@Nonnull
	public T getFirstOrThrow()
	{
		return Objects.requireNonNull( first );
	}
	
	@Nonnull
	public U getSecondOrThrow()
	{
		return Objects.requireNonNull( second);
	}
}
