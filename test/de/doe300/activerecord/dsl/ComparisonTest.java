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
package de.doe300.activerecord.dsl;

import de.doe300.activerecord.AssertException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class ComparisonTest extends Assert implements AssertException
{
	public ComparisonTest()
	{
	}

	@Test
	public void testIS()
	{
		Object a = "a";
		assertTrue( Comparison.IS.test( a, a));
		assertFalse( Comparison.IS.test( a, "b"));
	}
	
	@Test
	public void testIS_NOT()
	{
		Object a = "a";
		assertTrue( Comparison.IS_NOT.test( a, "b"));
		assertFalse( Comparison.IS_NOT.test( a, a));
	}
	
	@Test
	public void testIS_NULL()
	{
		Object a = null;
		assertTrue( Comparison.IS_NULL.test( a, null));
		assertFalse( Comparison.IS_NULL.test( "b", null));
	}
	
	@Test
	public void testIS_NOT_NULL()
	{
		Object a = null;
		assertTrue( Comparison.IS_NOT_NULL.test( "b", null));
		assertFalse( Comparison.IS_NOT_NULL.test( a, null));
	}
	
	@Test
	public void testLIKE()
	{
		Object a = "Apfelsaft";
		assertTrue( Comparison.LIKE.test( a, "%saft"));
		assertTrue( Comparison.LIKE.test( a, "%el%af%"));
		assertFalse( Comparison.LIKE.test( a, "%Saft"));
		assertFalse( Comparison.LIKE.test( a, "Birne"));
		assertFalse( Comparison.LIKE.test( a, "Saftapfel"));
		assertFalse( Comparison.LIKE.test( a, null));
	}

	@Test
	public void testLARGER()
	{
		Object a = -5, b = 7;
		assertTrue( Comparison.LARGER.test( b, a));
		assertFalse( Comparison.LARGER.test( a, b));
		assertFalse( Comparison.LARGER.test( a, a ));

		assertThrows( ClassCastException.class, () ->Comparison.LARGER.test( "a", new String[0]));
	}

	@Test
	public void testSMALLER()
	{
		Object a = -5, b = 7;
		assertTrue( Comparison.SMALLER.test( a, b));
		assertFalse( Comparison.SMALLER.test( b, a));
		assertFalse( Comparison.SMALLER.test( a, a));

		assertThrows( ClassCastException.class, () -> Comparison.SMALLER.test( a, new int[]{7}));
	}

	@Test
	public void testLARGER_EQUALS()
	{
		Object a = -5, b = 7;
		assertTrue( Comparison.LARGER_EQUALS.test( b, a));
		assertTrue( Comparison.LARGER_EQUALS.test( a, a ));
		assertFalse( Comparison.LARGER_EQUALS.test( a, b));

		assertThrows( ClassCastException.class, () -> Comparison.LARGER_EQUALS.test( "a", new String[0]));
	}

	@Test
	public void testSMALLER_EQUALS()
	{
		Object a = -5, b = 7;
		assertTrue( Comparison.SMALLER_EQUALS.test( a, b));
		assertTrue( Comparison.SMALLER_EQUALS.test( a, a));
		assertFalse( Comparison.SMALLER_EQUALS.test( b, a));

		assertThrows( ClassCastException.class, () -> Comparison.SMALLER_EQUALS.test( a, new int[]{7}));
	}

	@Test
	public void testTRUE()
	{
		assertTrue( Comparison.TRUE.test( this, this));
		assertTrue( Comparison.TRUE.test( null, null));
	}

	@Test
	public void  testIN()
	{
		Object a = "a";
		assertTrue( Comparison.IN.test( a, new Object[]{a, "b"}));
		assertTrue( Comparison.IN.test( a, Collections.singleton( a)));
		assertTrue( Comparison.IN.test( a, Arrays.asList( a )));
		assertTrue( Comparison.IN.test( a, a ));
		assertFalse( Comparison.IN.test( a, new String[0]));
		assertFalse( Comparison.IN.test( a, Collections.singleton( "b")));
		assertFalse( Comparison.IN.test( a, Arrays.asList()));
		assertFalse( Comparison.IN.test( a, null));

		assertThrows( IllegalArgumentException.class, () -> Comparison.IN.test( a, "b"));
	}
}
