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
package de.doe300.activerecord;

import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.validation.ValidationFailed;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author daniel
 * @param <T>
 */
@RunWith(Parameterized.class)
public class RecordBaseTest<T extends TestInterface> extends Assert
{
	private final RecordBase<T> base;
	private final Class<T> type;
	
	//FIXME validations fail for simple jdbc record-store / memory store /anything without cache??)
	
	public RecordBaseTest(Class<T> type, RecordBase<T> base) throws SQLException
	{
		this.type = type;
		this.base = base;
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() throws SQLException
	{
		return Arrays.asList(
			new Object[]{TestInterface.class, TestServer.getTestCore().getBase( TestInterface.class )},
			new Object[]{TestPOJO.class, TestServer.getTestCore().getBase( TestPOJO.class )},
			new Object[]{TestSingleInheritancePOJO.class, TestServer.getTestCore().getBase( TestSingleInheritancePOJO.class )}
		);
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}
	
	@Test
	public void testGetStore()
	{
		assertNotNull( base.getStore());
	}

	@Test
	public void testGetCore()
	{
		assertNotNull( base.getCore());
	}

	@Test
	public void testGetRecordType()
	{
		assertEquals( type, base.getRecordType());
	}

	@Test
	public void testGetTableName()
	{
		assertEquals(type.getAnnotation(RecordType.class).typeName(), base.getTableName());
	}

	@Test
	public void testGetPrimaryColumn()
	{
		assertEquals(type.getAnnotation(RecordType.class).primaryKey(), base.getPrimaryColumn());
	}

	@Test
	public void testEquals() throws Exception
	{
		T t = base.createRecord();
		assertNotNull( t );
		assertTrue(RecordBase.equals( t,t));
		assertFalse(RecordBase.equals( t, base.createRecord()));
	}

	@Test
	public void testGetRecord() throws Exception
	{
		T t = base.createRecord();
		int key = t.getPrimaryKey();
		assertNotNull( base.getRecord( key));
		assertEquals( t, base.getRecord( key));
		t.destroy();
		assertNull( base.getRecord( key ));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNewRecord() throws Exception
	{
		base.destroy( 50);
		TestInterface i = base.newRecord( 50);
		assertNotNull(i);
		assertFalse( i.inRecordStore() );
		assertFalse( i.isSynchronized());
		i.setName( "Adam");
		//throws exception
		base.newRecord( 50);
	}

	@Test
	public void testCreateRecord() throws Exception
	{
		TestInterface i = base.createRecord();
		assertNotNull( i );
		assertTrue( i.inRecordStore());
		i.destroy();
		assertFalse( i.inRecordStore());
		assertFalse( i.isSynchronized());
	}

	@Test
	public void testHasRecord() throws Exception
	{
		T t = base.createRecord();
		assertNotNull( t);
		assertTrue( base.hasRecord( t.getPrimaryKey()));
		assertTrue( base.getStore().containsRecord( base, t.getPrimaryKey()));
		t.destroy();
	}

	@Test
	public void testGetDefaultColumns()
	{
		assertTrue(Arrays.equals(type.getAnnotation(RecordType.class).defaultColumns(), base.getDefaultColumns()));
	}

	@Test
	public void testGetDefaultOrder()
	{
		assertNotNull( base.getDefaultOrder());
	}

	@Test
	public void testSaveAll()
	{
		//make sure all records are savable(validated)
		base.find( new SimpleCondition("name", null, Comparison.IS_NULL)).forEach( (T t) -> t.setName( "Dummy"));
		base.saveAll();
		//no data has changed since last save
		assertFalse( base.saveAll() );
		T t = base.createRecord();
		t.setName( "Idiot");
		if(base.getStore().isCached())
		{
			assertTrue( base.saveAll());
		}
		else
		{
			assertFalse( base.saveAll());
		}
	}

	@Test
	public void testRecordStoreExists()
	{
		assertTrue( base.recordStoreExists());
	}

	@Test
	public void testSave() throws Exception
	{
		T t = base.createRecord();
		assertNotNull( t );
		t.setAge( 23);
		t.setName( "Adam");
		if(base.getStore().isCached())
		{
			//we're writing the cache to the storage
			assertTrue( t.save());
		}
		else
		{
			//we already have everything in storage
			assertFalse( t.save());
		}
		base.reload( t);
		assertEquals(23, t.getAge());
		assertEquals( 23, base.getStore().getValue( base, t.getPrimaryKey(), "age"));
	}

	@Test
	public void testIsSynchronized() throws Exception
	{
		base.destroy( 101);
		T t = base.newRecord( 101);
		t.setName( "Johny");
		if(base.getStore().isCached())
		{
			assertFalse( t.isSynchronized());
			assertTrue( base.createRecord().isSynchronized());
			assertTrue( t.save());
		}
		assertTrue( t.isSynchronized());
	}

	@Test
	public void testReload() throws Exception
	{
		T t = base.createRecord();
		assertNotNull( t );
		t.setAge( 12);
		t.setName( "Test123");
		assertEquals( 12, t.getAge());
		if(base.getStore().isCached())
		{
			assertFalse( t.isSynchronized());
		}
		assertTrue( t.inRecordStore());
		if(base.getStore().isCached())
		{
			assertTrue( t.save());
		}
		
		base.getStore().setValue( base, t.getPrimaryKey(), "age", 13);
		//or t.setAge(13);
		t.reload();
		assertTrue( t.inRecordStore());
		assertTrue( t.isSynchronized());
		if(base.getStore().isCached())
		{
			assertEquals(12, t.getAge() );
		}
		else
		{
			assertEquals( 13, t.getAge());
		}
	}

	@Test
	public void testDestroy() throws Exception
	{
		T t = base.createRecord();
		assertNotNull( t );
		int key = t.getPrimaryKey();
		t.destroy();
		assertFalse( t.inRecordStore());
		assertFalse( t.isSynchronized());
		assertNull( base.getRecord( key));
		assertFalse( base.hasRecord( key));
		assertFalse( base.getStore().containsRecord( base, key));
	}

	@Test
	public void testFind() throws Exception
	{
		T t = base.createRecord();
		assertTrue( base.find( new SimpleCondition(base.getPrimaryColumn(), t.getPrimaryKey(), Comparison.IS)).count() == 1);
	}

	@Test
	public void testFindFirst()
	{
		assertNotNull( base.findFirst( new SimpleCondition(base.getPrimaryColumn(), null, Comparison.IS_NOT_NULL) ));
	}

	@Test
	public void testCount()
	{
		assertTrue( base.count( new SimpleCondition(base.getPrimaryColumn(), base, Comparison.IS_NOT_NULL)) == base.find( 
				new SimpleCondition(base.getPrimaryColumn(), base, Comparison.TRUE)).count());
	}

	@Test
	public void testWhere()
	{
		QueryResult<T> res = base.where( new SimpleCondition("name", base, Comparison.TRUE));
		assertNotNull( res );
		assertEquals( base.getDefaultOrder(), res.getOrder());
	}

	@Test
	public void testIsSearchable()
	{
		assertTrue( base.isSearchable() );
	}

	@Test
	public void testSearch() throws Exception
	{
		T t = base.createRecord( Collections.singletonMap( "name", "AlexandraEven"));
		assertNotNull( t );
		assertTrue(base.search( "AlexandraEven").count() >= 1);
		base.search( "AlexandraEven").forEach( (T i) -> i.destroy());
		assertTrue(base.search( "AlexandraEven").count() == 0);
	}

	@Test
	public void testSearchFirst() throws Exception
	{
		T t = base.createRecord( Collections.singletonMap( "name", "hansPeterOlaf"));
		assertNotNull( t );
		assertEquals( t, base.searchFirst( "PeterOla") );
		base.search( "hansPeterOlaf").forEach( (T i) -> i.destroy());
	}

	@Test
	public void testIsTimestamped()
	{
		assertTrue( base.isTimestamped());
	}

	@Test
	public void testIsValidated()
	{
		assertTrue( base.isValidated());
	}

	@Test
	public void testIsValid() throws Exception
	{
		T t = base.createRecord();
		assertFalse( base.isValid(t));
	}

	@Test(expected = ValidationFailed.class)
	public void testValidate() throws Exception
	{
		T t = base.createRecord();
		t.setName( "Name");
		assertNotNull( t.getName());
		base.validate( t);
		try{
			t.setName( null );
		}
		catch(UndeclaredThrowableException e)
		{
			//TODO fix, so ValidationFailed is thrown
			throw new ValidationFailed("name", t);
		}
		base.validate( t);
	}
	
	@Test
	public void testFindForColumn()
	{
		T t = base.createRecord();
		assertTrue( base.findFor(base.getPrimaryColumn(), t.getPrimaryKey()).count() == 1);
	}
	
	@Test
	public void testFindFirstForColumn()
	{
		T t = base.createRecord();
		assertSame(t, base.findFirstFor(base.getPrimaryColumn(), (Integer)t.getPrimaryKey()));
	}
	
	@Test
	public void testFindForMap()
	{
		T t = base.createRecord();
		assertTrue( base.findFor(Collections.singletonMap( base.getPrimaryColumn(), t.getPrimaryKey())).count() == 1);
	}
	
	@Test
	public void testFindFirstForMap()
	{
		T t = base.createRecord();
		assertSame(t, base.findFirstFor(Collections.singletonMap( base.getPrimaryColumn(), t.getPrimaryKey())));
	}

	@Test
	public void testCreateRecord_0args()
	{
		assertNotNull( base.createRecord() );
	}

	@Test
	public void testCreateRecord_Map()
	{
		T record = base.createRecord( Collections.singletonMap( "name", "Afam"));
		assertNotNull( record);
		assertEquals( "Afam", record.getName());
	}

	@Test
	public void testDuplicate()
	{
		T record = base.createRecord(Collections.singletonMap( "name", "Alex"));
		assertNotNull( record);
		T dup = base.duplicate( record );
		assertNotNull( dup);
		assertTrue( base.find( new SimpleCondition("name", "Alex", Comparison.IS)).count() >= 2);
		assertTrue( base.count( new SimpleCondition("name", "Alex", Comparison.IS)) >= 2);
	}

	@Test
	public void testHasCallbacks()
	{
		assertTrue( base.hasCallbacks());
	}

	@Test
	public void testFindWithScope()
	{
		Scope scope = new Scope(new SimpleCondition(base.getPrimaryColumn(), null, Comparison.IS_NOT_NULL), null, Scope.NO_LIMIT );
		assertNotNull( base.findFirstWithScope( scope ));
	}

	@Test
	public void testFindFirstWithScope()
	{
		Scope scope = new Scope(new SimpleCondition(base.getPrimaryColumn(), null, Comparison.IS_NOT_NULL), null, Scope.NO_LIMIT );
		assertNotNull( base.findFirstWithScope( scope ));
	}

	@Test
	public void testQueryWithScope()
	{
		Scope scope = new Scope(new SimpleCondition("name", base, Comparison.TRUE), null, Scope.NO_LIMIT );
		QueryResult<T> res = base.withScope(scope);
		assertNotNull( res );
		assertEquals( base.getDefaultOrder(), res.getOrder());
	}

	@Test
	public void testIsAutoCreate()
	{
		assertFalse( base.isAutoCreate());
	}
}
