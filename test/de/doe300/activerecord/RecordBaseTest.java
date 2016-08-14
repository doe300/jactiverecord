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

import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.Orders;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.dsl.functions.Absolute;
import de.doe300.activerecord.dsl.functions.Sum;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.record.validation.ValidationFailed;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;

/**
 *
 * @author daniel
 * @param <T>
 */
public class RecordBaseTest<T extends TestInterface> extends TestBase implements AssertException
{
	private final RecordBase<T> base;
	private final Class<T> type;
	
	public RecordBaseTest(Class<T> type, final RecordCore core) throws SQLException
	{
		super(core);
		this.type = type;
		this.base = core.getBase( type).getShardBase( RecordBaseTest.class.getSimpleName());
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() throws SQLException
	{
		return Arrays.asList(
			new Object[]{TestInterface.class, TestServer.getTestCore( SimpleJDBCRecordStore.class)},
			new Object[]{TestInterface.class, TestServer.getTestCore( CachedJDBCRecordStore.class)},
			new Object[]{TestInterface.class, TestServer.getTestCore( MemoryRecordStore.class)},
			new Object[]{TestPOJO.class, TestServer.getTestCore( SimpleJDBCRecordStore.class)},
			new Object[]{TestPOJO.class, TestServer.getTestCore( CachedJDBCRecordStore.class)},
			new Object[]{TestPOJO.class, TestServer.getTestCore( MemoryRecordStore.class)},
			new Object[]{TestSingleInheritancePOJO.class, TestServer.getTestCore( SimpleJDBCRecordStore.class )},
			new Object[]{TestSingleInheritancePOJO.class, TestServer.getTestCore( CachedJDBCRecordStore.class )},
			new Object[]{TestSingleInheritancePOJO.class, TestServer.getTestCore( MemoryRecordStore.class )}
		);
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestSingleInheritancePOJO.class, RecordBaseTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestSingleInheritancePOJO.class, RecordBaseTest.class.getSimpleName());
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
	public void testGetShardBase() throws Exception
	{
		assertEquals(base, base.getCore().getBase( type ).getShardBase( RecordBaseTest.class.getSimpleName()));
		
		assertThrows( IllegalStateException.class, () ->base.getShardBase( "dummyShard"));
	}

	@Test
	public void testGetRecordType()
	{
		assertEquals( type, base.getRecordType());
	}

	@Test
	public void testGetTableName()
	{
		assertEquals(RecordBaseTest.class.getSimpleName(), base.getTableName());
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
		assertFalse( RecordBase.equals( t, null));
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
		base.find( Conditions.isNull("name")).parallel().forEach( (T t) -> t.setName( "Dummy"));
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
		assertEquals(23, t.getAge());
		assertEquals( 23, base.getStore().getValue( base, t.getPrimaryKey(), "age"));
	}

	@Test
	public void testIsSynchronized() throws Exception
	{
		T t = Objects.requireNonNull(base.createRecord());
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
		assertEquals(1, base.find( Conditions.is(base.getPrimaryColumn(), t.getPrimaryKey())).count());
	}

	@Test
	public void testFindFirst()
	{
		assertNotNull( base.findFirst( Conditions.isNotNull(base.getPrimaryColumn()) ));
	}

	@Test
	public void testCount()
	{
		assertEquals( base.count( Conditions.isNotNull(base.getPrimaryColumn())), base.find( Conditions.isTrue()).count());
	}

	@Test
	public void testWhere() throws Exception
	{
		try(QueryResult<T> res = base.where( Conditions.isTrue()))
		{
			assertNotNull( res );
			assertEquals( base.getDefaultOrder(), res.getOrder());
		}
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
		base.search( "AlexandraEven").parallel().forEach( (T i) -> i.destroy());
		assertEquals(0, base.search( "AlexandraEven").count());
	}

	@Test
	public void testSearchFirst() throws Exception
	{
		T t = base.createRecord( Collections.singletonMap( "name", "hansPeterOlaf"));
		assertNotNull( t );
		assertEquals( t, base.searchFirst( "PeterOla") );
		base.search( "hansPeterOlaf").parallel().forEach( (T i) -> i.destroy());
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

	@Test
	public void testValidate() throws Exception
	{
		T t = base.createRecord();
		t.setName( "Name");
		assertNotNull( t.getName());
		base.validate( t);
		mayThrow(ValidationFailed.class, () -> {
		try{
			t.setName( null );
		}
		catch(UndeclaredThrowableException e)
		{
			//TODO fix, so ValidationFailed is thrown
			throw new ValidationFailed("name", t);
		}} );
		mayThrow(ValidationFailed.class, () -> base.validate( t));
	}
	
	@Test
	public void testFindForColumn()
	{
		T t = base.createRecord();
		assertEquals(1, base.findFor(base.getPrimaryColumn(), t.getPrimaryKey()).count());
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
		//tests single condition
		T t = base.createRecord();
		assertEquals(1, base.findFor(Collections.singletonMap( base.getPrimaryColumn(), t.getPrimaryKey())).count());
		//tests multiple conditions
		t.setAge( 123);
		final Map<String, Object> conds = new HashMap<>(2);
		conds.put( base.getPrimaryColumn(), t.getPrimaryKey());
		conds.put( "age", 123);
		assertEquals(1, base.findFor( conds).count());
	}
	
	@Test
	public void testFindFirstForMap()
	{
		//tests single condition
		T t = base.createRecord();
		assertSame(t, base.findFirstFor(Collections.singletonMap( base.getPrimaryColumn(), t.getPrimaryKey())));
		//tests multiple conditions
		t.setAge( 124);
		final Map<String, Object> conds = new HashMap<>(2);
		conds.put( base.getPrimaryColumn(), t.getPrimaryKey());
		conds.put( "age", 124);
		assertEquals(1, base.findFor( conds).count());
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
		
	}
	
	@Test
	public void testCreateRecord_Map_Consumer()
	{
		T record = base.createRecord( Collections.singletonMap( "name", "Afam"), 
				(final T t) -> {
					assertEquals( "Afam", t.getName());
				}
		);
		assertNotNull( record);
		assertThrows( RecordException.class, () ->base.createRecord( null, (final T t) -> {
			throw new IllegalArgumentException("Dummy error");
		}));
	}

	@Test
	public void testDuplicate()
	{
		T record = base.createRecord(Collections.singletonMap( "name", "Alex"));
		assertNotNull( record);
		T dup = base.duplicate( record );
		assertNotNull( dup);
		assertTrue( base.find( Conditions.is("name", "Alex")).count() >= 2);
		assertTrue( base.count( Conditions.is("name", "Alex")) >= 2);
	}

	@Test
	public void testHasCallbacks()
	{
		assertTrue( base.hasCallbacks());
	}

	@Test
	public void testFindWithScope()
	{
		Scope scope = new Scope(Conditions.isNotNull(base.getPrimaryColumn()), null, Scope.NO_LIMIT );
		assertNotNull( base.findFirstWithScope( scope ));
	}

	@Test
	public void testFindFirstWithScope()
	{
		Scope scope = new Scope(Conditions.isNotNull(base.getPrimaryColumn()), null, Scope.NO_LIMIT );
		assertNotNull( base.findFirstWithScope( scope ));
	}

	@Test
	public void testQueryWithScope() throws Exception
	{
		Scope scope = new Scope(Conditions.isTrue(), null, Scope.NO_LIMIT );
		try(QueryResult<T> res = base.withScope(scope))
		{
			assertNotNull( res );
			assertEquals( base.getDefaultOrder(), res.getOrder());
		}
	}

	@Test
	public void testIsAutoCreate()
	{
		assertFalse(base.isAutoCreate());
	}

	@Test
	public void testAggregate()
	{
		final T min = base.createRecord();
		min.setName( "Aaaa");
		min.setAge( -112);
		//to compensate the -112 for sum-test
		base.createRecord().setAge( 113);
		final T max = base.createRecord();
		max.setName( "Aaaa");
		max.setAge( 112200);
		//default aggregates
		assertTrue( base.minimum( "age", TestInterface::getAge) <= min.getAge());
		assertTrue( min.getName().compareTo(base.minimum( "name", TestInterface::getName)) >= 0);
		assertTrue( base.maximum( "age", TestInterface::getAge) >= max.getAge() );
		//we have duplicate name, so count not null must be larger than count distinct
		assertTrue( base.countNotNull( "name", TestInterface::getName) >= base.countDistinct( "name", TestInterface::getName));
		assertTrue( base.sum( "age", TestInterface::getAge) > max.getAge());
		assertEquals( base.sum( "age", TestInterface::getAge), base.sumFloating( "age", TestInterface::getAge), 0.01d);
		assertTrue( base.average( "age", TestInterface::getAge) < max.getAge());
		//more complex aggregates
		assertTrue( base.aggregate( new Sum<>(new Absolute<>("age", TestInterface::getAge)), null).longValue() > base.sum( "age", TestInterface::getAge));
	}

	@Test
	public void testWithScope() throws Exception
	{
		try(QueryResult<T> r = base.where( Conditions.isLarger( "age", 12)))
		{
			assertEquals( r.getEstimatedSize(), base.withScope( 
				new Scope(Conditions.isLarger("age", 12), Orders.fromSQLString( "age DESC"), Scope.NO_LIMIT )).stream().count() );
		}
	}

	@Test
	public void testGetAll() throws Exception
	{
		try(QueryResult<T> r = base.where( null))
		{
			assertEquals( base.getAll().size(), r.getEstimatedSize());
		}
		assertEquals( base.getAll().getOrder(), base.getDefaultOrder());
	}

	@Test
	public void testGetForCondition()
	{
		assertEquals( base.getForCondition( null, null).size(), base.count( null));
	}
}
