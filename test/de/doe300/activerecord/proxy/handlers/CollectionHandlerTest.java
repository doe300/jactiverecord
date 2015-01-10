package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.proxy.handlers.CollectionHandler;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.RecordType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class CollectionHandlerTest extends Assert
{
	private static RecordBase<TestCollectionInterface> base;
	private static CollectionHandler handler;
	private static TestCollectionInterface record;
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		handler = new CollectionHandler();
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), false).buildBase(CollectionHandlerTest.TestCollectionInterface.class, handler);
		record = base.createRecord();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}
	
	public CollectionHandlerTest()
	{
	}

	@Test
	public void testSize()
	{
		assertTrue( record.size() == base.getStore().getAllColumnNames( base.getTableName()).length);
	}

	@Test
	public void testIsEmpty()
	{
		assertFalse( record.isEmpty());
	}

	@Test
	public void testContains()
	{
		assertTrue( record.contains( record.getPrimaryKey()));
	}

	@Test
	public void testIterator()
	{
		int size = 0;
		Iterator<Object> it = record.iterator();
		while(it.hasNext())
		{
			size++;
			assertTrue( record.contains( it.next()));
		}
		assertTrue( size == base.getStore().getAllColumnNames( base.getTableName()).length);
	}

	@Test
	public void testToArray()
	{
		assertTrue( record.toArray().length == base.getStore().getAllColumnNames( base.getTableName()).length);
		Object[] arr = new Object[base.getStore().getAllColumnNames( base.getTableName()).length];
		assertSame( record.toArray(arr), arr);
	}

	@Test(expected = RuntimeException.class)
	public void testAdd()
	{
		record.add( "Test");
	}

	@Test(expected = RuntimeException.class)
	public void testRemove( )
	{
		record.remove( "Tes");
	}

	@Test
	public void testContainsAll()
	{
		record.setName( "Name1");
		Collection<Object> col = Arrays.asList( record.getPrimaryKey(), "Name1" );
		assertTrue( record.containsAll( col));
		record.setName( "name2");
		assertFalse( record.containsAll( col));
	}

	@Test(expected = RuntimeException.class)
	public void testAddAll()
	{
		record.addAll( Arrays.asList( record.getPrimaryKey(), "Name1" ) );
	}

	@Test(expected = RuntimeException.class)
	public void testRetainAll()
	{
		record.retainAll(Arrays.asList( record.getPrimaryKey(), "Name1" ) );
	}

	@Test(expected = RuntimeException.class)
	public void testRemoveAll()
	{
		record.removeAll( Arrays.asList( record.getPrimaryKey(), "Name1" ) );
	}

	@Test(expected = RuntimeException.class)
	public void testClear()
	{
		record.clear();
	}
	
	@RecordType(typeName = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
	public static interface TestCollectionInterface extends TestInterface, Collection<Object>
	{
		
	}
}
