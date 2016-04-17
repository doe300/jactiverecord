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
public class GroupResultTest extends Assert
{
	private static RecordBase<TestInterface> base;
	
	public GroupResultTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTable(TestInterface.class, GroupResultTest.class.getSimpleName());
		base = TestServer.getTestCore().getBase(TestInterface.class).getShardBase( GroupResultTest.class.getSimpleName());
		TestInterface t = base.createRecord();
		t.setName( "Adam5");
		t.setAge( 145);
		t = base.createRecord();
		t.setName( "Adam5");
		t.setAge( 123);
		t = base.createRecord();
		t.setName( "Adam5");
		t.setAge( 122);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTable(TestInterface.class, GroupResultTest.class.getSimpleName());
	}
	
	@Test
	public void testStream()
	{
		assertTrue( getGroup().stream().count() == 3);
	}

	@Test
	public void testWhere()
	{
		GroupResult<String,TestInterface> res = new GroupResult<String,TestInterface>("Adam5", base.find( new SimpleCondition("name", "Adam5", Comparison.IS)), base.count( new SimpleCondition("name", "Adam5", Comparison.IS)), base.getDefaultOrder());
		assertTrue( res.where( new SimpleCondition("name", "Adam5", Comparison.IS)).stream().count() == 3);
	}

	@Test
	public void testLimit()
	{
		GroupResult<String,TestInterface> res = new GroupResult<String,TestInterface>("Adam5", base.find( new SimpleCondition("name", "Adam5", Comparison.IS)), GroupResult.SIZE_UNKNOWN, base.getDefaultOrder());
		assertTrue( res.limit( 2).stream().count() <= 2);
	}

	@Test
	public void testGetOrder() throws Exception
	{
		try(GroupResult<Object, TestInterface> res = getGroup())
		{
			assertTrue( res.getOrder().equals( base.getDefaultOrder()));
		}
	}
	
	@Test
	public void testOrder() throws Exception
	{
		try(GroupResult<Object, TestInterface> res = getGroup())
		{
			assertEquals(122, res.order( SimpleOrder.fromSQLString( "age ASC")).findFirst( null ).getAge() );
		}
		try(GroupResult<Object, TestInterface> res = getGroup())
		{
			assertEquals(145, res.order( SimpleOrder.fromSQLString( "age DESC")).findFirst( null ).getAge() );
		}
	}
	
	private GroupResult<Object, TestInterface> getGroup()
	{
		return base.where( new SimpleCondition("name", null, Comparison.IS_NOT_NULL) ).groupBy( "name").filter( (GroupResult<Object,TestInterface> r) -> r.getKey().equals( "Adam5")).findAny().get();
	}

	@Test
	public void testGetKey() throws Exception
	{
		try(GroupResult<String,TestInterface> res = new GroupResult<String,TestInterface>("Adam5", base.find( new SimpleCondition("name", "Adam5", Comparison.IS)), GroupResult.SIZE_UNKNOWN, base.getDefaultOrder()))
		{
			assertEquals( "Adam5", res.getKey());
		}
	}

	@Test
	public void testSize() throws Exception
	{
		try(GroupResult<String,TestInterface> res = new GroupResult<String,TestInterface>("Adam5", base.find( new SimpleCondition("name", "Adam5", Comparison.IS)), GroupResult.SIZE_UNKNOWN, base.getDefaultOrder()))
		{
			assertTrue( res.getEstimatedSize() == GroupResult.SIZE_UNKNOWN);
		}
	}
}
