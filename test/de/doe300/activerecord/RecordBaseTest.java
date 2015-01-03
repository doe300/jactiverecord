package de.doe300.activerecord;

import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.QueryResult;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.DataSet;
import de.doe300.activerecord.validation.ValidationFailed;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
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
	
	public RecordBaseTest(Class<T> type, RecordBase<T> base) throws SQLException
	{
		this.type = type;
		this.base = base;
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() throws SQLException
	{
		return Arrays.asList(
			new Object[]{TestInterface.class, RecordCore.fromDatabase( TestInterface.createTestConnection(), true).buildBase( TestInterface.class )},
				new Object[]{TestPOJO.class, RecordCore.fromDatabase( TestInterface.createTestConnection(), true).buildBase( TestPOJO.class )}
		);
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
		assertEquals( type.getAnnotation( DataSet.class).dataSet(), base.getTableName());
	}

	@Test
	public void testGetPrimaryColumn()
	{
		assertEquals( type.getAnnotation( DataSet.class).primaryKey(), base.getPrimaryColumn());
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
		assertNotNull( base.newRecord( 50));
		assertFalse(base.getStore().containsRecord( base, 50));
		base.newRecord( 50);
	}

	@Test
	public void testCreateRecord() throws Exception
	{
		assertNotNull( base.createRecord());
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
		assertTrue(Arrays.equals( type.getAnnotation( DataSet.class).defaultColumns(), base.getDefaultColumns()));
	}

	@Test
	public void testGetDefaultOrder()
	{
		assertNotNull( base.getDefaultOrder());
	}

	@Test
	public void testSaveAll()
	{
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
		assertTrue( t.save());
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
		assertFalse( t.isSynchronized());
		assertTrue( base.createRecord().isSynchronized());
		assertTrue( t.save());
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
		assertFalse( t.isSynchronized());
		assertTrue( t.inRecordStore());
		assertTrue( t.save());
		
		base.getStore().setValue( base, t.getPrimaryKey(), "age", 13);
		//or t.setAge(13);
		t.reload();
		assertTrue( t.inRecordStore());
		assertTrue( t.isSynchronized());
		assertEquals(12, t.getAge() );
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
		assertFalse( t.isValid());
	}

	@Test(expected = ValidationFailed.class)
	public void testValidate() throws Exception
	{
		T t = base.createRecord();
		t.setName( "Name");
		assertNotNull( t.getName());
		t.validate();
		t.setName( null );
		t.validate();
	}
	
	@Test
	//Tests FinderMethods#findFor(String,Object)
	public void testFindForColumn()
	{
		T t = base.createRecord();
		assertTrue( base.findFor(base.getPrimaryColumn(), t.getPrimaryKey()).count() == 1);
	}
	
	@Test
	//Tests FinderMethods#findFirstFor(String,Object)
	public void testFindFirstForColumn()
	{
		T t = base.createRecord();
		assertSame(t, base.findFirstFor(base.getPrimaryColumn(), (Integer)t.getPrimaryKey()));
	}
	
	@Test
	//Tests FinderMethods#findFor(Map)
	public void testFindForMap()
	{
		T t = base.createRecord();
		assertTrue( base.findFor(Collections.singletonMap( base.getPrimaryColumn(), t.getPrimaryKey())).count() == 1);
	}
	
	@Test
	//Tests FinderMethods#findFirstFor(Map)
	public void testFindFirstForMap()
	{
		T t = base.createRecord();
		assertSame(t, base.findFirstFor(Collections.singletonMap( base.getPrimaryColumn(), t.getPrimaryKey())));
	}
}
