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

import de.doe300.activerecord.AssertException;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.functions.CountNotNull;
import de.doe300.activerecord.scope.Scope;
import java.util.Arrays;
import java.util.SortedSet;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */


public class TableSetTest extends Assert implements AssertException
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
		TestServer.buildTestTable( TestInterface.class, TableSetTest.class.getSimpleName());
		base = TestServer.getTestCore().getBase( TestInterface.class ).getShardBase( TableSetTest.class.getSimpleName());
		set = new TableSet<TestInterface>(base, null );
		
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
		TestServer.destroyTestTable( TestInterface.class, TableSetTest.class.getSimpleName());
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
		for(TestInterface i : set)
		{
			assertTrue( set.contains( i));
		}
	}

	@Test
	public void testAdd()
	{
		assertThrows(UnsupportedOperationException.class, () -> set.add( a2));
	}

	@Test
	public void testRemove()
	{
		assertThrows(UnsupportedOperationException.class, () -> set.remove( a5));
	}

	@Test
	public void testContainsAll()
	{
		assertTrue( set.containsAll( Arrays.asList( a1,a2,a3)) );
	}

	@Test
	public void testAddAll()
	{
		assertThrows(UnsupportedOperationException.class, () -> set.addAll( Arrays.asList( a4,a5)));
	}

	@Test
	public void testRetainAll()
	{
		assertThrows(UnsupportedOperationException.class,  () -> set.retainAll( Arrays.asList( a4,a2,a5)));
	}

	@Test
	public void testRemoveAll()
	{
		assertThrows(UnsupportedOperationException.class, () -> set.removeAll( Arrays.asList( a1,a2,a3)));
	}

	@Test
	public void testClear()
	{
		assertThrows( UnsupportedOperationException.class, () -> set.clear());
		assertFalse( set.isEmpty());
	}

	@Test
	public void testStream()
	{
		assertEquals( 5, set.stream().count());
	}

	@Test
	public void testFind()
	{
		assertTrue(set.find( Conditions.is(a2.getBase().getPrimaryColumn(), a2.getPrimaryKey())).allMatch( (TestInterface i) -> i.equals( a2)));
	}

	@Test
	public void testFindFirst()
	{
		assertEquals( a2, set.findFirst( Conditions.is(a2.getBase().getPrimaryColumn(), a2.getPrimaryKey())));
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
		assertNotNull( set.comparator());
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
		assertSame( 2, subSet.size());
		assertSame( a2, subSet.first());
		assertSame( a3, subSet.last());
		assertThrows( UnsupportedOperationException.class, () -> subSet.clear());
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
		SortedSet<TestInterface> subSet = set.getForCondition(Conditions.is("name", "Hans"));
		assertSame( 2, subSet.size());
		assertSame( a2, subSet.first());
		assertSame( a3, subSet.last());
	}

	@Test
	public void testFindWithScope()
	{
		assertTrue( set.findWithScope( new Scope(null, null, 2)).count() <= 2);
	}

	@Test
	public void testFindFirstWithScope()
	{
		assertEquals( base.findFirstWithScope( new Scope(null, null, Scope.NO_LIMIT)), set.findFirstWithScope( new Scope(null, null, Scope.NO_LIMIT)));
	}

	@Test
	public void testGetOrder()
	{
		assertEquals( set.getOrder(), base.getDefaultOrder());
	}

	@Test
	public void testAggregate()
	{
		assertEquals( base.count( null), set.aggregate( new CountNotNull<>(base.getPrimaryColumn(), TestInterface::getPrimaryKey), null).intValue());
	}
}
