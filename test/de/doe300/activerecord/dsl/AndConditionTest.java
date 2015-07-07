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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AndConditionTest extends Assert
{
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2,t3;
	
	public AndConditionTest()
	{
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables();
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), false).buildBase( TestInterface.class);
		t1 = base.createRecord();
		t1.setName( "123Name1");
		t1.setAge( 912);
		t2 = base.createRecord();
		t2.setName( "123Name1");
		t2.setAge( 913);
		t3 = base.createRecord();
		t3.setName( "123Name4");
		t3.setAge( 914);
	}
	
	@Test
	public void testAndUnrolling()
	{
		Condition simpleCond = new SimpleCondition(base.getPrimaryColumn(), 3, Comparison.SMALLER_EQUALS);
		Condition c1 = AndCondition.andConditions(simpleCond );
		Condition c2 = AndCondition.andConditions(c1);
		assertEquals( c1.toSQL( VendorSpecific.MYSQL), c2.toSQL( VendorSpecific.MYSQL));
	}

	@Test
	public void testHasWildcards()
	{
		Condition condition = AndCondition.andConditions(new SimpleCondition("age", null, Comparison.IS_NOT_NULL), new SimpleCondition("name", null, Comparison.IS_NOT_NULL));
		assertFalse( condition.hasWildcards());
		Condition condition1 = AndCondition.andConditions(condition, new SimpleCondition("name", "123Name1", Comparison.IS));
		assertTrue( condition1.hasWildcards());
	}

	@Test
	public void testGetValues()
	{
		Condition condition = AndCondition.andConditions(
				new SimpleCondition("age", 913, Comparison.IS),
				new SimpleCondition("name", "123Name1", Comparison.IS));
		assertArrayEquals( new Object[]{913, "123Name1"}, condition.getValues() );
	}

	@Test
	public void testTest_ActiveRecord()
	{
		Condition condition = AndCondition.andConditions(
				new SimpleCondition("age", 913, Comparison.IS),
				new SimpleCondition("name", "123Name1", Comparison.IS));
		assertFalse( condition.test( t1));
		assertTrue( condition.test( t2));
	}

	@Test
	public void testTest_Map()
	{
		Condition condition = AndCondition.andConditions(
				new SimpleCondition("age", 913, Comparison.IS),
				new SimpleCondition("name", "123Name1", Comparison.IS));
		Map<String,Object> map = new HashMap<>(2);
		map.put( "age", 913);
		map.put( "name", "123Name1");
		assertTrue( condition.test( map ) );
		assertFalse( condition.test( Collections.singletonMap( "age", 913)));
	}

	@Test
	public void testNegate()
	{
		Condition condition = AndCondition.andConditions(
				new SimpleCondition("age", 913, Comparison.IS),
				new SimpleCondition("name", "123Name1", Comparison.IS));
		condition = condition.negate();
		assertTrue( condition.test( t1));
		assertFalse( condition.test( t2));
	}
	
}
