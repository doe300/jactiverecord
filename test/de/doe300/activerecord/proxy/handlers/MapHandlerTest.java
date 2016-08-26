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

import de.doe300.activerecord.AssertException;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.RecordType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class MapHandlerTest extends TestBase implements AssertException
{
	private final RecordBase<TestMapInterface> base;
	private final MapHandler handler;
	private final TestMapInterface record;
	
	public MapHandlerTest(final RecordCore core)
	{
		super(core);
		
		handler = new MapHandler();
		base = core.getBase( TestMapInterface.class, handler).getShardBase( MapHandlerTest.class.getSimpleName());
		record = base.createRecord();
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestMapInterface.class, MapHandlerTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestMapInterface.class, MapHandlerTest.class.getSimpleName());
	}
	
	@Test
	public void testSize()
	{
		assertSame( base.getStore().getAllColumnNames( base.getTableName()).size(), record.size());
	}

	@Test
	public void testIsEmpty()
	{
		assertFalse( record.isEmpty());
	}

	@Test
	public void testContainsKey()
	{
		assertTrue( record.containsKey( "name"));
		assertFalse( record.containsKey( "birthdate"));
	}

	@Test
	public void testContainsValue()
	{
		assertTrue( record.containsValue( record.getPrimaryKey()));
		assertFalse( record.containsValue( record));
	}

	@Test
	public void testGet()
	{
		record.setName( "Klaus");
		assertEquals(record.getName(), record.get( "name"));
	}

	@Test
	public void testPut()
	{
		record.put( "age", 112);
		assertEquals(record.getAge(), record.get( "age"));
		int key = record.getPrimaryKey();
		record.put( base.getPrimaryColumn(), 01);
		assertEquals(key, record.getPrimaryKey());
		
		assertThrows( RuntimeException.class, () -> record.put( "some_key", 34));
	}

	@Test
	public void testRemove()
	{
		assertThrows( RuntimeException.class, () -> record.remove( "name"));
	}

	@Test
	public void testPutAll()
	{
		Map<String, Object> m = new HashMap<>(2);
		m.put( "name", "Adam");
		m.put( "age", 0);
		record.putAll( m );
		assertEquals(m.get( "name"), record.get( "name"));
		assertEquals(m.get( "age"), record.getAge());
		
		assertThrows( RuntimeException.class, () -> record.putAll( Collections.singletonMap( "no_such_key", "Eve")));
		//skips setting primary key
		final int primaryKey = record.getPrimaryKey();
		record.putAll( Collections.singletonMap( base.getPrimaryColumn(), primaryKey + 112));
		assertEquals( primaryKey, record.getPrimaryKey());
	}

	@Test
	public void testClear()
	{
		assertThrows( RuntimeException.class, () -> record.clear());
	}

	@Test
	public void testKeySet()
	{
		assertTrue( base.getStore().getAllColumnNames( base.getTableName()).containsAll( record.keySet()));
		assertTrue( record.keySet().containsAll( base.getStore().getAllColumnNames( base.getTableName())));
	}

	@Test
	public void testValues()
	{
		assertTrue( record.values().size() == base.getStore().getAllColumnNames( base.getTableName()).size());
	}

	@Test
	public void testEntrySet()
	{
		assertTrue( record.entrySet().size() == base.getStore().getAllColumnNames( base.getTableName()).size());
		for(Map.Entry<String,Object> e:record.entrySet())
		{
			assertEquals( base.getStore().getValue( base, record.getPrimaryKey(), e.getKey()), e.getValue());
			if("name".equals( e.getKey() ))
			{
				e.setValue( "Stevenson");
				assertEquals( "Stevenson", record.getName());
				record.setName( "Adam");
				assertEquals( "Adam", e.getValue());
			}
		}
	}
	
	
	@RecordType(typeName = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
	public static interface TestMapInterface extends TestInterface, Map<String, Object>
	{
		
	}
}
