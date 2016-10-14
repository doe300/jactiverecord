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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
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
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.functions.Absolute;
import de.doe300.activerecord.dsl.functions.AbsoluteDouble;
import de.doe300.activerecord.dsl.functions.CastType;
import de.doe300.activerecord.dsl.functions.Ceiling;
import de.doe300.activerecord.dsl.functions.Coalesce;
import de.doe300.activerecord.dsl.functions.Concatenate;
import de.doe300.activerecord.dsl.functions.Floor;
import de.doe300.activerecord.dsl.functions.LowerCase;
import de.doe300.activerecord.dsl.functions.Round;
import de.doe300.activerecord.dsl.functions.Signum;
import de.doe300.activerecord.dsl.functions.SquareRoot;
import de.doe300.activerecord.dsl.functions.StringLength;
import de.doe300.activerecord.dsl.functions.TrimString;
import de.doe300.activerecord.dsl.functions.UpperCase;
import de.doe300.activerecord.dsl.functions.Value;
import de.doe300.activerecord.jdbc.driver.SQLiteDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class ScalarFunctionTest extends TestBase
{
	private final RecordBase<TestInterface> base;
	private final TestInterface t1, t2,t3, t4;
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables( TestInterface.class, ScalarFunctionTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables( TestInterface.class, ScalarFunctionTest.class.getSimpleName());
	}
	
	public ScalarFunctionTest(final RecordCore core)
	{
		super(core);
		
		base =core.getBase( TestInterface.class).getShardBase( ScalarFunctionTest.class.getSimpleName());
		base.findAll().parallel().forEach( ActiveRecord::destroy);
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
		t4.setName( "  SomeName  ");
		t4.setAge( -913);
	}
	
	@Test
	public void testGeneral()
	{
		ScalarFunction<TestInterface, String, String> lower = new LowerCase<TestInterface>( "name", TestInterface::getName);
		ScalarFunction<TestInterface, ?, ?> lower1 = new LowerCase<TestInterface>( "name", TestInterface::getName);
		
		assertEquals( "name", lower.getAttributeName());
		assertThrows( IllegalArgumentException.class, () -> lower.apply( Collections.singletonMap( "age", 13)));
		assertTrue( lower.equals( (Object)lower));
		assertFalse( lower.equals( new Object()));
		assertFalse( lower.equals( (Object)null));
		assertFalse( lower.equals( (ScalarFunction)null));
		assertEquals( "LOWER(name)", lower.toString());
		assertEquals( lower, (Object)lower1);
		
		assertEquals( lower.hashCode(), lower.hashCode());
		assertEquals( lower.hashCode(), lower1.hashCode());
	}

	@Test
	public void testLOWER()
	{
		ScalarFunction<TestInterface, String, String> lower = new LowerCase<TestInterface>( "name", TestInterface::getName);
		//test direct #apply
		assertEquals( "123name1", lower.apply( t1));
		assertEquals( "", lower.apply( Collections.singletonMap( "name", "")));
		
		Condition cond = Conditions.is( lower, "123name1");
		assertTrue( cond.test( t1 ));
		assertEquals( 2, base.count( cond));
		
		ScalarFunction<TestInterface, String, String> lower2 = new LowerCase<>(new TrimString<>("name", TestInterface::getName));
		assertEquals( "123name1", lower2.apply( t1));
	}

	@Test
	public void testUPPER()
	{
		ScalarFunction<TestInterface, String, String> upper = new UpperCase<>("name", TestInterface::getName);
		//test direct #apply
		assertEquals( "123NAME1", upper.apply( t1));
		assertEquals( "", upper.apply( Collections.singletonMap( "name", "")));
		
		Condition cond = Conditions.is( upper, "123NAME1");
		assertTrue( cond.test( t1 ));
		assertEquals( 2, base.count( cond));
		
		ScalarFunction<TestInterface, String, String> upper2 = new UpperCase<>(new TrimString<>("name", TestInterface::getName));
		assertEquals( "123NAME1", upper2.apply( t1));
	}

	@Test
	public void testABS()
	{
		ScalarFunction<TestInterface, Integer, Long> abs = new Absolute<>( "age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Long.valueOf( 913), abs.apply( t2));
		assertEquals( Long.valueOf( 0), abs.apply(Collections.singletonMap( "age", 0.0)));
		
		Condition cond = Conditions.isNot( abs, 913);
		assertTrue( cond.test( t1 ));
		assertEquals( 2, base.count( cond));
		
		ScalarFunction<TestInterface, Number, Long> absFunc = new Absolute<>(new StringLength<>("name", TestInterface::getName));
		assertEquals( Long.valueOf( 8), absFunc.apply( t2));
		Condition condFunc = Conditions.isNot( absFunc, 8);
		assertTrue( condFunc.test( t4 ));
		assertEquals( 1, base.count( condFunc));
	}

	@Test
	public void testABS_FLOATING()
	{
		ScalarFunction<TestInterface, Integer, Double> abs = new AbsoluteDouble<>("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Double.valueOf( 913), abs.apply( t2));
		assertEquals( Double.valueOf( 0), abs.apply( Collections.singletonMap( "age", 0)));
		
		Condition cond = Conditions.isNot( abs, 913);
		assertTrue( cond.test( t1 ));
		assertEquals( 2, base.count( cond));
		
		ScalarFunction<TestInterface, Integer, Double> abs2 = new AbsoluteDouble<>(new Signum<>("age", TestInterface::getAge));
		assertEquals( Double.valueOf( 1.0), abs2.apply( t1));
	}

	@Test
	public void testSIGN()
	{
		ScalarFunction<TestInterface, Integer, Integer> sign = new Signum<>("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Integer.valueOf( -1), sign.apply( t2));
		assertEquals( Integer.valueOf( 0), sign.apply( Collections.singletonMap( "age", -0)));
		
		Condition cond = Conditions.isNot( sign, -1);
		assertFalse(cond.test( t1 ));
		assertEquals(0, base.count( cond));
		
		ScalarFunction<TestInterface, Number, Integer> sign2 = new Signum<>(new StringLength<TestInterface>("name", TestInterface::getName));
		assertEquals( Integer.valueOf( 1), sign2.apply( t1));
	}

	@Test
	public void testFLOOR()
	{
		ScalarFunction<TestInterface, Integer, Long> floor = new Floor<>("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Long.valueOf( t2.getAge()), floor.apply( t2));
		
		Condition cond = Conditions.isNot( floor, -1);
		assertTrue(cond.test( t1 ));
		assertEquals(4, base.count( cond));
		
		ScalarFunction<TestInterface, Double, Long> floor2 = new Floor<>(new SquareRoot<>(new Absolute<>("age", TestInterface::getAge)));
		assertEquals( Long.valueOf( 30), floor2.apply( t1));
		
		ScalarFunction<TestInterface, Double, Long> floor3 = new Floor<>(new SquareRoot<>(new Signum<>("age", (i) -> null)));
		assertNull( floor3.apply( t1));
	}

	@Test
	public void testCEILING()
	{
		ScalarFunction<TestInterface, Integer, Long> ceil = new Ceiling<>("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Long.valueOf( t2.getAge()), ceil.apply( t2));
		Condition cond = Conditions.isNot( ceil, -1);
		assertTrue(cond.test( t1 ));
		assertEquals(4, base.count( cond));
		
		ScalarFunction<TestInterface, Double, Long> ceil2 = new Ceiling<>(new SquareRoot<>(new Absolute<>("age", TestInterface::getAge)));
		assertEquals( Long.valueOf( 31), ceil2.apply( t1));
		
		ScalarFunction<TestInterface, Double, Long> ceil3 = new Ceiling<>(new SquareRoot<>(new AbsoluteDouble<>("age", (i) -> null)));
		assertNull( ceil3.apply( t1));
	}

	@Test
	public void testROUND()
	{
		ScalarFunction<TestInterface, Integer, Long> round = new Round<>("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( Long.valueOf( t2.getAge()), round.apply( t2));
		Condition cond = Conditions.isNot( round, -1);
		assertTrue(cond.test( t1 ));
		assertEquals(4, base.count( cond));
		
		ScalarFunction<TestInterface, Long, Long> round2 = new Round<>(new Absolute<>("age", TestInterface::getAge));
		assertEquals( Long.valueOf( -t1.getAge()), round2.apply( t1));
		
		ScalarFunction<TestInterface, Long, Long> round3 = new Round<>(new Absolute<>("age", (TestInterface i) -> null));
		assertNull( round3.apply( t1));
	}

	@Test
	public void testSQUARE_ROOT()
	{
		ScalarFunction<TestInterface, Long, Double> sqrt = new SquareRoot<>(new Absolute<>("age", TestInterface::getAge));
		//test direct #apply
		assertEquals( Math.sqrt( Math.abs(t2.getAge())), sqrt.apply( t2), 0.00001);
		assertEquals( 0, sqrt.apply( Collections.singletonMap( "age", 0)), 0.00001);
		
		Condition cond = Conditions.isNot( sqrt, -1);
		assertTrue(cond.test( t1 ));
		if(!(base.getStore().getDriver() instanceof SQLiteDriver))
		{
			//SQLite doesn't support SQRT
			assertEquals(4, base.count( cond));
		}
	}
	
	@Test
	public void testValue()
	{
		ScalarFunction<TestInterface, Integer, Integer> value = new Value<>("age", TestInterface::getAge);
		//test direct #apply
		assertEquals( t2.getAge(), value.apply( t2).longValue());
		Condition cond = Conditions.isNot( value, -1);
		assertTrue(cond.test( t1 ));
		assertEquals(4, base.count( cond));
	}
	
	@Test
	public void testStringLength()
	{
		ScalarFunction<TestInterface, String, Number> length = new StringLength<>("name", TestInterface::getName);
		//test direct #apply
		assertEquals( "123Name4".length(), length.apply( t3));
		assertEquals( 0, length.apply( Collections.singletonMap( "name", "")));
		
		Condition cond = Conditions.is( length, 8);
		assertTrue( cond.test( t3));
		assertEquals( 3, base.count( cond));
		
		ScalarFunction<TestInterface, String, Number> length2 = new StringLength<>(new TrimString<>("name", TestInterface::getName));
		TestInterface ti = base.createRecord();
		ti.setName( " Steve is cool!   ");
		assertEquals( 14, length2.apply( ti));
		ti.destroy();
	}
	
	@Test
	public void testTRIM()
	{
		ScalarFunction<TestInterface, String, String> trim = new TrimString<>("name", TestInterface::getName);
		//test direct #apply
		assertEquals( "SomeName", trim.apply( t4));
		assertEquals( "", trim.apply( Collections.singletonMap( "name", "   ")));
		
		Condition cond = Conditions.is( trim, "SomeName");
		assertTrue( cond.test( t4));
		assertEquals( 1, base.count( cond));
		
		ScalarFunction<TestInterface, String, String> trim2 = new TrimString<>(new UpperCase<>("name", TestInterface::getName));
		assertEquals( trim.apply( t4).toUpperCase(), trim2.apply( t4));
	}
	
	@Test
	public void testCAST()
	{
		//TODO for MySQL, can't cast to VARCHAR(XXX), needs to use CHAR. But where to set??
		ScalarFunction<TestInterface, Integer, String> cast = new CastType<TestInterface, Integer, String>("age", TestInterface::getAge, String.class, (Integer i) -> Integer.toString( i ));
		//test direct #apply
		assertEquals( "-913", cast.apply( t4));
		Condition cond = Conditions.is( cast, "-913");
		assertTrue( cond.test( t4));
		assertEquals( 2, base.count( cond));
		
		ScalarFunction<TestInterface, Long, String> cast2 = new CastType<TestInterface, Long, String>(new Absolute<>("age", TestInterface::getAge), String.class, (Long l) -> Long.toString( l ));
		assertEquals("913", cast2.apply( t4));
	}
	
	@Test
	public void testCONCAT()
	{
		final ScalarFunction<TestInterface, String, String> concat = new Concatenate<TestInterface>(
				new Value<TestInterface, String>("name", TestInterface::getName),
				new CastType<TestInterface, Integer, String>("age", TestInterface::getAge, String.class, (i) -> Integer.toString( i))
		);
		
		assertEquals( "123Name1-912", concat.apply( t1));
		Condition cond = Conditions.isLike( concat, "123Name%");
		assertTrue( cond.test( t1));
		assertTrue( cond.test( t2));
		assertTrue( cond.test( t3));
		assertEquals( 3, base.count( cond ) );
		
		final TestInterface t = base.createRecord( Collections.singletonMap( "age", 23));
		assertNull( concat.apply( t));
		t.destroy();
	}
	
	@Test
	public void testCOALESCE()
	{
		final ScalarFunction<TestInterface, String, String> coalesce = new Coalesce<TestInterface, String>(
				new Value<TestInterface, String>("name", TestInterface::getName),
				new CastType<TestInterface, Integer, String>("age", TestInterface::getAge, String.class, (i) -> Integer.toString( i))
		);
		
		final TestInterface t = base.createRecord( Collections.singletonMap( "age", 23));
		
		assertEquals( Integer.toString( 23), coalesce.apply( t));
		Condition cond = Conditions.is( coalesce, Integer.toString( 23));
		assertTrue( cond.test( t ));
		assertEquals( 1, base.count( cond));
		
		t.destroy();
	}
}
