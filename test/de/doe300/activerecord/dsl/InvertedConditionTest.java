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
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class InvertedConditionTest extends TestBase implements AssertException
{
	private final RecordBase<TestInterface> base;
	private final TestInterface t1, t2,t3;
	
	public InvertedConditionTest(final RecordCore core)
	{
		super(core);
		
		base = core.getBase( TestInterface.class).getShardBase( InvertedConditionTest.class.getSimpleName());
		base.findAll().parallel().forEach( ActiveRecord::destroy);
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
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, InvertedConditionTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, InvertedConditionTest.class.getSimpleName());
	}

	@Test
	public void testSimpleCondition()
	{
		//matches t2 and t3
		Condition cond = Conditions.is("age", 913);
		//matches t1
		assertSame( t1, base.findFirst(Conditions.invert(cond )));
	}

	@Test
	public void testAndCondition()
	{
		//matches t3
		Condition cond = Conditions.and(Conditions.is("age", 913), Conditions.is("name", "123Name4") );
		//matches t1 and t2
		assertTrue( base.find(Conditions.invert(cond)).count() >= 2 );
		try(final Stream<TestInterface> s = base.find(Conditions.invert(cond)))
		{
			assertFalse( s.anyMatch( (TestInterface i) -> i.equals( t3)));
		}
	}
	
	@Test
	public void testInvertCondition()
	{
		//matches t1
		Condition cond = Conditions.invert(Conditions.is("age", 913));
		//matches t2 and t3
		assertTrue( base.find(Conditions.invert(cond)).count() >= 2 );
		try(final Stream<TestInterface> s = base.find(Conditions.invert(cond)))
		{
			assertFalse( s.anyMatch( (TestInterface i) -> i.equals( t1)));
		}
		
		assertThrows( IllegalArgumentException.class, () -> Conditions.invert( null));
	}

	@Test
	public void testTest_ActiveRecord()
	{
		Condition cond = Conditions.invert(Conditions.is("age", 913));
		//t1 has age of non-913
		assertTrue( cond.test( t1));
		assertFalse( cond.test( t2));
	}

	@Test
	public void testTest_Map()
	{
		Condition cond = Conditions.invert(Conditions.is("age", 913));
		assertTrue( cond.test( Collections.singletonMap( "age", 912)));
		assertFalse( cond.test( Collections.singletonMap( "age", 913)));
	}

	@Test
	public void testNegate()
	{
		Condition cond = Conditions.is("age", 913);
		Condition invCond = Conditions.invert(cond);
		assertSame( cond, invCond.negate());
	}
	
	@Test
	public void testEquals()
	{
		Condition cond = Conditions.is("age", 913);
		Condition invCond = Conditions.invert(cond);
		Condition invCond2 = Conditions.invert(cond);
		
		assertTrue( invCond.equals( (Object)invCond));
		assertFalse( invCond.equals( (Object)null));
		assertFalse( invCond.equals( (Condition)null));
		assertTrue( invCond.equals( (Object)invCond2));
	}
	
	@Test
	public void testHashCode()
	{
		Condition cond = Conditions.is("age", 913);
		Condition invCond = Conditions.invert(cond);
		Condition invCond2 = Conditions.invert(cond);
		
		assertEquals( invCond.hashCode()	, invCond.hashCode() );
		assertEquals( invCond.hashCode(), invCond2.hashCode());
	}
}
