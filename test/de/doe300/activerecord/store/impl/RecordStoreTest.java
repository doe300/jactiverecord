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
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.dsl.SimpleOrder;
import de.doe300.activerecord.dsl.functions.Sum;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author doe300
 * @since 0.7
 */
@RunWith(Parameterized.class)
public class RecordStoreTest extends Assert implements AssertException
{
	private static String mappingTableName = "mappingTable" + RecordStoreTest.class.getSimpleName();
	private final RecordStore store;
	private final RecordBase<TestInterface> base;
	private final RecordBase<TestInterface> no_such_table;
	private final int primaryKey;
	
	public RecordStoreTest(final RecordStore store) throws Exception
	{
		this.store = store;
		this.base = RecordCore.fromStore( store.getClass().getCanonicalName(), store).getBase( TestInterface.class).getShardBase( RecordStoreTest.class.getSimpleName());
		this.no_such_table = RecordCore.fromStore( store.getClass().getCanonicalName(), store).getBase( TestInterface.class).getShardBase( "no_such_table");
		if(store instanceof MemoryRecordStore)
		{
			store.getDriver().createMigration( TestInterface.class, RecordStoreTest.class.getSimpleName(), store).apply();
			
			Map<String, Class<?>> columns = new HashMap<>(2);
			columns.put( "fk_test1", Integer.class);
			columns.put( "fk_test2", Integer.class);
			store.getDriver().createMigration( mappingTableName, columns, store ).apply();
		}
		this.primaryKey = base.createRecord().getPrimaryKey();
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTable(TestInterface.class, RecordStoreTest.class.getSimpleName());
		TestServer.buildTestMappingTable( mappingTableName );
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() throws SQLException
	{
		return Arrays.asList(
			new Object[]{new SimpleJDBCRecordStore(TestServer.getTestConnection())},
			new Object[]{new CachedJDBCRecordStore(TestServer.getTestConnection())}
			//new Object[]{new MemoryRecordStore()}
		);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestMappingTable( mappingTableName );
		TestServer.destroyTestTable(TestInterface.class, RecordStoreTest.class.getSimpleName());
	}
	
	@Test
	public void testContainsRecord()
	{
		assertTrue( store.containsRecord( base, primaryKey));
		assertFalse( store.containsRecord( base, primaryKey+2000) );
	}

	@Test
	public void testSetValue()
	{
		store.setValue( base, primaryKey, "name", "Eve");
		assertEquals( "Eve", store.getValue( base, primaryKey, "name"));
		
		//no such column
		assertThrows( () -> store.setValue( base, primaryKey, "no_such_column", "Value"), IllegalArgumentException.class );
		//no such primary key
		store.setValue( base, primaryKey + 10000, "name", "Value");
		//no such table
		assertThrows( () -> store.setValue( no_such_table, primaryKey, "name", "Value"), IllegalArgumentException.class );
	}

	@Test
	public void testSetValues_4args()
	{
		store.setValues( base, primaryKey, new String[]{"name", "age"}, new Object[]{"Adam", 10000});
		assertEquals( "Adam", store.getValue( base, primaryKey, "name"));
		assertEquals( 10000, store.getValue( base, primaryKey, "age"));
		
		//no such column
		assertThrows( () -> store.setValues( base, primaryKey, new String[]{"no_such_column", "no_column"}, new Object[]{"valu1", 1000}), IllegalArgumentException.class );
		//no such primary key
		store.setValues( base, primaryKey + 100000, new String[]{"name"}, new Object[]{"Adam"} );
		//no such table
		assertThrows( () -> store.setValues( no_such_table, primaryKey, new String[]{"name"}, new Object[]{"Adam"} ), IllegalArgumentException.class);
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
		
		//no such column
		assertThrows( () -> store.setValues( base, primaryKey, Collections.singletonMap( "no_column", base) ), IllegalArgumentException.class );
		//no such primary key
		store.setValues( base, primaryKey + 10000, Collections.singletonMap( "age", 123) );
		//no such table
		assertThrows( () -> store.setValues( no_such_table, primaryKey, Collections.singletonMap( "age", 123) ), IllegalArgumentException.class );
	}

	@Test
	public void testGetValue()
	{
		assertEquals( primaryKey, store.getValue( base, primaryKey, base.getPrimaryColumn()));
		
		//no such column
		assertThrows( () -> store.getValue( base, primaryKey, "no_column"), IllegalArgumentException.class );
		//no such primary key
		assertNull( store.getValue( base, primaryKey+1000, base.getPrimaryColumn()) );
		//no such table
		assertThrows( () -> store.getValue( no_such_table, primaryKey, "age"), IllegalArgumentException.class );
	}

	@Test
	public void testGetValues_3args()
	{
		store.setValue( base, primaryKey, "name", "Max");
		Map<String,Object> m = store.getValues( base, primaryKey, new String[]{base.getPrimaryColumn(), "name"});
		assertEquals( "Max", m.get( "name"));
		assertEquals( primaryKey, m.get( base.getPrimaryColumn()));
		assertTrue( store.getValues( base, primaryKey, new String[]{"age", "name", "id"} ).size() >= 3);
		
		//no such column
		assertThrows( () ->store.getValues( base, primaryKey, new String[]{"no_column"}), IllegalArgumentException.class);
		//no such primary key
		assertEquals(0, store.getValues( base, primaryKey+1000, base.getDefaultColumns() ).size());
		//no such table
		assertThrows( () ->store.getValues( no_such_table, primaryKey, new String[]{"age"}), IllegalArgumentException.class);
	}
	
	@Test
	public void testGetAllValues()
	{
		assertEquals(store.getAllColumnNames( base.getTableName()).size(), store.getAllValues(base, primaryKey).size());
		//no such primary key
		assertEquals(0, store.getAllValues(base, primaryKey+1000).size());
		//no such table
		assertThrows( () ->store.getAllValues(no_such_table, primaryKey), IllegalArgumentException.class);
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
		
		//no such search-column
		assertThrows( () -> store.getValues( base.getTableName(), "age", "no_column", primaryKey).close(), IllegalArgumentException.class );
		//no such result-column
		assertThrows( () -> store.getValues( base.getTableName(), "no_column", base.getPrimaryColumn(), primaryKey).close(), IllegalArgumentException.class );
		//no such table-name
		assertThrows( () -> store.getValues( "no_such_table", "age", base.getPrimaryColumn(), primaryKey).close(), IllegalArgumentException.class );
	}

	@Test
	public void testSave()
	{
		store.setValue( base, primaryKey, "name", "Alex");
		assertTrue(store.isCached() == store.save( base, primaryKey ));
		assertFalse( store.save( base, primaryKey));
		store.clearCache( base, primaryKey );
		assertEquals( "Alex", store.getValue( base, primaryKey, "name"));
		assertFalse( store.save( base, primaryKey + 12341));
	}

	@Test
	public void testSaveAll()
	{
		store.setValue( base, primaryKey, "age", 113);
		assertTrue( store.isCached() == store.saveAll( base ));
		assertFalse( store.saveAll( base));
		store.clearCache( base, primaryKey );
		assertEquals( 113, store.getValue( base, primaryKey, "age"));
	}

	@Test
	public void testIsSynchronized()
	{
		store.setValue( base, primaryKey, "name", "Smith");
		assertTrue( store.isCached() != store.isSynchronized( base, primaryKey));
		assertTrue( store.isCached() == store.save( base, primaryKey ));
		assertTrue( store.isSynchronized( base, primaryKey));
	}

	@Test
	public void testDestroy()
	{
		int key = store.insertNewRecord( base, null);
		assertTrue( key >= 0);
		assertTrue( store.containsRecord( base, key));
		store.setValue( base, key, "name", "Adam");
		store.destroy( base, key );
		assertFalse( store.containsRecord( base, key));
	}

	@Test
	public void testFindFirstWithData()
	{
		Scope scope = new Scope(new SimpleCondition(base.getPrimaryColumn(), primaryKey, Comparison.IS), null, Scope.NO_LIMIT );
		assertTrue(store.findFirstWithData( base, base.getDefaultColumns(), scope).size()>=base.getDefaultColumns().length);
		//test for empty results
		scope = new Scope(new SimpleCondition(base.getPrimaryColumn(), null, Comparison.IS_NULL), null, Scope.NO_LIMIT);
		assertEquals( 0, store.findFirstWithData( base, new String[]{base.getPrimaryColumn()}, scope).size());
		
		//no such column
		final Scope failScope  = new Scope(new SimpleCondition("no_column", "112", Comparison.IS), null, Scope.NO_LIMIT);
		assertThrows( () ->store.findFirstWithData( base, base.getDefaultColumns(), failScope), IllegalArgumentException.class);
		//no such table
		final Scope failScope2  = new Scope(new SimpleCondition("age", 112, Comparison.IS), null, Scope.NO_LIMIT);
		assertThrows( () ->store.findFirstWithData( no_such_table, base.getDefaultColumns(), failScope2), IllegalArgumentException.class);
	}

	@Test
	public void testStreamAllWithData()
	{
		Scope scope = new Scope(new SimpleCondition(base.getPrimaryColumn(), primaryKey, Comparison.IS), null, 2 );
		assertTrue( store.streamAllWithData( base, new String[]{base.getPrimaryColumn()}, scope).count() == 1);
		
		Scope scope2 = new Scope(new SimpleCondition("name", "Failes", Comparison.IS), SimpleOrder.fromSQLString( "id DESC"), 2 );
		//Tests streaming with data in cache but not in store
		TestInterface i = base.createRecord();
		i.setName( "Failes");
		if(store.isCached())
		{
			try(Stream<Map<String, Object>> s = store.streamAllWithData( base, new String[]{base.getPrimaryColumn()}, scope2))
			{
				assertTrue( s.anyMatch((Map<String,Object> m) -> ((Integer)i.getPrimaryKey()).equals(m.get( base.getPrimaryColumn()))));
			}
		}

		//Test Limit
		Scope scope4 = new Scope(new SimpleCondition("age", 123, Comparison.IS), null, 2 );
		base.createRecord().setAge( 123);
		base.createRecord().setAge( 123);
		base.createRecord().setAge( 123);
		assertTrue( store.streamAllWithData( base, base.getDefaultColumns(), scope4).count() == 2);
		
		//test empty condition
		Scope scope3 = new Scope(null, null, 2);
		assertEquals( 2, store.streamAllWithData( base, base.getDefaultColumns(), scope3).count());

		//Test Order (the two last added records should be returned, so the first is not in the results)
		try(Stream<Map<String, Object>> s = store.streamAllWithData( base, base.getDefaultColumns(), scope4))
		{
			assertFalse( s.anyMatch((Map<String,Object> map) -> Integer.valueOf( i.getPrimaryKey()).equals( map.get( base.getPrimaryColumn()))));
		}
		
		//no such column
		assertThrows( () ->store.streamAllWithData( base, new String[]{"id", "no_colunm"}, scope), IllegalArgumentException.class);
		//no such table
		assertThrows( () ->store.streamAllWithData( no_such_table, new String[]{"id", "name"}, scope), IllegalArgumentException.class);
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
		
		assertThrows( () -> store.getAllColumnNames( "no_such_table" ), IllegalArgumentException.class );
	}

	@Test
	public void testClearCache()
	{
		if(!store.isCached())
		{
			return;
		}
		store.setValue( base, primaryKey, "age", 112 );
		store.save( base, primaryKey );
		store.setValue( base, primaryKey, "age", -112);
		store.clearCache( base, primaryKey );
		assertFalse( Objects.equals( store.getValue( base, primaryKey, "age"), -112));
		assertEquals( 112, store.getValue( base, primaryKey, "age"));
	}

	@Test
	public void testInsertNewRecord()
	{
		assertTrue( store.insertNewRecord(base, null ) > 0);
		//no such column
		assertThrows( ()->store.insertNewRecord( base, Collections.singletonMap( "no_column", "Dummy")), IllegalArgumentException.class);
		//no such talbe
		assertThrows( () -> store.insertNewRecord( no_such_table, Collections.singletonMap( "name", "Adam")), IllegalArgumentException.class);
	}

	@Test
	public void testCount()
	{
		assertTrue( store.count( base, new SimpleCondition(base.getPrimaryColumn(), primaryKey, Comparison.IS)) == 1);
		//test no results
		assertEquals( 0, store.count( base, new SimpleCondition(base.getPrimaryColumn(), null, Comparison.IS_NULL)));
		
		//no such column
		assertThrows( () ->store.count( base, new SimpleCondition("no_column", base, Comparison.IS)), IllegalArgumentException.class);
		//no such table
		assertThrows( () -> store.count( no_such_table, new SimpleCondition("age", 112, Comparison.IS)), IllegalArgumentException.class );
	}

	@Test
	public void testAddRow()
	{
		assertTrue( store.addRow( mappingTableName, new String[]{"fk_test1", "fk_test2"}, new Object[]{primaryKey,primaryKey} ));
		
		//FIXME cosistent handling for already eyisting row
		//adding already existing row
		mayThrow( () -> assertFalse( store.addRow( base.getTableName(), new String[]{"id", "name"}, new Object[]{primaryKey,"Test"} )), IllegalArgumentException.class);
		//no such colums
		assertThrows( () -> store.addRow( base.getTableName(), new String[]{"id", "no_such_column"}, new Object[]{primaryKey,"Test"} ), IllegalArgumentException.class );
		//no such table
		assertThrows( () -> store.addRow( "no_such_table", new String[]{"id", "name"}, new Object[]{primaryKey,"Test"} ), IllegalArgumentException.class );
	}

	@Test
	public void testRemoveRow()
	{
		assertTrue( store.addRow( mappingTableName, new String[]{"fk_test1", "fk_test2"}, new Object[]{primaryKey,primaryKey} ));
		assertTrue( store.removeRow( mappingTableName, new SimpleCondition("fk_test1", primaryKey, Comparison.IS)));
		//removing not existing row
		assertFalse( store.removeRow( mappingTableName, new SimpleCondition("fk_test1", primaryKey, Comparison.IS)));
		//negative test - throws exception
		assertThrows( () ->store.removeRow( "noSuchTable", new SimpleCondition("fk_test1", primaryKey, Comparison.IS)), IllegalArgumentException.class);
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
		int conditional = store.aggregate( base, new Sum<TestInterface, Integer>("age", TestInterface::getAge), new SimpleCondition("name", null, Comparison.IS_NOT_NULL) ).intValue();
		assertTrue(conditional < total);
		
		//no such column
		assertThrows( () ->store.aggregate( base, new Sum<TestInterface, Integer>("no_such_row", TestInterface::getAge), null ), IllegalArgumentException.class) ;
		//no such table
		assertThrows( () ->store.aggregate( no_such_table, new Sum<TestInterface, Integer>("age", TestInterface::getAge), null ), IllegalArgumentException.class) ;
	}

	@Test
	public void testTouch()
	{
		Timestamp start = base.getRecord( primaryKey ).getUpdatedAt();
		store.touch( base, primaryKey );
		Timestamp end = base.getRecord( primaryKey ).getUpdatedAt();
		assertTrue( end.compareTo( start) >= 0);
		
		assertThrows( () -> store.touch( no_such_table, primaryKey), IllegalArgumentException.class );
	}

	@Test
	public void testGetAllColumnTypes()
	{
		assertEquals( store.getAllColumnNames( base.getTableName()).size(), store.getAllColumnTypes( base.getTableName()).size());
		assertTrue( store.getAllColumnTypes( base.getTableName()).get( "name").equals( String.class));
		//fails
		assertThrows( () ->store.getAllColumnTypes( "no_such_table"), IllegalArgumentException.class);
	}
}
