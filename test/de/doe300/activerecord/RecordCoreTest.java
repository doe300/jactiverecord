package de.doe300.activerecord;

import de.doe300.activerecord.proxy.handlers.MapHandler;
import de.doe300.activerecord.store.impl.MapRecordStore;
import java.sql.SQLException;
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
	public static void init() throws SQLException
	{
		core = RecordCore.fromDatabase( TestInterface.createTestConnection(), true);
	}
	
	@AfterClass
	public static void tearDown() throws Exception
	{
		core.close();
	}
	
	@Test
	public void testFromDatabase() throws Exception
	{
		RecordCore.fromDatabase( TestInterface.createTestConnection(), false).close();
	}

	@Test
	public void testNewMemoryStore() throws Exception
	{
		RecordCore.newMemoryStore( "test1").close();
	}

	@Test
	public void testFromStore() throws Exception
	{
		RecordCore.fromStore( "test2", new MapRecordStore()).close();
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
		assertSame( core.buildBase( TestInterface.class), core.getBase( TestInterface.class));
	}

	@Test
	public void testGetBase()
	{
		assertNotNull( core.getBase( TestInterface.class));
	}
}
