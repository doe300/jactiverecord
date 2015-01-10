package de.doe300.activerecord;

import de.doe300.activerecord.proxy.handlers.MapHandler;
import de.doe300.activerecord.store.impl.MapRecordStore;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class RecordCoreTest extends Assert
{
	private static RecordCore core;
	
	public RecordCoreTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		core = RecordCore.fromDatabase( TestServer.getTestConnection(), true);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
		core.close();
	}
	
	@Test
	public void testFromDatabase() throws Exception
	{
		assertNotNull( RecordCore.fromDatabase( TestServer.getTestConnection(), false));
	}

	@Test
	public void testNewMemoryStore() throws Exception
	{
		assertNotNull( RecordCore.newMemoryStore( "test1"));
	}

	@Test
	public void testFromStore() throws Exception
	{
		assertNotNull( RecordCore.fromStore( "test2", new MapRecordStore()));
	}

	@Test
	public void testGetCore()
	{
		assertNotNull( RecordCore.getCore( "PUBLIC"));
	}

	@Test
	public void testBuildBase()
	{
		assertNotNull( core.buildBase( TestInterface.class, new MapHandler()) );
		assertEquals(core.buildBase( TestInterface.class), core.getBase( TestInterface.class));
	}

	@Test
	public void testGetBase()
	{
		assertNotNull( core.getBase( TestPOJO.class));
	}
}
