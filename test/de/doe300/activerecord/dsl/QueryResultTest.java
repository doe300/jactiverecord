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
public class QueryResultTest extends Assert
{
	private static RecordBase<TestInterface> base;
	
	public QueryResultTest()
	{
	}
	
	@BeforeClass
	public static void build() throws SQLException
	{
		base = RecordCore.fromDatabase( TestInterface.createTestConnection(), false).buildBase( TestInterface.class);
	}

	@Test
	public void testStream()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).stream().count() > 2);
	}

	@Test
	public void testWhere()
	{
		assertTrue( base.where( new SimpleCondition("age", base, Comparison.IS_NOT_NULL)).where( new SimpleCondition("age", 20,
				Comparison.SMALLER_EQUALS)).stream().count() > 1);
	}

	@Test
	public void testLimit()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).limit( 2).stream().count() <= 2);
	}

	@Test
	public void testSize()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).size() == base.find( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).count());
	}

	@Test
	public void testGroupBy_String()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).groupBy( "name").count() > 1);
	}

	@Test
	public void testGroupBy_Function()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).groupBy( (TestInterface i )-> i.getName()).count() > 2);
	}

	@Test
	public void testGetOrder()
	{
	}
	
}
