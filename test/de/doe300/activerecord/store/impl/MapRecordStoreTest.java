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
package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import java.util.Collections;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class MapRecordStoreTest extends Assert
{
	private static MapRecordStore store;
	private static RecordBase<TestInterface> base;
	private static int primaryKey;
	
	public MapRecordStoreTest()
	{
	}
	
	@BeforeClass
	public static void init() throws Exception
	{
		store = new MapRecordStore();
		base = RecordCore.fromStore( "map", store).buildBase( TestInterface.class);
		primaryKey = base.createRecord().getPrimaryKey();
	}

	@Test
	public void testSetValue()
	{
		store.setValue( base, primaryKey, "name", "Adam");
		assertEquals( "Adam", store.getValue( base, primaryKey, "name"));
	}

	@Test
	public void testGetValue()
	{
		store.setValue( base, primaryKey, "name", "Adam");
		assertEquals( "Adam", store.getValue( base, primaryKey, "name"));
	}

	@Test
	public void testGetValues_3args()
	{
	}

	@Test
	public void testIsSynchronized()
	{
		assertTrue( store.isSynchronized( base, primaryKey));
	}

	@Test
	public void testContainsRecord()
	{
		assertTrue( store.containsRecord( base, primaryKey));
		assertFalse( store.containsRecord( base, primaryKey+200));
	}

	@Test
	public void testDestroy()
	{
		int key = store.insertNewRecord( base );
		assertTrue( store.containsRecord( base, key));
		store.destroy( base, key);
		assertFalse( store.containsRecord( base, key));
	}

	@Test
	public void testCount()
	{
	}

	@Test
	public void testFindFirstWithData()
	{
	}

	@Test
	public void testStreamAllWithData()
	{
	}

	@Test
	public void testExists()
	{
		assertTrue( store.exists( base.getTableName()));
	}

	@Test
	public void testGetAllColumnNames()
	{
	}

	@Test
	public void testInsertNewRecord()
	{
		int key = store.insertNewRecord( base );
		assertTrue( store.containsRecord( base, key));
	}

	@Test
	public void testGetValues_4args()
	{
	}

	@Test
	public void testIsCached()
	{
		assertFalse( store.isCached());
	}

	@Test
	public void testSetValues_4args()
	{
		store.setValues( base, primaryKey, new String[]{"name","age"}, new Object[]{"Johnny", 13});
		assertEquals( 13, store.getValue( base, primaryKey, "age"));
		assertEquals( "Johnny", store.getValue( base, primaryKey, "name"));
	}

	@Test
	public void testSetValues_3args()
	{
		store.setValues( base, primaryKey, Collections.singletonMap("name", "Johnny2"));
		assertEquals( "Johnny2", store.getValue( base, primaryKey, "name"));
	}

	@Test
	public void testSave()
	{
		assertFalse( store.save( base, primaryKey));
	}

	@Test
	public void testSaveAll()
	{
		assertFalse( store.saveAll( base));
	}

}
