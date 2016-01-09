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
import org.junit.AfterClass;
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
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		base = TestServer.getTestCore().getBase( TestInterface.class);
		TestInterface i = base.createRecord();
		i.setName( "Alfons");
		i.setAge( 20);
		i = base.createRecord();
		i.setName( "Johhny");
		i.setAge( 23);
		i = base.createRecord();
		i.setName( "Adam");
		i.setAge( -123);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}
	
	@Test
	public void testStream()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).stream().count() == 3);
	}

	@Test
	public void testWhere()
	{
		assertEquals(2, base.where( new SimpleCondition("age", base, Comparison.IS_NOT_NULL)).where( new SimpleCondition("age", 20,
				Comparison.SMALLER_EQUALS)).stream().count());
	}

	@Test
	public void testLimit()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).limit( 2).stream().count() <= 2);
	}

	@Test
	public void testSize()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).getEstimatedSize() == 3);
	}

	@Test
	public void testGroupBy_String()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).groupBy( "name").count() == 3);
	}

	@Test
	public void testGroupBy_Function()
	{
		assertTrue( base.where( new SimpleCondition("name", base, Comparison.IS_NOT_NULL)).groupBy( (TestInterface i )-> i.getName()).count() == 3);
	}

	@Test
	public void testGetOrder()
	{
		assertSame( base.getDefaultOrder(), base.where( new SimpleCondition("name", null, Comparison.IS_NOT_NULL)).getOrder());
		//XXX does not close stream
	}
	
	@Test
	public void testOrder()
	{
		assertEquals( -123, base.where( new SimpleCondition("name", null, Comparison.IS_NOT_NULL)).order( SimpleOrder.
				fromSQLString( "age ASC")).findFirst( null ).getAge());
		assertEquals( 23, base.where( new SimpleCondition("name", null, Comparison.IS_NOT_NULL)).order( new SimpleOrder("age", SimpleOrder.OrderType.DESCENDING)).findFirst( null ).getAge());
	}
}	
