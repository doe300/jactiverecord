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
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.jdbc.VendorSpecific;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class OrConditionTest extends Assert
{
	
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2,t3;
	private static OrCondition cond;
	
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
		
		cond = new OrCondition(new SimpleCondition("name", "123Name4", Comparison.IS), new SimpleCondition("age",
				-913, Comparison.SMALLER_EQUALS));
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}
	
	@Test
	public void testOrUnrolling()
	{
		Condition c1 = new OrCondition(cond);
		assertEquals( cond.toSQL( VendorSpecific.HSQLDB), c1.toSQL( VendorSpecific.HSQLDB ));
	}

	@Test
	public void testHasWildcards()
	{
		assertTrue( cond.hasWildcards());
		Condition c = new OrCondition(new SimpleCondition("name", null, Comparison.IS_NOT_NULL), new SimpleCondition("age", null, Comparison.IS_NULL));
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
		assertTrue( cond.test( Collections.singletonMap( "age", -1000)));
	}

	@Test
	public void testNegate()
	{
		Condition invCond = cond.negate();
		assertTrue(invCond.test( t1));
		assertFalse(invCond.test( t2));
		assertFalse( invCond.test( t3));
	}
}
