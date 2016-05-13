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

import de.doe300.activerecord.AssertException;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class OrConditionTest extends TestBase implements AssertException
{
	
	private final RecordBase<TestInterface> base;
	private final TestInterface t1, t2,t3;
	private final Condition cond;
	
	public OrConditionTest(final RecordCore core)
	{
		super(core);
		
		base = core.getBase( TestInterface.class).getShardBase( OrConditionTest.class.getSimpleName());
		base.findAll().forEach( ActiveRecord::destroy);
		t1 = base.createRecord();
		t1.setName( "123Name1");
		t1.setAge( -912);
		t2 = base.createRecord();
		t2.setName( "123Name1");
		t2.setAge( -913);
		t3 = base.createRecord();
		t3.setName( "123Name4");
		t3.setAge( -913);
		
		cond = Conditions.or(Conditions.is("name", "123Name4"), Conditions.isSmallerEquals( "age",-913));
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables( TestInterface.class, OrConditionTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables( TestInterface.class, OrConditionTest.class.getSimpleName());
	}
	
	@Test
	public void testOrError()
	{
		assertThrows(IllegalArgumentException.class, () -> Conditions.or( new Condition[0]));
	}
	
	@Test
	public void testOrConditions()
	{
		Condition c1 = Conditions.or(cond);
		//test OR-unrolling
		assertEquals( cond.toSQL( JDBCDriver.guessDriver( null ), null), c1.toSQL( JDBCDriver.guessDriver( null ), null ));
		//test skip duplicates
		Condition s1 = Conditions.is("test", "dummy");
		Condition c2 = Conditions.or(s1, s1);
		assertSame( s1, c2);
		//test skip non-false
		Condition s2 = Conditions.isTrue();
		Condition c3 = Conditions.or(s1, s2);
		assertSame( s2, c3);
		//test unwrap single condition
		Condition c4 = Conditions.or(s2);
		assertSame( s2, c4);
		//test skip nulls
		assertSame(s1, Conditions.or( s1, null, null, null));
		
		assertThrows( IllegalArgumentException.class, () -> Conditions.or( new Condition[0]));
	}

	@Test
	public void testHasWildcards()
	{
		assertTrue( cond.hasWildcards());
		Condition c = Conditions.or(Conditions.isNotNull("name"), Conditions.isNull("age"));
		assertFalse( c.hasWildcards());
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
		assertThrows(IllegalArgumentException.class, () -> cond.test( Collections.singletonMap( "age", -1000)));
	}

	@Test
	public void testNegate()
	{
		Condition invCond = cond.negate();
		assertTrue(invCond.test( t1));
		assertFalse(invCond.test( t2));
		assertFalse( invCond.test( t3));
	}
	
	@Test
	public void testEquals()
	{
		final Condition cond1 = Conditions.or( cond);
		assertEquals( cond, cond1 );
		
		assertNotEquals( cond, new Object());
	}
}
