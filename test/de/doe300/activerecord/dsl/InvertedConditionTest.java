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
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;



public class InvertedConditionTest extends Assert
{
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2,t3;
	
	public InvertedConditionTest()
	{
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables();
		base = TestServer.getTestCore().getBase( TestInterface.class);
		t1 = base.createRecord();
		t1.setName( "123Name1");
		t1.setAge( 912);
		t2 = base.createRecord();
		t2.setName( "123Name1");
		t2.setAge( 913);
		t3 = base.createRecord();
		t3.setName( "123Name4");
		t3.setAge( 913);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testSimpleCondition()
	{
		//matches t2 and t3
		Condition cond = new SimpleCondition("age", 913, Comparison.IS);
		//matches t1
		assertSame( t1, base.findFirst( InvertedCondition.invertCondition(cond )));
	}

	@Test
	public void testAndCondition()
	{
		//matches t3
		Condition cond = AndCondition.andConditions(new SimpleCondition("age", 913, Comparison.IS), new SimpleCondition("name", "123Name4", Comparison.IS) );
		//matches t1 and t2
		assertTrue( base.find( InvertedCondition.invertCondition(cond)).count() >= 2 );
		assertFalse( base.find( InvertedCondition.invertCondition(cond)).anyMatch( (TestInterface i) -> i.equals( t3)));
	}
	
	@Test
	public void testInvertedCondition()
	{
		//matches t1
		Condition cond = InvertedCondition.invertCondition(new SimpleCondition("age", 913, Comparison.IS));
		//matches t2 and t3
		assertTrue( base.find( InvertedCondition.invertCondition(cond)).count() >= 2 );
		assertFalse( base.find( InvertedCondition.invertCondition(cond)).anyMatch( (TestInterface i) -> i.equals( t1)));
	}

	@Test
	public void testTest_ActiveRecord()
	{
		Condition cond = InvertedCondition.invertCondition(new SimpleCondition("age", 913, Comparison.IS));
		//t1 has age of non-913
		assertTrue( cond.test( t1));
		assertFalse( cond.test( t2));
	}

	@Test
	public void testTest_Map()
	{
		Condition cond = InvertedCondition.invertCondition(new SimpleCondition("age", 913, Comparison.IS));
		assertTrue( cond.test( Collections.singletonMap( "age", 912)));
		assertFalse( cond.test( Collections.singletonMap( "age", 913)));
	}

	@Test
	public void testNegate()
	{
		Condition cond = new SimpleCondition("age", 913, Comparison.IS);
		Condition invCond = InvertedCondition.invertCondition(cond);
		assertSame( cond, invCond.negate());
	}
}
