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
package de.doe300.activerecord.dsl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */


public class HasManyThroughConditionTest extends Assert
{
	private static final String mappingTable = "mappingTable" + HasManyThroughConditionTest.class.getSimpleName();
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2, t3;
	private static Condition cond1, cond2, cond3;
	
	public HasManyThroughConditionTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestMappingTable( mappingTable );
		TestServer.buildTestTable(TestInterface.class, HasManyThroughConditionTest.class.getSimpleName());
		
		base = TestServer.getTestCore().getBase( TestInterface.class).getShardBase( HasManyThroughConditionTest.class.getSimpleName());
		t1 = base.createRecord();
		t1.setName( "123Name1");
		t1.setAge( -912);
		t2 = base.createRecord();
		t2.setName( "123Name1");
		t2.setAge( -913);
		t3 = base.createRecord();
		t3.setName( "123Name4");
		t3.setAge( -913);
		//associations
		base.getStore().addRow( mappingTable, new String[]{"fk_test1", "fk_test2"}, new Object[]{t1.getPrimaryKey(), t2.getPrimaryKey()} );
		base.getStore().addRow( mappingTable, new String[]{"fk_test1", "fk_test2"}, new Object[]{t2.getPrimaryKey(), t3.getPrimaryKey()} );
		base.getStore().addRow( mappingTable, new String[]{"fk_test1", "fk_test2"}, new Object[]{t3.getPrimaryKey(), t3.getPrimaryKey()} );
		
		//has no entries
		cond1 = new HasManyThroughCondition(base, mappingTable, "fk_test1", "fk_test2", "id", base, new SimpleCondition("age", 200, Comparison.IS));
		//matches all
		cond2 = new HasManyThroughCondition(base, mappingTable, "fk_test1", "fk_test2", "id", base, new SimpleCondition("name", null, Comparison.IS_NOT_NULL));
		//matches t2 and t3
		cond3 = new HasManyThroughCondition(base, mappingTable, "fk_test1", "fk_test2", "id", base, new SimpleCondition("name", "123Name4", Comparison.IS));
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTable(TestInterface.class, HasManyThroughConditionTest.class.getSimpleName());
		TestServer.destroyTestMappingTable( mappingTable);
	}

	@Test
	public void testHasWildcards()
	{
		assertFalse( cond2.hasWildcards());
		assertTrue( cond1.hasWildcards());
	}

	@Test
	public void testToSQL()
	{
		assertEquals( 0, base.count( cond1));
		assertEquals( 3, base.count( cond2 ));
		assertEquals( 2, base.count( cond3));
		try(final Stream<TestInterface> s = base.find( cond3))
		{
			assertTrue( s.anyMatch( (TestInterface i) -> i.equals( t2)));
		}
		try(final Stream<TestInterface> s = base.find( cond3))
		{
			assertFalse( s.anyMatch( (TestInterface i) -> i.equals( t1)));
		}
	}

	@Test
	public void testTest_ActiveRecord()
	{
		assertFalse( cond1.test( t1));
		assertFalse( cond1.test( t2));
		assertFalse( cond1.test( t3));
		
		assertTrue( cond2.test( t1));
		assertTrue( cond2.test( t2));
		assertTrue( cond2.test( t3));
		
		assertFalse( cond3.test( t1));
		assertTrue( cond3.test( t2));
		assertTrue( cond3.test( t3));
		
		assertFalse( cond1.test( (ActiveRecord)null));
	}

	@Test
	public void testTest_Map()
	{
		assertFalse( cond1.test( new HashMap<String, Object>(0) ));
		assertTrue( cond2.test( Collections.singletonMap( "id", t1.getPrimaryKey())));
	}

	@Test
	public void testNegate()
	{
		Condition invCond = cond3.negate();
		
		assertTrue( invCond.test( t1));
		assertFalse( invCond.test( t2));
		assertFalse( invCond.test( t3));
	}

}
