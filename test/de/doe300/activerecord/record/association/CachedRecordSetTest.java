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
import de.doe300.activerecord.dsl.functions.CountNotNull;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class CachedRecordSetTest extends TestBase implements AssertException
{
	private final RecordBase<TestInterface> base;
	private final RecordSet<TestInterface> set;
	private final TestInterface a1, a2, a3;
	private final TestInterface a4, a5;
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, CachedRecordSetTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void dropTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, CachedRecordSetTest.class.getSimpleName());
	}
	
	public CachedRecordSetTest(final RecordCore core)
	{
		super(core);
		base = core.getBase( TestInterface.class ).getShardBase( CachedRecordSetTest.class.getSimpleName());
		base.findAll().parallel().forEach( ActiveRecord::destroy);
		set = new CachedRecordSet<>(new TableSet<TestInterface>(base, null ));
		
		//fill set
		a1 = base.createRecord();
		a2 = base.createRecord();
		a2.setName( "Hans");
		a3 = base.createRecord();
		a3.setName( "Hans");
		a4 = base.createRecord();
		a5 = base.createRecord();
	}

	@Test
	public void testStream()
	{
		assertTrue( set.stream().count() >= 5);
	}

	@Test
	public void testGetRecordBase()
	{
		assertEquals( base, set.getRecordBase());
	}

	@Test
	public void testGetForCondition()
	{
		assertSame( 2, set.getForCondition( Conditions.is("name", "Hans")).size());
	}

	@Test
	public void testFirst()
	{
		assertEquals( a1, set.first());
		//loads from cache
		assertNotSame(a2, set.first());
	}

	@Test
	public void testLast()
	{
		assertEquals( a5, set.last());
		//loads from cache
		assertNotSame( a2, set.last());
	}

	@Test
	public void testSize()
	{
		assertEquals( 5, set.size());
		//loads from cache
		assertNotSame( 6, set.size());
	}

	@Test
	public void testIsEmpty()
	{
		assertFalse( set.isEmpty());
		//loads from cache
		assertFalse(set.isEmpty());
	}

	@Test
	public void testContains()
	{
		assertTrue( set.contains( a4));
		//loads from cache
		assertTrue( set.contains( a1));
	}

	@Test
	public void testAdd()
	{
		assertThrows( UnsupportedOperationException.class, () -> set.add( a3));
	}

	@Test
	public void testRemove()
	{
		assertThrows( UnsupportedOperationException.class, () -> set.remove( a1 ));
	}

	@Test
	public void testContainsAll()
	{
		assertTrue( set.containsAll( new TableSet<TestInterface>(base, null)));
		assertTrue( new TableSet<TestInterface>(base, null).containsAll(set ));
	}

	@Test
	public void testAddAll()
	{
		assertThrows( UnsupportedOperationException.class, () -> set.addAll( new TableSet<TestInterface>(base, null )));
	}

	@Test
	public void testRetainAll()
	{
		assertThrows( UnsupportedOperationException.class, () -> set.retainAll(new TableSet<TestInterface>(base, null )));
	}

	@Test
	public void testRemoveAll()
	{
		assertThrows( UnsupportedOperationException.class, () -> set.removeAll(new TableSet<TestInterface>(base, null )));
	}

	@Test
	public void testClear()
	{
		assertThrows( UnsupportedOperationException.class, () -> set.clear());
	}

	@Test
	public void testFindWithScope()
	{
		assertEquals( 1, set.findWithScope( new Scope(Conditions.is("name", "Hans"), null, 1 )).count());
	}

	@Test
	public void testFindFirstWithScope()
	{
		assertSame( a2, set.findFirstWithScope( new Scope(Conditions.is("name", "Hans"), null, Scope.NO_LIMIT )));
	}

	@Test
	public void testAggregate()
	{
		final CountNotNull<TestInterface, String> count = new CountNotNull<>("name", TestInterface::getName);
		assertEquals( base.aggregate( count, null ).longValue(), base.getAll().cached().aggregate( count, null).longValue());
	}

	@Test
	public void testCached()
	{
		final RecordSet<TestInterface> set = base.getAll().cached();
		assertEquals( set, set.cached());
	}
	
}
