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


public class ConditionSetTest extends Assert
{
	private static RecordBase<TestInterface> base;
	private static RecordSet<TestInterface> set;
	private static TestInterface a1, a2, a3;
	private static TestInterface n1, n2;
	
	public ConditionSetTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), true).buildBase( TestInterface.class );
		set = AssociationHelper.getConditionSet( base, "age", 23, 12);
		
		//fill set
		a1 = base.createRecord();
		a1.setAge( 23);
		a2 = base.createRecord();
		a2.setAge( 23);
		a3 = base.createRecord();
		a3.setAge( 23);
		n1 = base.createRecord();
		n1.setAge( 10);
		n2 = base.createRecord();
		n2.setAge( 24);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testSize()
	{
		assertEquals(3, set.size());
	}

	@Test
	public void testContains()
	{
		assertTrue( set.contains( a1));
		assertFalse( set.contains( n1));
	}

	@Test
	public void testIterator()
	{
		assertNotNull( set.iterator() );
	}

	@Test
	public void testAdd()
	{
		assertFalse( set.add( a2));
		assertTrue( set.add( n1));
		assertTrue( set.remove( n1));
	}

	@Test
	public void testRemove()
	{
		assertFalse( set.remove( n2));
		assertTrue( set.remove( a3));
		assertTrue( set.add( a3));
	}

	@Test
	public void testContainsAll()
	{
		assertTrue( set.containsAll( Arrays.asList( a1,a2,a3)) );
		assertFalse( set.containsAll( Arrays.asList( a1, a2, n1)));
	}

	@Test
	public void testAddAll()
	{
		assertTrue( set.addAll( Arrays.asList( n1,n2)));
		assertFalse( set.addAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.removeAll( Arrays.asList( n1,n2)));
	}

	@Test
	public void testRetainAll()
	{
		assertTrue( set.retainAll( Arrays.asList( n1,a2,n2)));
		assertFalse( set.retainAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.addAll( Arrays.asList( a1,a2,a3)));
	}

	@Test
	public void testRemoveAll()
	{
		assertTrue( set.removeAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.isEmpty());
		assertTrue( set.addAll( Arrays.asList( a1,a2,a3)));
	}

	@Test
	public void testClear()
	{
		assertFalse( set.isEmpty());
		set.clear();
		assertTrue( set.isEmpty());
	}

	@Test
	public void testStream()
	{
		set.addAll( Arrays.asList( a1,a2,a3));
		assertEquals( 3, set.stream().count());
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
	public void testRecordBase()
	{
		assertNotNull( set.getRecordBase());
		assertSame( base, set.getRecordBase());
	}
	
}