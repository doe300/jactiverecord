package de.doe300.activerecord.record.association;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class AssociationHelperTest extends Assert
{
	private final RecordBase<TestInterface> base;
	
	public AssociationHelperTest() throws SQLException
	{
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), false).buildBase( TestInterface.class);
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testGetBelongsTo()
	{
		TestInterface i = base.createRecord();
		base.getStore().setValue( base, i.getPrimaryKey(), "fk_test_id", i.getPrimaryKey());
		assertEquals( i, AssociationHelper.getBelongsTo( i, TestInterface.class, "fk_test_id"));
	}

	@Test
	public void testGetHasOne_3args_1()
	{
		TestInterface i = base.createRecord();
		base.getStore().setValue( base, i.getPrimaryKey(), "fk_test_id", i.getPrimaryKey());
		assertEquals( i, AssociationHelper.getHasOne(i, TestInterface.class, "fk_test_id"));
	}

	@Test
	public void testGetHasOne_3args_2()
	{
		TestInterface i = base.createRecord();
		base.getStore().setValue( base, i.getPrimaryKey(), "fk_test_id", i.getPrimaryKey());
		assertEquals( i, AssociationHelper.getHasOne(i, TestInterface.class, new SimpleCondition("fk_test_id", i.getPrimaryKey(), Comparison.IS)));
	}

	@Test
	public void testGetHasMany_3args_1()
	{
		TestInterface i = base.createRecord(), m1 = base.createRecord(), m2 = base.createRecord();
		base.getStore().setValue( base, m1.getPrimaryKey(), "fk_test_id", i.getPrimaryKey());
		base.getStore().setValue( base, m2.getPrimaryKey(), "fk_test_id", i.getPrimaryKey());
		assertTrue( AssociationHelper.getHasMany( i, TestInterface.class, "fk_test_id").count() == 2);
	}

	@Test
	public void testGetHasMany_3args_2()
	{
		TestInterface i = base.createRecord(), m1 = base.createRecord(), m2 = base.createRecord();
		m1.setName( "Appoloo");
		m2.setName( "Appl");
		assertTrue( AssociationHelper.getHasMany( i, TestInterface.class, new SimpleCondition("name", "App%l%", Comparison.LIKE)).count() == 2);
	}
	
	@Test
	public void testGetHasManyThrough()
	{
	}

	@Test
	public void testSetBelongsTo() throws Exception
	{
		TestInterface i = base.createRecord();
		AssociationHelper.setBelongsTo( i, i, "fk_test_id");
		assertEquals( i, AssociationHelper.getBelongsTo( i, TestInterface.class, "fk_test_id"));
	}

	@Test
	public void testSetHasOne() throws Exception
	{
		TestInterface i = base.createRecord();
		AssociationHelper.setHasOne( i, i, "fk_test_id");
		assertEquals( i, AssociationHelper.getHasOne(i, TestInterface.class, "fk_test_id"));
	}

	@Test
	public void testAddHasMany() throws Exception
	{
		TestInterface i = base.createRecord(), m1 = base.createRecord(), m2 = base.createRecord();
		AssociationHelper.addHasMany( i, m1, "fk_test_id");
		AssociationHelper.addHasMany( i, m2, "fk_test_id");
		assertTrue( AssociationHelper.getHasMany( i, TestInterface.class, "fk_test_id").count() == 2);
	}

	@Test
	public void testAddHasManyThrough()
	{
		TestInterface i = base.createRecord(), i2 = base.createRecord();
		assertTrue( AssociationHelper.addHasManyThrough( i, i2, "mappingTable", "fk_test1", "fk_test2"));
	}
	
	@Test
	public void testRemoveHasManyThrough()
	{
		TestInterface i = base.createRecord(), i2 = base.createRecord();
		assertTrue( AssociationHelper.addHasManyThrough( i, i2, "mappingTable", "fk_test1", "fk_test2"));
		assertTrue( AssociationHelper.removeHasManyThrough( i, i2, "mappingTable", "fk_test1", "fk_test2"));
	}

	@Test
	public void testGetHasManySet()
	{
		TestInterface i = base.createRecord(), i2 = base.createRecord();
		AssociationHelper.setHasOne( i, i2, "fk_test_id");
		AssociationHelper.setHasOne( i, i, "fk_test_id");
		assertEquals( 2, AssociationHelper.getHasManySet( i, TestInterface.class, "fk_test_id").size());
	}

	@Test
	public void testGetHasManyThroughSet()
	{
		TestInterface i = base.createRecord(), i2 = base.createRecord();
		assertTrue( AssociationHelper.addHasManyThrough( i, i2, "mappingTable", "fk_test1", "fk_test2"));
		assertTrue( AssociationHelper.addHasManyThrough( i, i, "mappingTable", "fk_test1", "fk_test2"));
		assertEquals( 2, AssociationHelper.getHasManyThroughSet( i, TestInterface.class, "mappingTable", "fk_test1", "fk_test2").size());
	}
}