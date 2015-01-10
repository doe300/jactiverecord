package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.proxy.RecordHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class InterfaceProxyHandlerTest extends Assert
{
	private static DummyInterfaceProxyHandler handler;
	
	public InterfaceProxyHandlerTest()
	{
	}
	
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		handler = new DummyInterfaceProxyHandler();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testInvoke()
	{
	}

	@Test
	public void testHandlesMethod()
	{
	}

	public class InterfaceProxyHandlerImpl extends InterfaceProxyHandler
	{

		public InterfaceProxyHandlerImpl()
		{
			super( null );
		}
	}
	
	static class DummyInterfaceProxyHandler extends InterfaceProxyHandler<TestInterface>
	{

		DummyInterfaceProxyHandler()
		{
			super( TestInterface.class );
		}
		
		public TestInterface getDirectionOne(TestInterface record, RecordHandler<TestInterface> handler)
		{
			return record;
		}
		
		public void setDirectionOne(TestInterface record, RecordHandler<TestInterface> handler, TestInterface otherRecord)
		{
		}
	}
}
