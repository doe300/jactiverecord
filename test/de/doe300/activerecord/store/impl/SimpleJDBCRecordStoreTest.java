package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import java.util.Collections;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class SimpleJDBCRecordStoreTest extends Assert
{
	private static SimpleJDBCRecordStore store;
	private static RecordBase<TestInterface> base;
	private static int primaryKey;
	
	public SimpleJDBCRecordStoreTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		store = new SimpleJDBCRecordStore(TestServer.getTestConnection());
		base = RecordCore.fromStore( "Test1", store).buildBase(TestInterface.class);
		assertNotNull( base );
		primaryKey = base.createRecord().getPrimaryKey();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testSetValue()
	{
		store.setValue( base, primaryKey, "age", 104);
		assertEquals( 104, store.getValue( base, primaryKey, "age"));
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
	}

	@Test
	public void testGetValues_3args()
	{
		store.setValue( base, primaryKey, "name", "Max");
		Map<String,Object> m = store.getValues( base, primaryKey, new String[]{base.getPrimaryColumn(), "name"});
		assertEquals( "Max", m.get( "name"));
		assertEquals( primaryKey, m.get( base.getPrimaryColumn()));
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
		int key = store.insertNewRecord( base);
		assertTrue( key >= 0);
		assertTrue( store.containsRecord( base, key));
		store.destroy( base, key );
		assertFalse( store.containsRecord( base, key));
	}

	@Test
	public void testFindFirstWithData()
	{
		assertTrue(store.findFirstWithData( base, base.getDefaultColumns(), new SimpleCondition(base.getPrimaryColumn(), primaryKey, Comparison.IS) ).size()>=base.getDefaultColumns().length);
	}

	@Test
	public void testCount()
	{
		assertTrue( store.count( base, new SimpleCondition(base.getPrimaryColumn(), primaryKey, Comparison.IS)) == 1);
	}

	@Test
	public void testStreamAllWithData()
	{
		assertTrue( store.streamAllWithData( base, new String[]{base.getPrimaryColumn()}, new SimpleCondition(base.getPrimaryColumn(), primaryKey, Comparison.IS)).count() == 1);
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
		assertArrayEquals( new String[]{"id", "name", "age", "fk_test_id", "other", "created_at", "updated_at"}, store.getAllColumnNames( base.getTableName()) );
	}

	@Test
	public void testInsertNewRecord()
	{
		assertTrue( store.insertNewRecord(base ) > 0);
	}

	@Test
	public void testGetValues_4args()
	{
		store.setValue( base, primaryKey, "name", "Heinz");
		assertEquals("Heinz", store.getValues( base.getTableName(), "name", base.getPrimaryColumn(), primaryKey).findFirst().get());
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
		assertTrue( store.addRow( "mappingTable", new String[]{"fk_test1", "fk_test2"}, new Object[]{primaryKey,primaryKey} ));
	}

	@Test
	public void testRemoveRow()
	{
		assertTrue( store.removeRow( "mappingTable", new SimpleCondition("fk_test1", primaryKey, Comparison.IS)));
	}
}
