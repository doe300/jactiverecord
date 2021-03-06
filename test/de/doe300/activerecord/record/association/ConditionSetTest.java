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
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.functions.Sum;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import java.util.Arrays;
import java.util.Objects;
import java.util.SortedSet;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */


public class ConditionSetTest extends TestBase implements AssertException
{
	private final RecordBase<TestInterface> base;
	private final RecordSet<TestInterface> set;
	private final TestInterface a1, a2, a3;
	private final TestInterface n1, n2;
	
	public ConditionSetTest(final RecordCore core)
	{
		super(core);
		base = core.getBase( TestInterface.class ).getShardBase( ConditionSetTest.class.getSimpleName());
		base.findAll().parallel().forEach( ActiveRecord::destroy);
		set = AssociationHelper.getConditionSet( base, "age", 23, 12);
		
		//fill set
		a1 = base.createRecord();
		a1.setAge( 23);
		a2 = base.createRecord();
		a2.setAge( 23);
		a2.setName( "Hans");
		a3 = base.createRecord();
		a3.setAge( 23);
		a3.setName( "Hans");
		n1 = base.createRecord();
		n1.setAge( 10);
		n2 = base.createRecord();
		n2.setAge( 24);
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, ConditionSetTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, ConditionSetTest.class.getSimpleName());
	}
	
	@Before
	public void resetObjects()
	{
		a1.setAge( 23);
		a2.setAge( 23);
		a3.setAge( 23);
		n1.setAge( 10);
		n2.setAge( 24);
	}
	
	@Test
	public void testUnmodifiableSet()
	{
		RecordSet<TestInterface> set1 = new ConditionSet<TestInterface>(base, Conditions.is("age", 23), null);
		assertTrue( set1.containsAll( Arrays.asList( a1,a2,a3)) );
		assertThrows( UnsupportedOperationException.class, () -> set1.remove( a1 ));
	}

	@Test
	public void testSize()
	{
		set.clear();
		set.addAll( Arrays.asList( a1,a2,a3));
		assertEquals(3, set.size());
	}

	@Test
	public void testContains()
	{
		assertTrue( set.contains( a1));
		assertFalse( set.contains( n1));
		assertFalse( set.contains( null));
		assertFalse( set.contains( "A"));
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
		assertFalse( set.removeAll( Arrays.asList( a1,a2,a3)));
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
	public void testFindAll()
	{
		set.addAll( Arrays.asList( a1,a2,a3));
		assertEquals( 3, set.findAll().count());
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
		assertTrue(set.find( Conditions.is(a2.getBase().getPrimaryColumn(), a2.getPrimaryKey())).parallel().allMatch( (TestInterface i) -> i.equals( a2)));
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
		assertNotNull( set.comparator() );
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
		SortedSet<TestInterface> tailSet = set.tailSet( a1);
		assertSame( 2, tailSet.size());
		assertSame( a2, tailSet.first());
		assertSame( a3, tailSet.last());
	}
	
	@Test
	public void testSubSet()
	{
		SortedSet<TestInterface> subSet = set.subSet(a1, a3);
		assertSame( 2, subSet.size());
		assertSame( a1, subSet.first());
		assertSame( a2, subSet.last());
		//test backing
		set.remove( a2);
		assertSame( 1, subSet.size());
		set.add( a2);
		assertSame( 2, subSet.size());
		subSet.clear();
		assertSame( 1, set.size());
		subSet.add( a2);
		assertSame( 2, set.size());
	}
	
	@Test
	public void testFirst()
	{
		assertSame( a1, set.first());
	}
	
	@Test
	public void testLast()
	{
		assertSame( a3, set.last());
	}

	@Test
	public void testGetForCondition()
	{
		SortedSet<TestInterface> subSet = set.getForCondition(Conditions.is("name", "Hans"));
		assertSame( 2, subSet.size());
		assertSame( a2, subSet.first());
		assertSame( a3, subSet.last());
		//test backing
		set.remove( a2);
		assertSame( 1, subSet.size());
		set.add( a2);
		assertSame( 2, subSet.size());
		subSet.clear();
		assertSame( 1, set.size());
		subSet.add( a2);
		assertSame( 2, set.size());
	}

	@Test
	public void testFindWithScope()
	{
		TestInterface i = base.createRecord();
		i.setName( "Hans");
		i.setAge( 23);
		try(final Stream<TestInterface> s = set.findWithScope( new Scope(null, null, Scope.NO_LIMIT)).parallel())
		{
			assertTrue( s.anyMatch( (TestInterface t) -> Objects.equals( t.getName(), "Hans")));
		}
	}

	@Test
	public void testFindFirstWithScope()
	{
		assertNotNull( set.findFirstWithScope( new Scope(null, null, Scope.NO_LIMIT)));
	}

	@Test
	public void testGetOrder()
	{
		assertEquals( set.getOrder(), base.getDefaultOrder());
	}

	@Test
	public void testAggregate()
	{
		assertEquals( Long.valueOf( 69).longValue(), set.aggregate( new Sum<>("age", TestInterface::getAge), null).longValue());
	}
}
