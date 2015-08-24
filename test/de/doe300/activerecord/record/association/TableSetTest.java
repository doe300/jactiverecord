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
package de.doe300.activerecord.record.association;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import java.util.Arrays;
import java.util.SortedSet;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */


public class TableSetTest extends Assert
{
	private static RecordBase<TestInterface> base;
	private static RecordSet<TestInterface> set;
	private static TestInterface a1, a2, a3;
	private static TestInterface a4, a5;
	
	public TableSetTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), true).getBase( TestInterface.class );
		set = new TableSet<TestInterface>(base );
		
		//fill set
		a1 = base.createRecord();
		a2 = base.createRecord();
		a2.setName( "Hans");
		a3 = base.createRecord();
		a3.setName( "Hans");
		a4 = base.createRecord();
		a5 = base.createRecord();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testSize()
	{
		assertEquals(5, set.size());
	}

	@Test
	public void testContains()
	{
		assertTrue( set.contains( a1));
		assertFalse( set.contains( null));
		assertFalse( set.contains( "Hans"));
	}

	@Test
	public void testIterator()
	{
		assertNotNull( set.iterator() );
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAdd()
	{
		assertFalse( set.add( a2));
		assertTrue( set.add( a4));
		assertTrue( set.remove( a4));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemove()
	{
		assertFalse( set.remove( a5));
		assertTrue( set.remove( a3));
		assertTrue( set.add( a3));
	}

	@Test
	public void testContainsAll()
	{
		assertTrue( set.containsAll( Arrays.asList( a1,a2,a3)) );
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddAll()
	{
		assertTrue( set.addAll( Arrays.asList( a4,a5)));
		assertFalse( set.addAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.removeAll( Arrays.asList( a4,a5)));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRetainAll()
	{
		assertTrue( set.retainAll( Arrays.asList( a4,a2,a5)));
		assertFalse( set.retainAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.addAll( Arrays.asList( a1,a2,a3)));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemoveAll()
	{
		assertTrue( set.removeAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.isEmpty());
		assertTrue( set.addAll( Arrays.asList( a1,a2,a3)));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testClear()
	{
		assertFalse( set.isEmpty());
		set.clear();
		assertTrue( set.isEmpty());
	}

	@Test
	public void testStream()
	{
		assertEquals( 5, set.stream().count());
	}

	@Test
	public void testFind()
	{
		assertTrue(set.find( new SimpleCondition(a2.getBase().getPrimaryColumn(), a2.getPrimaryKey(), Comparison.IS)).allMatch( (TestInterface i) -> i.equals( a2)));
	}

	@Test
	public void testFindFirst()
	{
		assertEquals( a2, set.findFirst( new SimpleCondition(a2.getBase().getPrimaryColumn(), a2.getPrimaryKey(), Comparison.IS)));
	}

	@Test
	public void testGetRecordBase()
	{
		assertNotNull( set.getRecordBase());
		assertSame( base, set.getRecordBase());
	}
	
	@Test
	public void testComparator()
	{
		assertNull( set.comparator());
	}
	
	@Test
	public void testHeadSet()
	{
		SortedSet<TestInterface> headSet = set.headSet( a3);
		assertSame( 2, headSet.size());
		assertSame( a2, headSet.last());
		assertSame( a1, headSet.first());
	}
	
	@Test
	public void testTailSet()
	{
		SortedSet<TestInterface> tailSet = set.tailSet( a3);
		assertSame( 2, tailSet.size());
		assertSame( a4, tailSet.first());
		assertSame( a5, tailSet.last());
	}
	
	@Test
	public void testSubSet()
	{
		SortedSet<TestInterface> subSet = set.subSet(a2, a4);
		assertSame( 1, subSet.size());
		assertSame( a3, subSet.first());
		assertSame( a3, subSet.last());
	}
	
	@Test
	public void testFirst()
	{
		assertSame( a1, set.first());
	}
	
	@Test
	public void testLast()
	{
		assertSame( a5, set.last());
	}
	
	@Test
	public void testGetForCondition()
	{
		SortedSet<TestInterface> subSet = set.getForCondition(new SimpleCondition("name", "Hans", Comparison.IS ));
		assertSame( 2, subSet.size());
		assertSame( a2, subSet.first());
		assertSame( a3, subSet.last());
	}
}
