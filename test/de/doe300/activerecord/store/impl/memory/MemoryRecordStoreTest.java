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
package de.doe300.activerecord.store.impl.memory;

import de.doe300.activerecord.AssertException;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.dsl.functions.Maximum;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.scope.Scope;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
@Deprecated
public class MemoryRecordStoreTest extends Assert implements AssertException
{
	private static MemoryRecordStore store;
	private static RecordBase<TestInterface> base;
	private static int primaryKey;
	
	@BeforeClass
	public static void init() throws Exception
	{
		store = new MemoryRecordStore();
		store.getDriver().createMigration( TestInterface.class, store).apply();
		base = RecordCore.fromStore( "memory", store).getBase( TestInterface.class);
		primaryKey = base.createRecord().getPrimaryKey();
		base.createRecord().setName( "Adam");
	}
	
	@AfterClass
	public static void deinit() throws Exception
	{
		store.getDriver().createMigration( TestInterface.class, store).revert();
	}
	
	public MemoryRecordStoreTest()
	{
	}

	@Test
	public void testExists()
	{
		assertTrue( store.exists( base.getTableName()));
		assertFalse( store.exists( "NoSuchTable"));
	}

	@Test
	public void testGetAllColumnNames()
	{
		Set<String> columnNames = store.getAllColumnNames( base.getTableName());
		assertNotNull( columnNames);
		assertTrue( columnNames.size() > 4);
		assertTrue( columnNames.contains( "name"));
	}

	@Test
	public void testSetValue()
	{
		store.setValue( base, primaryKey, "name", "Adam");
		assertEquals( "Adam", store.getValue( base, primaryKey, "name"));
	}

	@Test
	public void testSetValues_4args()
	{
		store.setValues( base, primaryKey, new String[]{"name", "age"}, new Object[]{"Eve", 23});
		assertEquals( "Eve", store.getValue( base, primaryKey, "name"));
		assertEquals( 23, base.getRecord( primaryKey).getAge());
	}

	@Test
	public void testSetValues_3args()
	{
		store.setValues( base, primaryKey, Collections.singletonMap( "age", 123));
		assertEquals( 123, base.getRecord( primaryKey).getAge());
	}

	@Test
	public void testGetValue()
	{
		store.setValue( base, primaryKey, "name", "Adam");
		assertNotNull( store.getValue( base, primaryKey, "name"));
		assertEquals( primaryKey, store.getValue( base, primaryKey, base.getPrimaryColumn()));
	}

	@Test
	public void testGetValues_3args()
	{
		Map<String, Object> values = store.getValues( base, primaryKey, new String[]{base.getPrimaryColumn(), "name", "age"});
		assertTrue( values.size() >= 3);
		assertEquals( primaryKey, values.get( base.getPrimaryColumn()));
	}

	@Test
	public void testGetValues_4args()
	{
		assertTrue( store.getValues( base.getTableName(), base.getPrimaryColumn(), "name", "Adam").count() >= 1);
	}

	@Test
	public void testAddRow()
	{
		assertTrue( store.addRow( base.getTableName(), new String[]{"name", "age"}, new Object[]{"Adam", 123} ));
		//throws exception
		assertThrows( IllegalArgumentException.class, () -> store.addRow( base.getTableName(), new String[]{"name", "age"}, new Object[]{"Adam", "Age"} ));
	}

	@Test
	public void testRemoveRow()
	{
		int key = base.createRecord().getPrimaryKey();
		assertTrue( store.removeRow( base.getTableName(), new SimpleCondition(base.getPrimaryColumn(), key, Comparison.IS)));
		assertFalse( store.removeRow( base.getTableName(), new SimpleCondition(base.getPrimaryColumn(), key, Comparison.IS)));
	}

	@Test
	public void testSave()
	{
		assertFalse( store.save( null, primaryKey ));
	}

	@Test
	public void testSaveAll()
	{
		assertFalse( store.saveAll( base));
	}

	@Test
	public void testIsCached()
	{
		assertFalse( store.isCached());
	}

	@Test
	public void testInsertNewRecord()
	{
		assertNotNull( base.createRecord( Collections.singletonMap( "name", "Steve")));
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
		assertFalse( store.containsRecord( base, -10));
	}

	@Test
	public void testDestroy()
	{
		TestInterface i = base.createRecord();
		int key= i.getPrimaryKey();
		assertTrue( store.containsRecord( base, key));
		i.destroy();
		assertFalse( store.containsRecord( base, key));
	}

	@Test
	public void testGetDriver()
	{
		assertFalse(store.getDriver() instanceof JDBCDriver);
	}

	@Test
	public void testGetAllColumnTypes()
	{
		assertTrue(String.class.equals(store.getAllColumnTypes( base.getTableName()).get( "name")));
	}

	@Test
	public void testFindFirstWithData()
	{
		assertEquals("Adam", store.findFirstWithData( base, new String[]{"name"}, new Scope(new SimpleCondition("name", "Adam", Comparison.IS), null, Scope.NO_LIMIT )).get( "name"));
		final Scope noMatch = new Scope(new SimpleCondition("name", "Stevenson", Comparison.IS), null, Scope.NO_LIMIT);
		assertTrue( store.findFirstWithData( base, new String[]{"name"}, noMatch).isEmpty());
	}

	@Test
	public void testAggregate()
	{
		store.setValue( base, primaryKey, "name", "Steve");
		assertEquals( "Steve", store.aggregate( base, new Maximum<TestInterface, String>("name", TestInterface::getName), null));
	}

	@Test
	public void testTouch()
	{
		int key = base.createRecord().getPrimaryKey();
		final Timestamp orig = base.getRecord( key).getUpdatedAt();
		store.touch( base, key);
		final Timestamp newTime = base.getRecord( key).getUpdatedAt();
		assertTrue( newTime.compareTo( orig) >= 0 );
	}
}
