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
package de.doe300.activerecord.scope;

import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.Orders;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author doe300
 * @since 0.8
 */
public class ScopeTest extends Assert
{
	private final Scope emptyScope = new Scope(null, null, 0);
	private final Scope usefulScope = new Scope(Conditions.is( "age", 12), Orders.sortAscending( "name"), 100);
	
	public ScopeTest()
	{
	}

	@Test
	public void testGetCondition()
	{
		assertNull( emptyScope.getCondition());
		assertNull( Scope.DEFAULT.getCondition());
		assertNotNull( usefulScope.getCondition());
	}

	@Test
	public void testGetOrder()
	{
		assertNull( emptyScope.getOrder());
		assertNull( Scope.DEFAULT.getOrder());
		assertNotNull( usefulScope.getOrder());
	}

	@Test
	public void testGetLimit()
	{
		assertEquals( 0, emptyScope.getLimit());
		assertEquals( Scope.NO_LIMIT, Scope.DEFAULT.getLimit());
		assertEquals( 100, usefulScope.getLimit());
	}

	@Test
	public void testEquals()
	{
		assertFalse( emptyScope.equals( null));
		assertFalse( emptyScope.equals( usefulScope));
		assertTrue( emptyScope.equals( emptyScope));
	}

	@Test
	public void testHashCode()
	{
		assertEquals( emptyScope.hashCode(), emptyScope.hashCode());
		assertNotEquals( emptyScope.hashCode(), Scope.DEFAULT.hashCode());
	}
	
}
