package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class CachedJDBCRecordStoreTest extends Assert
{
	private static CachedJDBCRecordStore store;
	private static RecordBase<TestInterface> base;
	private static int primaryKey;
	
	public CachedJDBCRecordStoreTest()
	{
	}
	
	@BeforeClass
	public static void init() throws SQLException, Exception
	{
		store = new CachedJDBCRecordStore(TestInterface.createTestConnection());
		base = RecordCore.fromStore( "Test1", store).buildBase(TestInterface.class);
		assertNotNull( base );
		primaryKey = base.createRecord().getPrimaryKey();
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
	}

	@Test
	public void testGetValues()
	{
		store.setValue( base, primaryKey, "name", "Tom");
		Map<String,Object> m = store.getValues( base, primaryKey, new String[]{base.getPrimaryColumn(), "name"});
		assertEquals( "Tom", m.get( "name"));
		assertEquals( primaryKey, m.get( base.getPrimaryColumn()));
	}

	@Test
	public void testSave()
	{
		store.setValue( base, primaryKey, "name", "Alex");
		assertTrue( store.save( base, primaryKey ));
		assertFalse( store.save( base, primaryKey));
		store.clearCache( base, primaryKey );
		assertEquals( "Alex", store.getValue( base, primaryKey, "name"));
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
		int key = store.insertNewRecord( base);
		assertTrue( key >= 0);
		assertTrue( store.containsRecord( base, key));
		store.setValue( base, key, "name", "Adam");
		store.destroy( base, key );
		assertFalse( store.containsRecord( base, key));
	}

	@Test
	public void testFindFirstWithData()
	{
		assertTrue(store.findFirstWithData( base, base.getDefaultColumns(), new SimpleCondition(base.getPrimaryColumn(), primaryKey, Comparison.IS) ).size()>=base.getDefaultColumns().length);
	}

	@Test
	public void testStreamAllWithData()
	{
		assertTrue( store.streamAllWithData( base, new String[]{base.getPrimaryColumn()}, new SimpleCondition(base.getPrimaryColumn(), primaryKey, Comparison.IS)).count() == 1);
	}

	@Test
	public void testGetAllColumnNames()
	{
		assertArrayEquals( new String[]{"id", "name", "age", "fk_test_id", "other", "created_at", "updated_at"}, store.getAllColumnNames( base.getTableName()) );
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
	
}

