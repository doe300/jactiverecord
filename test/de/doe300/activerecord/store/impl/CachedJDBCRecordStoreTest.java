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
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.Orders;
import de.doe300.activerecord.dsl.functions.Sum;
import de.doe300.activerecord.scope.Scope;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
@Deprecated
public class CachedJDBCRecordStoreTest extends Assert
{
	private static final CachedJDBCRecordStore store;
	static
	{
		try
		{
			store = new CachedJDBCRecordStore(TestServer.getTestConnection());
		}
		catch ( SQLException ex )
		{
			throw new RuntimeException(ex);
		}
	}
	private static RecordBase<TestInterface> base;
	private static int primaryKey;
	
	public CachedJDBCRecordStoreTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTable(store, TestInterface.class, CachedJDBCRecordStoreTest.class.getSimpleName());
		base = RecordCore.fromStore( "Test1", store).getBase(TestInterface.class).getShardBase( CachedJDBCRecordStoreTest.class.getSimpleName() );
		assertNotNull( base );
		primaryKey = base.createRecord().getPrimaryKey();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTable(store, TestInterface.class, CachedJDBCRecordStoreTest.class.getSimpleName());
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
	}

	@Test
	public void testSetValues_4args()
	{
		store.setValues( base, primaryKey, new String[]{"name", "age"}, new Object[]{"Adam", 10000});
		assertEquals( "Adam", store.getValue( base, primaryKey, "name"));
		assertEquals( 10000, store.getValue( base, primaryKey, "age"));
	}

	@Test
	public void testSetValues_3args()
	{
		store.setValues( base, primaryKey, Collections.singletonMap( "age", 13));
		assertSame( 13, store.getValue( base, primaryKey, "age"));
	}

	@Test
	public void testGetValue()
	{
		assertEquals( primaryKey, store.getValue( base, primaryKey, base.getPrimaryColumn()));
		assertNull( store.getValue( base, 100000, "name"));
	}

	@Test
	public void testGetValues()
	{
		store.setValue( base, primaryKey, "name", "Tom");
		final Map<String,Object> m = store.getValues( base, primaryKey, new String[]{base.getPrimaryColumn(), "name"});
		assertEquals( "Tom", m.get( "name"));
		assertEquals( primaryKey, m.get( base.getPrimaryColumn()));
		
		assertEquals( 0, store.getValues( base, primaryKey + 1110, new String[]{base.getPrimaryColumn(), "name"} ).size());
	}

	@Test
	public void testSave()
	{
		store.setValue( base, primaryKey, "name", "Alex");
		assertTrue( store.save( base, primaryKey ));
		assertFalse( store.save( base, primaryKey));
		store.clearCache( base, primaryKey );
		assertEquals( "Alex", store.getValue( base, primaryKey, "name"));
		assertFalse( store.save( base, primaryKey + 12341));
	}

	@Test
	public void testSaveAll()
	{
		store.setValue( base, primaryKey, "age", 113);
		assertTrue( store.saveAll( base ));
		assertFalse( store.saveAll( base));
		store.clearCache( base, primaryKey );
		assertEquals( 113, store.getValue( base, primaryKey, "age"));
	}

	@Test
	public void testIsSynchronized()
	{
		store.setValue( base, primaryKey, "name", "Smith");
		assertFalse( store.isSynchronized( base, primaryKey));
		assertTrue( store.save( base, primaryKey ));
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
		Scope scope = new Scope(Conditions.is(base.getPrimaryColumn(), primaryKey), null, Scope.NO_LIMIT );
		assertTrue(store.findFirstWithData( base, base.getDefaultColumns(), scope).size()>=base.getDefaultColumns().length);
	}

	@Test
	public void testStreamAllWithData()
	{
		Scope scope = new Scope(Conditions.is(base.getPrimaryColumn(), primaryKey), null, 2 );
		assertEquals(1, store.streamAllWithData( base, new String[]{base.getPrimaryColumn()}, scope).count());
		
		Scope scope2 = new Scope(Conditions.is("name", "Failes"), Orders.fromSQLString( "id DESC"), 2 );
		//Tests streaming with data in cache but not in store
		TestInterface i = base.createRecord();
		i.setName( "Failes");
		try(final Stream<Map<String, Object>> s = store.streamAllWithData( base, new String[]{base.getPrimaryColumn()}, scope2))
		{
			assertTrue( s.anyMatch((Map<String,Object> m) -> ((Integer)i.getPrimaryKey()).equals(m.get( base.getPrimaryColumn()))));
		}
		
		//Test Limit
		base.createRecord().setName( "Failes");
		base.createRecord().setName( "Failes");
		assertTrue( store.streamAllWithData( base, base.getDefaultColumns(), scope2).count() == 2);
		
		//Test Order (the two last added records should be returned, so the first is not in the results)
		try(final Stream<Map<String, Object>> s =  store.streamAllWithData( base, base.getDefaultColumns(), scope2))
		{
			assertFalse( s.anyMatch((Map<String,Object> map) -> Integer.valueOf( i.getPrimaryKey()).equals( map.get( base.getPrimaryColumn()))));
		}
	}

	@Test
	public void testGetAllColumnNames()
	{
		assertTrue( Arrays.asList( new String[]{"id", "name", "age", "fk_test_id", "other", "created_at", "updated_at", "test_enum"}).containsAll( store.getAllColumnNames( base.getTableName()) ) );
		assertTrue( ( store.getAllColumnNames( base.getTableName()) ).containsAll( Arrays.asList( new String[]{"id", "name", "age", "fk_test_id", "other", "created_at", "updated_at"})) );
	}

	@Test
	public void testClearCache()
	{
		store.setValue( base, primaryKey, "age", 112 );
		store.save( base, primaryKey );
		store.setValue( base, primaryKey, "age", -112);
		store.clearCache( base, primaryKey );
		assertFalse( Objects.equals( store.getValue( base, primaryKey, "age"), -112));
		assertEquals( 112, store.getValue( base, primaryKey, "age"));
	}

	@Test
	public void testIsCached()
	{
		assertTrue( store.isCached());
	}

	@Test
	public void testCount()
	{
		assertTrue( store.count( base, Conditions.is(base.getPrimaryColumn(), primaryKey)) == 1);
	}

	@Test
	public void testExists()
	{
		assertFalse( store.exists( "noSuchTable"));
		assertTrue( store.exists( base.getTableName()));
	}

	@Test
	public void testTouch()
	{
		Timestamp start = base.getRecord( primaryKey ).getUpdatedAt();
		store.touch( base, primaryKey );
		Timestamp end = base.getRecord( primaryKey ).getUpdatedAt();
		assertTrue( end.compareTo( start) >= 0);
	}

	@Test
	public void testGetAllColumnTypes()
	{
		assertEquals( store.getAllColumnNames( base.getTableName()).size(), store.getAllColumnTypes( base.getTableName()).size());
		assertTrue( store.getAllColumnTypes( base.getTableName()).get( "name").equals( String.class));
	}

	@Test
	public void testAggregate()
	{
		assertTrue(0 <= store.aggregate( base, new Sum<TestInterface, Integer>("age", TestInterface::getAge), null ).intValue());
	}

}