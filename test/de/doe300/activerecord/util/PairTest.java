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

import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author doe300
 * @since 0.8
 */
public class PairTest extends Assert
{
	private static final Pair<String, String> firstNull = Pair.createPair(null, "Dummy");
	private static final Pair<String, String> secondNull = Pair.createPair("Dummy", null);
	
	public PairTest()
	{
	}

	@Test
	public void testGetFirst()
	{
		assertNull( firstNull.getFirst());
		assertNotNull( secondNull.getFirst());
	}

	@Test
	public void testGetSecond()
	{
		assertNotNull( firstNull.getSecond());
		assertNull( secondNull.getSecond());
	}

	@Test
	public void testHasFirst()
	{
		assertFalse( firstNull.hasFirst());
		assertTrue( secondNull.hasFirst());
	}

	@Test
	public void testHasSecond()
	{
		assertTrue( firstNull.hasSecond());
		assertFalse( secondNull.hasSecond());
	}

	@Test(expected = NullPointerException.class)
	public void testGetFirstOrThrow()
	{
		assertNotNull( secondNull.getFirstOrThrow());
		firstNull.getFirstOrThrow();
	}

	@Test(expected = NullPointerException.class)
	public void testGetSecondOrThrow()
	{
		assertNotNull( firstNull.getSecondOrThrow());
		secondNull.getSecondOrThrow();
	}

	@Test
	public void testEquals()
	{
		final Pair<String, String> testEq = Pair.createPair(firstNull.getFirst(), firstNull.getSecond());
		assertFalse( firstNull.equals( secondNull));
		assertTrue( firstNull.equals( testEq));
	}

}
