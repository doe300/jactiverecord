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
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.dsl.functions.CountNotNull;
import de.doe300.activerecord.scope.Scope;
import java.util.Arrays;
import java.util.SortedSet;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class HasManyAssociationSetTest extends Assert
{
	private static RecordSet<TestInterface> set;
	private static RecordBase<TestInterface> base;
	private static TestInterface assocI;
	private static TestInterface a1, a2, a3;
	private static TestInterface n1, n2;
	
	public HasManyAssociationSetTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTable(TestInterface.class, HasManyAssociationSetTest.class.getSimpleName());
		base = TestServer.getTestCore().getBase( TestInterface.class ).getShardBase( HasManyAssociationSetTest.class.getSimpleName());
		assocI = base.createRecord();
		set = AssociationHelper.getHasManySet( assocI, base, "fk_test_id" );
		
		//fill set
		a1 = base.createRecord();
		a1.setDirectionOne(assocI );
		a2 = base.createRecord();
		a2.setDirectionOne( assocI );
		a2.setName( "Hans");
		a3 = base.createRecord();
		a3.setDirectionOne( assocI );
		a3.setName( "Hans");
		n1 = base.createRecord();
		n2 = base.createRecord();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTable(TestInterface.class, HasManyAssociationSetTest.class.getSimpleName());;
	}
	
	@Before
	public void initObjects()
	{
		set.add( a1);
		set.add( a2);
		set.add( a3);
		set.remove( n1);
		set.remove( n2);
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
		assertFalse( set.contains( null));
		assertFalse( set.contains( "Adam"));
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
		SortedSet<TestInterface> subSet = set.getForCondition(new SimpleCondition("name", "Hans", Comparison.IS));
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
		assertEquals(3, set.findWithScope( new Scope(null, null, Scope.NO_LIMIT)).count());
	}

	@Test
	public void testFindFirstWithScope()
	{
		assertEquals(a2, set.findFirstWithScope( new Scope(new SimpleCondition(base.getPrimaryColumn(), a2.getPrimaryKey(), Comparison.IS), null, Scope.NO_LIMIT)));
	}

	@Test
	public void testGetOrder()
	{
		assertEquals( set.getOrder(), base.getDefaultOrder());
	}

	@Test
	public void testAggregate()
	{
		assertEquals( 2L, set.aggregate( new CountNotNull<>("name", TestInterface::getName), null).longValue());
	}
}
