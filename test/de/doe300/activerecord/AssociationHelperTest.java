package de.doe300.activerecord;

import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import java.sql.SQLException;
import org.junit.Assert;
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
		base = RecordCore.fromDatabase( TestInterface.createTestConnection(), false).buildBase( TestInterface.class);
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
		assertTrue( AssociationHelper.getHasMany( i, TestInterface.class, new SimpleCondition("name", "App%l%", Comparison.LIKE)).count() >= 2);
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
	}
	
}
