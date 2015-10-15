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
import de.doe300.activerecord.scope.Scope;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */


public class AggregateFunctionTest extends Assert
{
	private static RecordBase<TestInterface> base;
	private static TestInterface t1, t2,t3, t4;
	
	public AggregateFunctionTest()
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
		t3.setAge( 914);
		//record with not-unique age
		t4 = base.createRecord();
		t4.setName( "SomeName");
		t4.setAge( 913);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testMINIMUM()
	{
		AggregateFunction<TestInterface, Integer, Integer> min = AggregateFunction.MINIMUM( "age", TestInterface::getAge);
		Integer minAge = Stream.of( t1, t2, t3, t4).collect( min);
		
		assertEquals( Integer.valueOf( t1.getAge()), minAge);
		assertEquals( Integer.valueOf( t1.getAge()), base.aggregate( min, null));
		assertEquals( Integer.valueOf( t1.getAge()), base.getStore().aggregate( base, min, null));
		assertEquals( Integer.valueOf( t1.getAge()), min.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, new Scope(null, null, Scope.NO_LIMIT)).
						values().stream()));
	}

	@Test
	public void testMAXIMUM()
	{
		AggregateFunction<TestInterface, Integer, Integer> max = AggregateFunction.MAXIMUM("age", TestInterface::getAge);
		Integer maxAge = Stream.of( t1, t2, t3, t4).collect( max);
		
		assertEquals( Integer.valueOf( t3.getAge()), maxAge);
		assertEquals( Integer.valueOf( t3.getAge()), base.aggregate( max, null));
		assertEquals( Integer.valueOf( t3.getAge()), base.getStore().aggregate( base, max, null));
		assertEquals( Integer.valueOf( t3.getAge()), max.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, new Scope(null, null, Scope.NO_LIMIT)).
						values().stream()));
	}

	@Test
	public void testCOUNT()
	{
		AggregateFunction<TestInterface, Integer, Long> count = AggregateFunction.COUNT("age", TestInterface::getAge);
		Long number = Stream.of( t1, t2, t3, t4).collect( count);
		
		assertEquals( Long.valueOf( 4), number);
		assertEquals( Long.valueOf( 4), base.aggregate( count, null));
		assertEquals( Long.valueOf( 4), base.getStore().aggregate( base, count, null));
		assertEquals( Long.valueOf( 4), count.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, new Scope(null, null, Scope.NO_LIMIT)).
						values().stream()));
	}

	@Test
	public void testCOUNT_DISTINCT()
	{
		AggregateFunction<TestInterface, Integer, Long> countDistinct = AggregateFunction.COUNT_DISTINCT("age", TestInterface::getAge);
		Long distinctCount = Stream.of( t1, t2, t3, t4).collect( countDistinct);
		
		assertEquals( Long.valueOf( 3), distinctCount);
		assertEquals( Long.valueOf( 3), base.aggregate( countDistinct, null));
		assertEquals( Long.valueOf( 3), base.getStore().aggregate( base, countDistinct, null));
		assertEquals( Long.valueOf( 3), countDistinct.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, new Scope(null, null, Scope.NO_LIMIT)).
						values().stream()));
	}

	@Test
	public void testSUM()
	{
		AggregateFunction<TestInterface, Integer, Long> sum = AggregateFunction.SUM("age", TestInterface::getAge);
		Long sumAge = Stream.of( t1, t2, t3, t4).collect( sum);
		long otherSum = t1.getAge() + t2.getAge() + t3.getAge() + t4.getAge();
		
		assertEquals( Long.valueOf( otherSum), sumAge);
		assertEquals( Long.valueOf( otherSum), base.aggregate( sum, null));
		assertEquals( Long.valueOf( otherSum), base.getStore().aggregate( base, sum, null));
		assertEquals( Long.valueOf( otherSum), sum.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, new Scope(null, null, Scope.NO_LIMIT)).
						values().stream()));
	}

	@Test
	public void testSUM_FLOATING()
	{
		AggregateFunction<TestInterface, Integer, Double> sumFloat = AggregateFunction.SUM_FLOATING("age", TestInterface::getAge);
		Double sumAge = Stream.of( t1, t2, t3, t4).collect( sumFloat);
		double otherSum = t1.getAge() + t2.getAge() + t3.getAge() + t4.getAge();
		
		assertEquals( Double.valueOf( otherSum), sumAge);
		assertEquals( Double.valueOf( otherSum), base.aggregate( sumFloat, null));
		assertEquals( Double.valueOf( otherSum), base.getStore().aggregate( base, sumFloat, null));
		assertEquals( Double.valueOf( otherSum), sumFloat.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, new Scope(null, null, Scope.NO_LIMIT)).
						values().stream()));
	}

	@Test
	public void testAVERAGE()
	{
		AggregateFunction<TestInterface, Integer, Double> average = AggregateFunction.AVERAGE("age", TestInterface::getAge);
		Double averageAge = Stream.of( t1, t2, t3, t4).collect( average);
		double avgAge = (t1.getAge() + t2.getAge() + t3.getAge() + t4.getAge()) / 4.0;
		
		assertEquals( Double.valueOf( avgAge), averageAge);
		assertEquals( Double.valueOf( avgAge), base.aggregate( average, null));
		assertEquals( Double.valueOf( avgAge), base.getStore().aggregate( base, average, null));
		assertEquals( Double.valueOf( avgAge), average.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, new Scope(null, null, Scope.NO_LIMIT)).
						values().stream()));
	}
}
