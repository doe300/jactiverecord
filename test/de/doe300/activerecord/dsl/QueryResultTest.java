package de.doe300.activerecord.dsl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class QueryResultTest extends Assert
{
	private static RecordBase<TestInterface> base;
	
	public QueryResultTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), false).buildBase( TestInterface.class);
		TestInterface i = base.createRecord();
		i.setName( "Alfons");
		i.setAge( 20);
		base.createRecord().setName( "Johhny");
		base.createRecord().setName( "Adam");
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}
	
	@Test
	public void testStream()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).stream().count() == 3);
	}

	@Test
	public void testWhere()
	{
		assertTrue( base.where( new SimpleCondition("age", base, Comparison.IS_NOT_NULL)).where( new SimpleCondition("age", 20,
				Comparison.SMALLER_EQUALS)).stream().count() == 1);
	}

	@Test
	public void testLimit()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).limit( 2).stream().count() <= 2);
	}

	@Test
	public void testSize()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).size() == 3);
	}

	@Test
	public void testGroupBy_String()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).groupBy( "name").count() == 3);
	}

	@Test
	public void testGroupBy_Function()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).groupBy( (TestInterface i )-> i.getName()).count() == 3);
	}

	@Test
	public void testGetOrder()
	{
	}
	
}
