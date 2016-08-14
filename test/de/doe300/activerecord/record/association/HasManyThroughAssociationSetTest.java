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
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.Orders;
import de.doe300.activerecord.dsl.functions.CountDistinct;
import de.doe300.activerecord.scope.Scope;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class HasManyThroughAssociationSetTest extends TestBase
{
	private static final String mappingTable = "hasManyThroughMappingTable";
	private RecordSet<TestInterface> set;
	private RecordBase<TestInterface> base;
	private TestInterface assocI;
	private TestInterface a1, a2, a3;
	private TestInterface n1, n2;
	
	public HasManyThroughAssociationSetTest(@Nonnull final RecordCore core)
	{
		super(core);
		base = core.getBase( TestInterface.class ).getShardBase( HasManyThroughAssociationSetTest.class.getSimpleName());
		assocI = base.createRecord();
		set = AssociationHelper.getHasManyThroughSet(assocI, base, mappingTable, "fk_test1", "fk_test2" );
		
		//fill set
		a1 = base.createRecord();
		AssociationHelper.addHasManyThrough( assocI, a1, mappingTable, "fk_test1", "fk_test2");
		a2 = base.createRecord();
		AssociationHelper.addHasManyThrough( assocI, a2, mappingTable, "fk_test1", "fk_test2");
		a2.setName( "Hans");
		a3 = base.createRecord();
		AssociationHelper.addHasManyThrough( assocI, a3, mappingTable, "fk_test1", "fk_test2");
		a3.setName( "Hans");
		n1 = base.createRecord();
		n2 = base.createRecord();
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestMappingTables( mappingTable);
		TestServer.buildTestTables(TestInterface.class, HasManyThroughAssociationSetTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, HasManyThroughAssociationSetTest.class.getSimpleName());
		TestServer.destroyTestMappingTables( mappingTable);
	}

	@Test
	public void testSize()
	{
		assertEquals(3, set.size());
		assertEquals( 3, set.stream().count());
	}

	@Test
	public void testContains()
	{
		assertTrue( set.contains( a1));
		assertFalse( set.contains( n1));
		assertFalse( set.contains( null));
		assertFalse( set.contains( "b"));
	}

	@Test
	public void testIterator()
	{
		for(final TestInterface i : set)
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
		assertTrue( set.addAll( Arrays.asList( a1, a2, a3)));
	}

	@Test
	public void testStream()
	{
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
		assertFalse( subSet.contains( a2));
		set.add( a2);
		assertSame( 2, subSet.size());
		assertTrue( subSet.contains( a2));
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
		RecordSet<TestInterface> subSet = set.getForCondition( Conditions.is("name", "Hans") );
		assertSame( 2, subSet.size());
		assertSame( a2, subSet.first());
		assertSame( a3, subSet.last());
		//test backing
		assertTrue( set.remove( a2));
		assertSame( 1, subSet.size());
		assertTrue( set.add( a2));
		assertSame( 2, subSet.size());
		assertTrue( subSet.remove( a2));
		assertTrue( subSet.add( a2));
		assertSame( 2, subSet.size());
		assertEquals(2, subSet.countDistinct("id", TestInterface::getPrimaryKey));
	}

	@Test
	public void testFindWithScope()
	{
		try(final Stream<TestInterface> s = set.findWithScope( new Scope(null, Orders.sortDescending( base.getPrimaryColumn()), 5)).parallel())
		{
			assertSame( a3, s.findFirst().get());
		}
	}

	@Test
	public void testFindFirstWithScope()
	{
		assertSame( a3, set.findFirstWithScope( new Scope(null, Orders.sortDescending(base.getPrimaryColumn()), 5)));
	}

	@Test
	public void testAggregate()
	{
		assertEquals(1L, set.aggregate( new CountDistinct<>("name", TestInterface::getName), null ).longValue() );
		assertEquals(1, set.count(Conditions.isNull("name") ));
	}
}
