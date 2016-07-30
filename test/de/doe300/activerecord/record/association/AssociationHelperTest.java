/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 doe300
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.doe300.activerecord.record.association;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.record.ActiveRecord;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class AssociationHelperTest extends TestBase
{
	private final RecordBase<TestInterface> base;
	
	public AssociationHelperTest(final RecordCore core)
	{
		super(core);
		base = core.getBase( TestInterface.class);
		base.findAll().forEach( ActiveRecord::destroy);
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestMappingTables( "mappingTable");
		TestServer.buildTestTables(TestInterface.class, "TESTTABLE");
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, "TESTTABLE");
		TestServer.destroyTestMappingTables( "mappingTable");
	}

	@Test
	public void testGetBelongsTo()
	{
		TestInterface i = base.createRecord();
		base.getStore().setValue( base, i.getPrimaryKey(), "fk_test_id", i.getPrimaryKey());
		assertEquals( i, AssociationHelper.getBelongsTo( i, TestInterface.class, "fk_test_id"));
		//negative test
		TestInterface j = base.createRecord();
		assertNull( AssociationHelper.getBelongsTo( j, TestInterface.class, "fk_test_id"));
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
		assertEquals( i, AssociationHelper.getHasOne(i, TestInterface.class, Conditions.is("fk_test_id", i.getPrimaryKey())));
	}

	@Test
	public void testGetHasMany_3args_1()
	{
		TestInterface i = base.createRecord(), m1 = base.createRecord(), m2 = base.createRecord();
		base.getStore().setValue( base, m1.getPrimaryKey(), "fk_test_id", i.getPrimaryKey());
		base.getStore().setValue( base, m2.getPrimaryKey(), "fk_test_id", i.getPrimaryKey());
		assertEquals(2, AssociationHelper.getHasMany( i, TestInterface.class, "fk_test_id").count());
	}

	@Test
	public void testGetHasMany_3args_2()
	{
		TestInterface i = base.createRecord(), m1 = base.createRecord(), m2 = base.createRecord();
		m1.setName( "Appoloo");
		m2.setName( "Appl");
		assertEquals(2, AssociationHelper.getHasMany( i, TestInterface.class, Conditions.isLike("name", "App%l%")).count());
	}
	
	@Test
	public void testGetHasManyThrough()
	{
		TestInterface i = base.createRecord(), m1 = base.createRecord(), m2 = base.createRecord();
		AssociationHelper.addHasManyThrough(i, m1, "mappingTable", "fk_test1", "fk_test2");
		AssociationHelper.addHasManyThrough(i, m2, "mappingTable", "fk_test1", "fk_test2");
		assertEquals(2, AssociationHelper.getHasManyThrough( i, TestInterface.class, "mappingTable", "fk_test1", "fk_test2").count());
		//negative test
		assertEquals(0, AssociationHelper.getHasManyThrough( m1, TestInterface.class, "mappingTable", "fk_test1", "fk_test2").count());
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
		assertEquals(2, AssociationHelper.getHasMany( i, TestInterface.class, "fk_test_id").count());
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

	@Test
	public void testGetConditionSet()
	{
		RecordSet<TestInterface> set = AssociationHelper.getConditionSet( base, "name", "Adam", "Eve");
		base.createRecord().setName( "Adam");
		assertFalse( set.isEmpty());
		int size = set.size();
		set.remove( set.first());
		assertSame( size-1, set.size());
		
		set.clear();
		assertTrue(set.isEmpty());
	}
}
