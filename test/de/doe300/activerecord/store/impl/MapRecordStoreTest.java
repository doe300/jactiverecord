/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
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
	
}
