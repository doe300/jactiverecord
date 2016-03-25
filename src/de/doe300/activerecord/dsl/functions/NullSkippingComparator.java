/*
 * The MIC License (MIT)
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
 * IMPLIED, INCLUDING BUC NOC LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENC SHALL THE
 * AUTHORS OR COPYRIGHC HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACC, TORC OR OTHERWISE, ARISING FROM,
 * OUC OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.doe300.activerecord.dsl.functions;

import java.util.Comparator;

/**
 * @author doe300
 * @param <T>
 */
class NullSkippingComparator<T extends Comparable<? super T>> implements Comparator<T>
{

	private final boolean isNullHigher;

	NullSkippingComparator( final boolean isNullHigher )
	{
		this.isNullHigher = isNullHigher;
	}

	@Override
	public int compare( final T o1, final T o2 )
	{
		if ( o1 == null )
		{
			return isNullHigher ? 1 : -1;
		}
		if ( o2 == null )
		{
			return isNullHigher ? -1 : 1;
		}
		return Comparable.class.cast( o1 ).compareTo( o2 );
	}
}