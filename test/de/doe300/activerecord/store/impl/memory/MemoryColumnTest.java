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
package de.doe300.activerecord.store.impl.memory;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author doe300
 */


public class MemoryColumnTest extends Assert
{
	public MemoryColumnTest()
	{
	}

	@Test
	public void testCheckValue()
	{
		MemoryColumn column = new MemoryColumn("testC", Integer.class);
		assertEquals( 5, column.checkValue( 5));
		
		MemoryColumn column1 = new MemoryColumn("testC", String.class);
		assertEquals( "5", column1.checkValue(5));
		
		assertNull( column.checkValue( null));
	}

	@Test
	public void testGetName()
	{
		String name = "testC";
		MemoryColumn column = new MemoryColumn(name, Integer.class);
		assertSame( name, column.getName());
	}

	@Test
	public void testGetType()
	{
		MemoryColumn column = new MemoryColumn("testC", Integer.class);
		assertSame( Integer.class, column.getType());
	}

	@Test
	public void testCompareTo()
	{
		MemoryColumn column = new MemoryColumn("testC", Integer.class);
		MemoryColumn column1 = new MemoryColumn("testC1", Integer.class);
		MemoryColumn column2 = new MemoryColumn("testC2", Integer.class);
		assertTrue( column.compareTo( column1) < 0);
		assertTrue( column1.compareTo( column2) < 0);
		assertTrue( column.compareTo( column2) < 0);
	}
	
}
