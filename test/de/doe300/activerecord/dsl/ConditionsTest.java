package de.doe300.activerecord.dsl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * "Predicate"-Test and "SQL"-Test are the same.
 * But Predicate is already tested in {@link ComparisonTest}
 * @author doe300
 */
public class ConditionsTest extends Assert
{
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2,t3;
	
	@BeforeClass
	public static void init() throws SQLException
	{
		base = RecordCore.fromDatabase( TestInterface.createTestConnection(), false).buildBase( TestInterface.class);
		t1 = base.createRecord();
		t1.setName( "123Name1");
		t1.setAge( -912);
//		t1.save();
		t2 = base.createRecord();
		t2.setName( "123Name1");
		t2.setAge( -913);
//		t2.save();
		t3 = base.createRecord();
		t3.setName( "123Name4");
		t3.setAge( -913);
//		t3.save();
		
	}
	
	@AfterClass
	public static void tearDown()
	{
		t1.destroy();
		t2.destroy();
		t3.destroy();
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
		//TODO
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
		//TODO
	}
	
	@Test
	public void testIsNotNullCondition()
	{
		//TODO
	}
	
	@Test
	public void testLargerCondition()
	{
		//TODO
	}
	
	@Test
	public void testLargerEqualsCondition()
	{
		//TODO
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