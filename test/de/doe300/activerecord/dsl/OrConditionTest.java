package de.doe300.activerecord.dsl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class OrConditionTest extends Assert
{
	
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2,t3;
	private static OrCondition cond;
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), false).buildBase( TestInterface.class);
		t1 = base.createRecord();
		t1.setName( "123Name1");
		t1.setAge( -912);
		t2 = base.createRecord();
		t2.setName( "123Name1");
		t2.setAge( -913);
		t3 = base.createRecord();
		t3.setName( "123Name4");
		t3.setAge( -913);
		
		cond = new OrCondition(new SimpleCondition("name", "123Name4", Comparison.IS), new SimpleCondition("age",
				-913, Comparison.SMALLER_EQUALS));
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testHasWildcards()
	{
		assertTrue( cond.hasWildcards());
	}

	@Test
	public void testGetValues()
	{
		assertArrayEquals( new Object[]{"123Name4", -913}, cond.getValues());
	}

	@Test
	public void testTest_ActiveRecord()
	{
		assertFalse(cond.test( t1));
		assertTrue( cond.test( t2));
		assertTrue( cond.test( t3));
	}

	@Test
	public void testTest_Map()
	{
		Map<String,Object> map = new HashMap<>(2);
		map.put( "name", "Adam");
		map.put( "age", 100);
		assertFalse( cond.test( map));
		assertTrue( cond.test( Collections.singletonMap( "age", -1000)));
	}
}
