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

import de.doe300.activerecord.AssertException;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.functions.Sum;
import de.doe300.activerecord.scope.Scope;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author daniel
 */
@Deprecated
public class SimpleJDBCRecordStoreTest extends Assert implements AssertException
{
	private static final SimpleJDBCRecordStore store;
	static
	{
		try
		{
			store = new SimpleJDBCRecordStore(TestServer.getTestConnection());
		}
		catch ( SQLException ex )
		{
			throw new RuntimeException(ex);
		}
	}
	private static final String mappingTableName = "mappingTable"+SimpleJDBCRecordStoreTest.class.getSimpleName();
	private static RecordBase<TestInterface> base;
	private static int primaryKey;
	
	public SimpleJDBCRecordStoreTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTable(store, TestInterface.class, SimpleJDBCRecordStoreTest.class.getSimpleName());
		TestServer.buildTestMappingTable(store, mappingTableName );
		base = RecordCore.fromStore( "Test1", store).getBase(TestInterface.class).getShardBase( SimpleJDBCRecordStoreTest.class.getSimpleName());
		assertNotNull( base );
		primaryKey = base.createRecord().getPrimaryKey();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestMappingTable(store, mappingTableName );
		TestServer.destroyTestTable(store, TestInterface.class, SimpleJDBCRecordStoreTest.class.getSimpleName());
	}

	@Test
	public void testSetValue()
	{
		store.setValue( base, primaryKey, "age", 104);
		assertEquals( 104, store.getValue( base, primaryKey, "age"));
	}

	@Test
	public void testSetValues_3args()
	{
		store.setValues( base, primaryKey, Collections.singletonMap( "age", 13));
		assertEquals( 13, store.getValue( base, primaryKey, "age"));
		//set empty values
		store.setValues( base, primaryKey, Collections.emptyMap());
		//set only primary key, should be a no-op
		store.setValues( base, primaryKey, Collections.singletonMap( base.getPrimaryColumn(), 112112));
		assertNull( store.getValue( base, 112112, "age"));
		assertEquals( 13, store.getValue( base, primaryKey, "age"));
		//negative test - throws exception
		assertThrows( IllegalArgumentException.class, () -> store.setValues( base, primaryKey, Collections.singletonMap( "no_column", base) ));
	}

	@Test
	public void testGetValue()
	{
		assertEquals( primaryKey, store.getValue( base, primaryKey, base.getPrimaryColumn()));
		//no results
		assertNull( store.getValue( base, primaryKey+1000, base.getPrimaryColumn()) );
		//negative test - throws exception
		assertThrows( IllegalArgumentException.class, () -> store.getValue( base, primaryKey, "no_column"));
	}

	@Test
	public void testGetValues_3args()
	{
		store.setValue( base, primaryKey, "name", "Max");
		Map<String,Object> m = store.getValues( base, primaryKey, new String[]{base.getPrimaryColumn(), "name"});
		assertEquals( "Max", m.get( "name"));
		assertEquals( primaryKey, m.get( base.getPrimaryColumn()));
		//no results
		assertEquals( 0, store.getValues( base, primaryKey+1000, base.getDefaultColumns() ).size());
		//select all
		assertTrue( store.getValues( base, primaryKey, new String[]{"age", "name", "id"} ).size() >= 3);
		//negative test - throws exception
		assertThrows( IllegalArgumentException.class, () -> store.getValues( base, primaryKey, new String[]{"no_column"}));
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
	}

	@Test
	public void testDestroy()
	{
		int key = store.insertNewRecord( base, null);
		assertTrue( key >= 0);
		assertTrue( store.containsRecord( base, key));
		store.destroy( base, key );
		assertFalse( store.containsRecord( base, key));
	}

	@Test
	public void testFindFirstWithData()
	{
		Scope scope = new Scope(Conditions.is(base.getPrimaryColumn(), primaryKey), null, Scope.NO_LIMIT );
		assertTrue(store.findFirstWithData( base, base.getDefaultColumns(), scope).size()>=base.getDefaultColumns().length);
		//test for empty results
		final Scope scope1 = new Scope(Conditions.isNull(base.getPrimaryColumn()), null, Scope.NO_LIMIT);
		assertEquals( 0, store.findFirstWithData( base, new String[]{base.getPrimaryColumn()}, scope1).size());
		//negative test - throws exception
		final Scope scope2  = new Scope(Conditions.is("no_column", "112"), null, Scope.NO_LIMIT);
		assertThrows( IllegalArgumentException.class, () -> store.findFirstWithData( base, base.getDefaultColumns(), scope2));
		
	}

	@Test
	public void testCount()
	{
		assertTrue( store.count( base, Conditions.is(base.getPrimaryColumn(), primaryKey)) == 1);
		//test no results
		assertEquals( 0, store.count( base, Conditions.isNull(base.getPrimaryColumn())));
		//negative test - throws exception
		assertThrows( IllegalArgumentException.class, () -> store.count( base, Conditions.is("no_column", base)));
	}

	@Ignore
	@Test
	public void testStreamAllWithData()
	{
		Scope scope = new Scope(Conditions.is(base.getPrimaryColumn(), primaryKey), null, 2 );
		assertEquals(1, store.streamAllWithData( base, new String[]{base.getPrimaryColumn()}, scope).count());
		
		//Test Limit
		Scope scope2 = new Scope(Conditions.is("age", 123), null, 2 );
		base.createRecord().setAge( 123);
		base.createRecord().setAge( 123);
		base.createRecord().setAge( 123);
		//TODO somehow fails on Travis CI (both HSQLDB and SQLite)
		assertEquals(2, store.streamAllWithData( base, base.getDefaultColumns(), scope2).count());
		
		//test empty condition
		Scope scope3 = new Scope(null, null, 2);
		assertEquals( 2, store.streamAllWithData( base, base.getDefaultColumns(), scope3).count());
		
		//negative test - throws exception
		assertThrows( IllegalArgumentException.class, () -> {try(final Stream<Map<String, Object>> s = store.streamAllWithData( base, new String[]{"id", "no_colunm"}, scope))
		{
			
		}});
	}

	@Test
	public void testExists()
	{
		assertTrue( store.exists( base.getTableName()));
		assertFalse( store.exists( "NonexistentTable"));
	}

	@Test
	public void testGetAllColumnNames()
	{
		assertTrue( Arrays.asList( new String[]{"id", "name", "age", "fk_test_id", "other", "created_at", "updated_at", "test_enum"}).containsAll( store.getAllColumnNames( base.getTableName()) ) );
		assertTrue( ( store.getAllColumnNames( base.getTableName()) ).containsAll( Arrays.asList( new String[]{"id", "name", "age", "fk_test_id", "other", "created_at", "updated_at", "test_enum"})) );
	}

	@Test
	public void testInsertNewRecord()
	{
		assertTrue( store.insertNewRecord(base, null ) > 0);
		//negative test - throws exception
		assertThrows( IllegalArgumentException.class, () -> store.insertNewRecord( base, Collections.singletonMap( "no_column", "Dummy")));
	}

	@Test
	public void testGetValues_4args()
	{
		store.setValue( base, primaryKey, "name", "Heinz");
		try(final Stream<Object> s = store.getValues( base.getTableName(), "name", base.getPrimaryColumn(), primaryKey))
		{
			assertEquals("Heinz", s.findFirst().get());
		}
		//no results
		assertEquals( 0, store.getValues( base.getTableName(), "name", base.getPrimaryColumn(), primaryKey+1000 ).count());
		//negative test - throws exception
		assertThrows( IllegalArgumentException.class, () -> {
		try(final Stream<Object> s = store.getValues( base.getTableName(), "no_column", base.getPrimaryColumn(), primaryKey))
		{
			
		}});
	}


	@Test
	public void testIsCached()
	{
		assertFalse( store.isCached());
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

	@Test
	public void testAddRow()
	{
		assertTrue( store.addRow( mappingTableName, new String[]{"fk_test1", "fk_test2"}, new Object[]{primaryKey,primaryKey} ));
		//negative test - adding already existing row - throws exception
		assertThrows( IllegalArgumentException.class, () -> store.addRow( base.getTableName(), new String[]{"id", "name"}, new Object[]{primaryKey,"Test"} ));
	}

	@Test
	public void testRemoveRow()
	{
		assertTrue( store.addRow( mappingTableName, new String[]{"fk_test1", "fk_test2"}, new Object[]{primaryKey,primaryKey} ));
		assertTrue( store.removeRow( mappingTableName, Conditions.is("fk_test1", primaryKey)));
		//removing not existing row
		assertFalse( store.removeRow( mappingTableName, Conditions.is("fk_test1", primaryKey)));
		//negative test - throws exception
		assertThrows( IllegalArgumentException.class, () -> store.removeRow( "noSuchTable", Conditions.is("fk_test1", primaryKey)));
	}

	@Test
	public void testAggregate()
	{
		store.insertNewRecord( base, Collections.singletonMap( "age", 21));
		Map<String, Object> values = new HashMap<>(2);
		values.put( "name", "Adam");
		values.put( "age", 112);
		store.insertNewRecord( base, values );
		int total = store.aggregate( base, new Sum<TestInterface, Integer>("age", TestInterface::getAge), null ).intValue();
		assertTrue(21 <= total);
		int conditional = store.aggregate( base, new Sum<TestInterface, Integer>("age", TestInterface::getAge), Conditions.isNotNull("name") ).intValue();
		assertTrue(conditional < total);
		assertThrows( IllegalArgumentException.class, () -> store.aggregate( base, new Sum<TestInterface, Integer>("no_such_row", TestInterface::getAge), null ));
	}

	@Test
	public void testGetAllColumnTypes()
	{
		assertEquals( store.getAllColumnNames( base.getTableName()).size(), store.getAllColumnTypes( base.getTableName()).size());
		assertTrue( store.getAllColumnTypes( base.getTableName()).get( "name").equals( String.class));
		//fails
		assertThrows( IllegalArgumentException.class, () -> store.getAllColumnTypes( "no_such_table"));
	}
}
