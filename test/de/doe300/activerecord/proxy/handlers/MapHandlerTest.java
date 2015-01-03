package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.record.DataSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class MapHandlerTest extends Assert
{
	private static RecordBase<TestMapInterface> base;
	private static MapHandler handler;
	private static TestMapInterface record;
	
	public MapHandlerTest()
	{
	}
	
	@BeforeClass
	public static void init() throws SQLException, Exception
	{
		handler = new MapHandler();
		base = RecordCore.fromDatabase( TestInterface.createTestConnection(), false).buildBase( TestMapInterface.class, handler);
		record = base.createRecord();
	}

	@Test
	public void testSize()
	{
		assertSame( base.getStore().getAllColumnNames( base.getTableName()).length, record.size());
	}

	@Test
	public void testIsEmpty()
	{
		assertFalse( record.isEmpty());
	}

	@Test
	public void testContainsKey()
	{
		assertTrue( record.containsKey( "name"));
		assertFalse( record.containsKey( "birthdate"));
	}

	@Test
	public void testContainsValue()
	{
		assertTrue( record.containsValue( record.getPrimaryKey()));
		assertFalse( record.containsValue( record));
	}

	@Test
	public void testGet()
	{
		record.setName( "Klaus");
		assertSame( record.getName(), record.get( "name"));
	}

	@Test(expected = RuntimeException.class)
	public void testPut()
	{
		record.put( "age", 112);
		assertSame( record.getAge(), record.get( "age"));
		int key = record.getPrimaryKey();
		record.put( base.getPrimaryColumn(), 01);
		assertEquals(key, record.getPrimaryKey());
		record.put( "some_key", 34);
	}

	@Test(expected = RuntimeException.class)
	public void testRemove()
	{
		record.remove( "name");
	}

	@Test
	public void testPutAll()
	{
		Map<String, Object> m = new HashMap<>(2);
		m.put( "name", "Adam");
		m.put( "age", 0);
		record.putAll( m );
		assertSame( m.get( "name"), record.get( "name"));
		assertSame( m.get( "age"), record.getAge());
	}

	@Test(expected = RuntimeException.class)
	public void testClear()
	{
		record.clear();
	}

	@Test
	public void testKeySet()
	{
		assertTrue( Arrays.asList(base.getStore().getAllColumnNames( base.getTableName())).containsAll( record.keySet()));
		assertTrue( record.keySet().containsAll( Arrays.asList(base.getStore().getAllColumnNames( base.getTableName()))));
	}

	@Test
	public void testValues()
	{
		assertTrue( record.values().size() == base.getStore().getAllColumnNames( base.getTableName()).length);
	}

	@Test
	public void testEntrySet()
	{
		assertTrue( record.entrySet().size() == base.getStore().getAllColumnNames( base.getTableName()).length);
		for(Map.Entry<String,Object> e:record.entrySet())
		{
			assertEquals( base.getStore().getValue( base, record.getPrimaryKey(), e.getKey()), e.getValue());
		}
	}
	
	
	@DataSet(dataSet = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
	public static interface TestMapInterface extends TestInterface, Map<String, Object>
	{
		
	}
}
