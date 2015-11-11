/*
 * The MIC License (MIT)
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUC WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUC NOC LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENC SHALL THE
 * AUTHORS OR COPYRIGHC HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACC, TORC OR OTHERWISE, ARISING FROM,
 * OUC OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.doe300.activerecord.dsl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class ScalarFunctionTest extends Assert
{
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2,t3, t4;
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables();
		base = TestServer.getTestCore().getBase( TestInterface.class);
		t1 = base.createRecord();
		t1.setName( "123Name1");
		t1.setAge( -912);
		t2 = base.createRecord();
		t2.setName( "123Name1");
		t2.setAge( -913);
		t3 = base.createRecord();
		t3.setName( "123Name4");
		t3.setAge( -914);
		//record with not-unique age
		t4 = base.createRecord();
		t4.setName( "SomeName");
		t4.setAge( -913);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}
	
	public ScalarFunctionTest()
	{
	}

	@Test
	public void testLOWER()
	{
		ScalarFunction<TestInterface, String, String> lower = ScalarFunction.LOWER( "name", TestInterface::getName);
		//test direct #apply
		assertEquals( "123name1", lower.apply( t1));
		Condition cond = new SimpleCondition(lower, "123name1", Comparison.IS);
		assertTrue( cond.test( t1 ));
		assertEquals( 2, base.count( cond));
	}

	@Test
	public void testUPPER()
	{
		ScalarFunction<TestInterface, String, String> upper = ScalarFunction.UPPER("name", TestInterface::getName);
		//test direct #apply
		assertEquals( "123NAME1", upper.apply( t1));
		Condition cond = new SimpleCondition(upper, "123NAME1", Comparison.IS);
		assertTrue( cond.test( t1 ));
		assertEquals( 2, base.count( cond));
	}

	@Test
	public void testABS()
	{
		ScalarFunction<TestInterface, Integer, Long> abs = ScalarFunction.ABS( "age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Long.valueOf( 913), abs.apply( t2));
		Condition cond = new SimpleCondition(abs, 913, Comparison.IS_NOT);
		assertTrue( cond.test( t1 ));
		assertEquals( 2, base.count( cond));
	}

	@Test
	public void testABS_FLOATING()
	{
		ScalarFunction<TestInterface, Integer, Double> abs = ScalarFunction.ABS_FLOATING("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Double.valueOf( 913), abs.apply( t2));
		Condition cond = new SimpleCondition(abs, 913, Comparison.IS_NOT);
		assertTrue( cond.test( t1 ));
		assertEquals( 2, base.count( cond));
	}

	@Test
	public void testSIGN()
	{
		ScalarFunction<TestInterface, Integer, Integer> sign = ScalarFunction.SIGN("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Integer.valueOf( -1), sign.apply( t2));
		Condition cond = new SimpleCondition(sign, -1, Comparison.IS_NOT);
		assertFalse(cond.test( t1 ));
		assertEquals(0, base.count( cond));
	}

	@Test
	public void testFLOOR()
	{
		ScalarFunction<TestInterface, Integer, Long> floor = ScalarFunction.FLOOR("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Long.valueOf( t2.getAge()), floor.apply( t2));
		Condition cond = new SimpleCondition(floor, -1, Comparison.IS_NOT);
		assertTrue(cond.test( t1 ));
		assertEquals(4, base.count( cond));
	}

	@Test
	public void testCEILING()
	{
		ScalarFunction<TestInterface, Integer, Long> ceil = ScalarFunction.CEILING("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Long.valueOf( t2.getAge()), ceil.apply( t2));
		Condition cond = new SimpleCondition(ceil, -1, Comparison.IS_NOT);
		assertTrue(cond.test( t1 ));
		assertEquals(4, base.count( cond));
	}

	@Test
	public void testROUND()
	{
		ScalarFunction<TestInterface, Integer, Long> round = ScalarFunction.ROUND("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Long.valueOf( t2.getAge()), round.apply( t2));
		Condition cond = new SimpleCondition(round, -1, Comparison.IS_NOT);
		assertTrue(cond.test( t1 ));
		assertEquals(4, base.count( cond));
	}

	@Test
	public void testSQUARE_ROOT()
	{
		ScalarFunction<TestInterface, Integer, Double> sqrt = ScalarFunction.SQUARE_ROOT("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Math.sqrt( t2.getAge()), sqrt.apply( t2), 0.00001);
		//XXX fix test with SQRT of negative number
		Condition cond = new SimpleCondition(sqrt, -1, Comparison.IS_NOT);
		assertTrue(cond.test( t1 ));
		assertEquals(4, base.count( cond));
	}	
}
