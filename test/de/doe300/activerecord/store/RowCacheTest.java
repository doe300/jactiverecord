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
package de.doe300.activerecord.store;

import java.sql.Timestamp;
import java.util.Collections;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class RowCacheTest extends Assert
{
	private static RowCache cacheEntry;
	
	public RowCacheTest()
	{
	}
	
	@BeforeClass
	public static void setUpClass()
	{
		cacheEntry = RowCache.fromMap("test", "pk", Collections.singletonMap( "pk", 11), true);
	}
	
	@Test
	public void testFromResultRow_ResultSet() throws Exception
	{
	}

	@Test
	public void testFromResultRow_3args() throws Exception
	{
	}

	@Test
	public void testFromMap()
	{
		RowCache cache = RowCache.fromMap( "test123", "pk", Collections.singletonMap( "pk", 12), true);
		assertNotNull( cache );
		assertEquals( 12, cache.getPrimaryKey());
		assertEquals( 12, cache.getData( "pk"));
	}

	@Test
	public void testEmptyCache()
	{
		RowCache cache = RowCache.emptyCache( "tes", "key", false);
		assertNotNull( cache );
		assertNull( cache.getData( "key"));
	}

	@Test
	public void testSetData_3args_1() throws InterruptedException
	{
		long now = System.currentTimeMillis();
		Thread.sleep( 100);
		cacheEntry.setData( "name", "Adam", true);
		assertEquals("Adam", cacheEntry.getData( "name"));
		assertNotNull( cacheEntry.getData( RecordStore.COLUMN_UPDATED_AT));
		assertTrue( now < ((Timestamp)cacheEntry.getData( RecordStore.COLUMN_UPDATED_AT)).getTime());
		Thread.sleep( 100);
		now = System.currentTimeMillis();
		cacheEntry.setData( "age", 23, false);
		assertEquals(23, cacheEntry.getData( "age"));
		assertNotNull( cacheEntry.getData( RecordStore.COLUMN_UPDATED_AT));
		assertTrue( now > ((Timestamp)cacheEntry.getData( RecordStore.COLUMN_UPDATED_AT)).getTime());
	}

	@Test
	public void testSetData_3args_2() throws InterruptedException
	{
		long now = System.currentTimeMillis();
		Thread.sleep( 100);
		cacheEntry.setData( new String[]{"name", "age"}, new Object[]{"Adam", 23}, true);
		assertEquals("Adam", cacheEntry.getData( "name"));
		assertEquals(23, cacheEntry.getData( "age"));
		assertNotNull( cacheEntry.getData( RecordStore.COLUMN_UPDATED_AT));
		assertTrue( now < ((Timestamp)cacheEntry.getData( RecordStore.COLUMN_UPDATED_AT)).getTime());
	}

	@Test
	public void testGetData()
	{
		cacheEntry.setData( "title", "Mister", true);
		assertNotNull( cacheEntry.getData( RecordStore.COLUMN_UPDATED_AT));
		assertEquals( "Mister", cacheEntry.getData( "title"));
	}

	@Test
	public void testHasData()
	{
		assertFalse( cacheEntry.hasData( "no_data"));
		cacheEntry.setData( "name", "Florian", false);
		assertTrue( cacheEntry.hasData( RecordStore.COLUMN_CREATED_AT));
		assertTrue( cacheEntry.hasData( "name"));
	}

	@Test
	public void testGetPrimaryKey()
	{
		assertEquals( 11, cacheEntry.getPrimaryKey());
	}

	@Test
	public void testGetTableName()
	{
		assertEquals( "test", cacheEntry.getTableName());
	}

	@Test
	public void testIsInDB()
	{
		assertFalse( cacheEntry.isInDB() );
	}

	@Test
	public void testIsSynchronized()
	{
		cacheEntry.setData( "name", "Johny", true);
		assertFalse( cacheEntry.isSynchronized());
		cacheEntry.setSynchronized();
		assertTrue( cacheEntry.isSynchronized());
	}

	@Test
	public void testClear()
	{
		RowCache cache = RowCache.emptyCache("test", "pk", true);
		cache.setData( "name", "Adam", false);
		assertFalse( cache.isSynchronized());
		cache.clear();
		assertTrue( cache.isSynchronized());
	}

	@Test
	public void testUpdate_ResultSet_boolean() throws Exception
	{
	}

	@Test
	public void testToMap()
	{
		cacheEntry.setData( "name", "Johnyy", true);
		assertTrue( cacheEntry.toMap().size() > 0);
		assertEquals("Johnyy", cacheEntry.toMap().get( "name"));
	}

	@Test
	public void testUpdate_Map_boolean()
	{
		cacheEntry.update(Collections.singletonMap("age" , 25), true );
		assertEquals( 25, cacheEntry.getData( "age"));
	}

	@Test
	public void testEquals()
	{
		assertTrue( cacheEntry.equals( cacheEntry));
		assertFalse( cacheEntry.equals( this));
	}

	@Test
	public void testCompareTo()
	{
		RowCache cache = RowCache.fromMap("test", "pk", Collections.singletonMap( "pk", 14),true);
		assertTrue( cacheEntry.compareTo( cache) < 0);
		assertTrue( cache.compareTo( cacheEntry) > 0);
	}
	
}
