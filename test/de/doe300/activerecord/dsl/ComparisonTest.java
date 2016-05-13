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
import java.util.Optional;
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
		assertTrue( Comparison.IS.test( Optional.ofNullable( a), Optional.ofNullable(a)));
		assertFalse( Comparison.IS.test( Optional.ofNullable(a), Optional.ofNullable("b")));
		assertThrows( IllegalArgumentException.class, () -> Comparison.IS.test( null, null));
	}
	
	@Test
	public void testIS_NOT()
	{
		Object a = "a";
		assertTrue( Comparison.IS_NOT.test( Optional.ofNullable(a), Optional.ofNullable("b")));
		assertFalse( Comparison.IS_NOT.test( Optional.ofNullable(a), Optional.ofNullable(a)));
		assertThrows( IllegalArgumentException.class, () -> Comparison.IS_NOT.test( null, null));
	}
	
	@Test
	public void testIS_NULL()
	{
		Object a = null;
		assertTrue( Comparison.IS_NULL.test( Optional.ofNullable(a), Optional.ofNullable(null)));
		assertFalse( Comparison.IS_NULL.test( Optional.ofNullable("b"), Optional.ofNullable(null)));
		assertThrows( IllegalArgumentException.class, () -> Comparison.IS_NULL.test( null, null));
	}
	
	@Test
	public void testIS_NOT_NULL()
	{
		Object a = null;
		assertTrue( Comparison.IS_NOT_NULL.test( Optional.ofNullable("b"), Optional.ofNullable(null)));
		assertFalse( Comparison.IS_NOT_NULL.test( Optional.ofNullable(a), Optional.ofNullable(null)));
		assertThrows( IllegalArgumentException.class, () -> Comparison.IS_NOT_NULL.test( null, null));
	}
	
	@Test
	public void testLIKE()
	{
		Object a = "Apfelsaft";
		assertTrue( Comparison.LIKE.test( Optional.ofNullable(a), Optional.ofNullable("%saft")));
		assertTrue( Comparison.LIKE.test( Optional.ofNullable(a), Optional.ofNullable("%el%af%")));
		assertFalse( Comparison.LIKE.test( Optional.ofNullable(a), Optional.ofNullable("%Saft")));
		assertFalse( Comparison.LIKE.test( Optional.ofNullable(a), Optional.ofNullable("Birne")));
		assertFalse( Comparison.LIKE.test( Optional.ofNullable(a), Optional.ofNullable("Saftapfel")));
		assertFalse( Comparison.LIKE.test( Optional.ofNullable(a), Optional.ofNullable(null)));
		assertThrows( IllegalArgumentException.class, () -> Comparison.LIKE.test( null, null));
	}

	@Test
	public void testLARGER()
	{
		Object a = -5, b = 7;
		assertTrue( Comparison.LARGER.test( Optional.ofNullable(b), Optional.ofNullable(a)));
		assertFalse( Comparison.LARGER.test( Optional.ofNullable(a), Optional.ofNullable(b)));
		assertFalse( Comparison.LARGER.test( Optional.ofNullable(a), Optional.ofNullable(a )));

		assertThrows( ClassCastException.class, () ->Comparison.LARGER.test( Optional.ofNullable("a"), Optional.ofNullable(new String[0])));
		assertThrows( IllegalArgumentException.class, () -> Comparison.LARGER.test( null, null));
	}

	@Test
	public void testSMALLER()
	{
		Object a = -5, b = 7;
		assertTrue( Comparison.SMALLER.test( Optional.ofNullable(a), Optional.ofNullable(b)));
		assertFalse( Comparison.SMALLER.test( Optional.ofNullable(b), Optional.ofNullable(a)));
		assertFalse( Comparison.SMALLER.test( Optional.ofNullable(a), Optional.ofNullable(a)));

		assertThrows( ClassCastException.class, () -> Comparison.SMALLER.test( Optional.ofNullable(a), Optional.ofNullable(new int[]{7})));
		assertThrows( IllegalArgumentException.class, () -> Comparison.SMALLER.test( null, null));
	}

	@Test
	public void testLARGER_EQUALS()
	{
		Object a = -5, b = 7;
		assertTrue( Comparison.LARGER_EQUALS.test( Optional.ofNullable(b), Optional.ofNullable(a)));
		assertTrue( Comparison.LARGER_EQUALS.test( Optional.ofNullable(a), Optional.ofNullable(a )));
		assertFalse( Comparison.LARGER_EQUALS.test( Optional.ofNullable(a), Optional.ofNullable(b)));

		assertThrows( ClassCastException.class, () -> Comparison.LARGER_EQUALS.test( Optional.ofNullable("a"), Optional.ofNullable(new String[0])));
		assertThrows( IllegalArgumentException.class, () -> Comparison.LARGER_EQUALS.test( null, null));
	}

	@Test
	public void testSMALLER_EQUALS()
	{
		Object a = -5, b = 7;
		assertTrue( Comparison.SMALLER_EQUALS.test( Optional.ofNullable(a), Optional.ofNullable(b)));
		assertTrue( Comparison.SMALLER_EQUALS.test( Optional.ofNullable(a), Optional.ofNullable(a)));
		assertFalse( Comparison.SMALLER_EQUALS.test( Optional.ofNullable(b), Optional.ofNullable(a)));

		assertThrows( ClassCastException.class, () -> Comparison.SMALLER_EQUALS.test( Optional.ofNullable(a), Optional.ofNullable(new int[]{7})));
		assertThrows( IllegalArgumentException.class, () -> Comparison.SMALLER_EQUALS.test( null, null));
	}

	@Test
	public void testTRUE()
	{
		assertTrue( Comparison.TRUE.test( Optional.ofNullable(this), Optional.ofNullable(this)));
		assertTrue( Comparison.TRUE.test( Optional.ofNullable(null), Optional.ofNullable(null)));
		assertTrue( Comparison.TRUE.test( null, null));
	}

	@Test
	public void  testIN()
	{
		Object a = "a";
		assertTrue( Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable(new Object[]{a, "b"})));
		assertTrue( Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable(Collections.singleton( a))));
		assertTrue( Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable(Arrays.asList( a ))));
		assertTrue( Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable(a )));
		assertFalse( Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable(new String[0])));
		assertFalse( Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable(Collections.singleton( "b"))));
		assertFalse( Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable(Arrays.asList())));
		assertFalse( Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable(null)));

		assertThrows( IllegalArgumentException.class, () -> Comparison.IN.test( Optional.ofNullable(a), Optional.ofNullable("b")));
		assertThrows( IllegalArgumentException.class, () -> Comparison.IN.test( null, null));
	}
}
