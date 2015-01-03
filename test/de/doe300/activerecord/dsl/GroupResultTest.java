package de.doe300.activerecord.dsl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class GroupResultTest extends Assert
{
	private static RecordBase<TestInterface> base;
	
	public GroupResultTest()
	{
	}
	
	@BeforeClass
	public static void init() throws SQLException, Exception
	{
		base = RecordCore.fromDatabase( TestInterface.createTestConnection(), true).buildBase(TestInterface.class);
		base.createRecord().setName( "Adam5");
		base.createRecord().setName( "Adam5");
		base.createRecord().setName( "Adam5");
	}

	@Test
	public void testStream()
	{
		assertTrue(base.where( new SimpleCondition("name", null, Comparison.IS_NOT_NULL)).
				groupBy( "name").filter( (GroupResult<Object,TestInterface> r) -> r.getKey().equals( "Adam5")).
				anyMatch( (GroupResult<Object,TestInterface> r) -> r.stream().count() >= 3));
	}

	@Test
	public void testWhere()
	{
		GroupResult<String,TestInterface> res = new GroupResult<String,TestInterface>("Adam5", base.find( new SimpleCondition("name", "Adam5", Comparison.IS)), base.count( new SimpleCondition("name", "Adam5", Comparison.IS)), base.getDefaultOrder());
		assertTrue( res.where( new SimpleCondition("name", "Adam5", Comparison.IS)).stream().count() == res.size());
	}

	@Test
	public void testLimit()
	{
		GroupResult<String,TestInterface> res = new GroupResult<String,TestInterface>("Adam5", base.find( new SimpleCondition("name", "Adam5", Comparison.IS)), GroupResult.SIZE_UNKNOWN, base.getDefaultOrder());
		assertTrue( res.limit( 2).stream().count() <= 2);
	}

	@Test
	public void testGetOrder()
	{
		assertTrue(base.where( new SimpleCondition("name", null, Comparison.IS_NOT_NULL)).groupBy( "name").filter( (GroupResult<Object,TestInterface> r) -> r.getKey().equals( "Adam5")).
				allMatch( (GroupResult<Object,TestInterface> r) -> r.getOrder().equals( base.getDefaultOrder())));
	}
	
}
