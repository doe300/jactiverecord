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
 * "Predicate"-Test and "SQL"-Test are the same.
 * But Predicate is already tested in {@link ComparisonTest}
 * @author doe300
 */
public class SimpleConditionTest extends Assert
{
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2,t3;
	
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
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}
	
	@Test
	public void testAndCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new AndCondition(new SimpleCondition("age", -912, Comparison.IS ), new SimpleCondition("name", "123Name1", Comparison.IS))) );
		//test SQL
		assertEquals((Integer)t1.getPrimaryKey()	, base.getStore().findFirst( base,  new AndCondition(new SimpleCondition("age", -912, Comparison.IS ), new SimpleCondition("name", "123Name1", Comparison.IS)) ));
	}
	
	@Test
	public void testOrCondition()
	{
		//test Predicate
		assertSame( t3, base.findFirst( new OrCondition(new SimpleCondition("age", -910, Comparison.IS), new SimpleCondition("name",
				"123Name4", Comparison.IS))));
		//test SQL
		assertEquals( (Integer)t3.getPrimaryKey(), base.getStore().findFirst( base, new OrCondition(new SimpleCondition("age", -910, Comparison.IS), new SimpleCondition("name",
				"123Name4", Comparison.IS))));
	}
	
	@Test
	public void testIsCondition()
	{
		//test Predicate
		assertSame( t3, base.findFirst( new SimpleCondition("name", "123Name4", Comparison.IS)));
		//test SQL
		assertEquals( (Integer)t3.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("name", "123Name4", Comparison.IS)));
	}
	
	@Test
	public void testIsNotCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("other", null, Comparison.IS_NULL)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("other", null,Comparison.IS_NULL)));
	}
	
	@Test
	public void testLikeCondition()
	{
		//test Predicate
		assertSame( t3, base.findFirst( new SimpleCondition("name", "1%3Name4", Comparison.LIKE)));
		//test SQL
		assertEquals( (Integer)t3.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("name", "1%3Name4", Comparison.LIKE)));
	}
	
	@Test
	public void testIsNullCondition()
	{
		//test Predicate
		assertSame( t3, base.findFirst( new SimpleCondition("name", "123Name1", Comparison.IS_NOT)));
		//test SQL
		assertEquals( (Integer)t3.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("name", "123Name1", Comparison.IS_NOT)));
	}
	
	@Test
	public void testIsNotNullCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("name", null, Comparison.IS_NOT_NULL)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("name", null,Comparison.IS_NOT_NULL)));
	}
	
	@Test
	public void testLargerCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("age", -913, Comparison.LARGER)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("age", -913, Comparison.LARGER)));
	}
	
	@Test
	public void testLargerEqualsCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("age", -913, Comparison.LARGER_EQUALS)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("age", -913, Comparison.LARGER_EQUALS)));
	}
	
	@Test
	public void testSmallerCondition()
	{
		//test Predicate
		assertSame( t2, base.findFirst( new SimpleCondition("age", -912, Comparison.SMALLER)));
		//test SQL
		assertEquals( (Integer)t2.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("age", -912, Comparison.SMALLER)));
	}
	
	@Test
	public void testSmallerEqualsCondition()
	{
		//test Predicate
		assertSame( t2, base.findFirst( new SimpleCondition("age", -913, Comparison.SMALLER_EQUALS)));
		//test SQL
		assertEquals( (Integer)t2.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("age", -913, Comparison.SMALLER_EQUALS)));
	}
	
	@Test
	public void testInCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("age", new Integer[]{-912,-913}, Comparison.IN)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, new SimpleCondition("age", new Integer[]{-912,-913}, Comparison.IN)));
	}
}
