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
package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.RecordType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class CollectionHandlerTest extends Assert
{
	private static RecordBase<TestCollectionInterface> base;
	private static CollectionHandler handler;
	private static TestCollectionInterface record;
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, CollectionHandlerTest.class.getSimpleName());
		handler = new CollectionHandler();
		base = TestServer.getTestCore().getBase(CollectionHandlerTest.TestCollectionInterface.class, handler).getShardBase( CollectionHandlerTest.class.getSimpleName());
		record = base.createRecord();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, CollectionHandlerTest.class.getSimpleName());
	}
	
	public CollectionHandlerTest()
	{
	}

	@Test
	public void testSize()
	{
		assertTrue( record.size() == base.getStore().getAllColumnNames( base.getTableName()).size());
	}

	@Test
	public void testIsEmpty()
	{
		assertFalse( record.isEmpty());
	}

	@Test
	public void testContains()
	{
		assertTrue( record.contains( record.getPrimaryKey()));
	}

	@Test
	public void testIterator()
	{
		int size = 0;
		Iterator<Object> it = record.iterator();
		while(it.hasNext())
		{
			size++;
			assertTrue( record.contains( it.next()));
		}
		assertTrue( size == base.getStore().getAllColumnNames( base.getTableName()).size());
	}

	@Test
	public void testToArray()
	{
		assertTrue( record.toArray().length == base.getStore().getAllColumnNames( base.getTableName()).size());
		Object[] arr = new Object[base.getStore().getAllColumnNames( base.getTableName()).size()];
		assertSame( record.toArray(arr), arr);
	}

	@Test(expected = RuntimeException.class)
	public void testAdd()
	{
		record.add( "Test");
	}

	@Test(expected = RuntimeException.class)
	public void testRemove( )
	{
		record.remove( "Tes");
	}

	@Test
	public void testContainsAll()
	{
		record.setName( "Name1");
		Collection<Object> col = Arrays.asList( record.getPrimaryKey(), "Name1" );
		assertTrue( record.containsAll( col));
		record.setName( "name2");
		assertFalse( record.containsAll( col));
	}

	@Test(expected = RuntimeException.class)
	public void testAddAll()
	{
		record.addAll( Arrays.asList( record.getPrimaryKey(), "Name1" ) );
	}

	@Test(expected = RuntimeException.class)
	public void testRetainAll()
	{
		record.retainAll(Arrays.asList( record.getPrimaryKey(), "Name1" ) );
	}

	@Test(expected = RuntimeException.class)
	public void testRemoveAll()
	{
		record.removeAll( Arrays.asList( record.getPrimaryKey(), "Name1" ) );
	}

	@Test(expected = RuntimeException.class)
	public void testClear()
	{
		record.clear();
	}
	
	@RecordType(typeName = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
	public static interface TestCollectionInterface extends TestInterface, Collection<Object>
	{
		
	}
}
