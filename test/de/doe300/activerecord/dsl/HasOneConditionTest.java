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
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class HasOneConditionTest extends TestBase
{
	private final RecordBase<TestInterface> base;
	private final TestInterface t1, t2,t3;
	private final Condition cond1, cond2, cond3;
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, HasOneConditionTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, HasOneConditionTest.class.getSimpleName());
	}
	
	public HasOneConditionTest(final RecordCore core)
	{
		super(core);
		
		base = core.getBase( TestInterface.class).getShardBase( HasOneConditionTest.class.getSimpleName());
		base.getAll().forEach( ActiveRecord::destroy);
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
		t1.setDirectionOther( t2);
		t2.setDirectionOther( t3);
		//the linking from t3 to t2 is overwritten in the next line
		t3.setDirectionOther( t3);
		
		//has no entries
		cond1 = new HasOneCondition("id", base, "fk_test_id", Conditions.isNull("name"));
		//matches t1 and t3
		cond2 = new HasOneCondition("id", base, "fk_test_id", Conditions.is("age", -913));
		//matches t3
		cond3 = new HasOneCondition("id", base, "fk_test_id", Conditions.is("id", t3.getPrimaryKey()));
	}
	
	@Before
	public void resetObjects()
	{
		t1.setDirectionOther( t2);
		t2.setDirectionOther( t3);
		t3.setDirectionOther( t3);
	}
	
	@Test
	public void testHasWildcards()
	{
		assertFalse( cond1.hasWildcards());
		assertTrue( cond2.hasWildcards());
	}

	@Test
	public void testToSQL()
	{
		assertEquals( 0, base.count( cond1));
		assertEquals( 2, base.count( cond2 ));
		assertEquals( 1, base.count( cond3));
		try(final Stream<TestInterface> s = base.find( cond3))
		{
			assertTrue( s.anyMatch( (TestInterface i) -> i.equals( t3)));
		}
		try(final Stream<TestInterface> s = base.find( cond3))
		{
			assertFalse(s.anyMatch( (TestInterface i) -> i.equals( t1)));
		}
	}

	@Test
	public void testTest_ActiveRecord()
	{
		assertFalse( cond1.test( t1));
		assertFalse( cond1.test( t2));
		assertFalse( cond1.test( t3));
		
		assertTrue( cond2.test( t1));
		assertFalse( cond2.test( t2));
		assertTrue( cond2.test( t3));
		
		assertFalse( cond3.test( t1));
		assertFalse( cond3.test( t2));
		assertTrue( cond3.test( t3));
		
		assertFalse( cond3.test( (ActiveRecord)null));
	}

	@Test
	public void testTest_Map()
	{
		assertFalse( cond1.test( new HashMap<String, Object>(0) ));
		assertTrue( cond3.test( Collections.singletonMap( "id", t3.getPrimaryKey())));
	}

	@Test
	public void testNegate()
	{
		Condition invCond = cond3.negate();
		
		assertTrue( invCond.test( t1));
		assertTrue( invCond.test( t2));
		assertFalse( invCond.test( t3));
	}

}
