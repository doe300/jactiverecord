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
import de.doe300.activerecord.TestPOJO;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.functions.Average;
import de.doe300.activerecord.dsl.functions.CountDistinct;
import de.doe300.activerecord.dsl.functions.CountNotNull;
import de.doe300.activerecord.dsl.functions.Maximum;
import de.doe300.activerecord.dsl.functions.Minimum;
import de.doe300.activerecord.dsl.functions.Signum;
import de.doe300.activerecord.dsl.functions.Sum;
import de.doe300.activerecord.dsl.functions.SumDouble;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class AggregateFunctionTest extends TestBase
{
	private RecordBase<TestInterface> base;
	private TestInterface t1, t2,t3, t4;
	
	public AggregateFunctionTest(final RecordCore core)
	{
		super(core);
		base = core.getBase( TestInterface.class).getShardBase( AggregateFunctionTest.class.getSimpleName());
		base.findAll().forEach( ActiveRecord::destroy);
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
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, AggregateFunctionTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, AggregateFunctionTest.class.getSimpleName());
	}
	
	@Test
	public void testGeneral()
	{
		AggregateFunction<TestInterface, Integer, ?, Integer> ag1 = new Minimum<>( "age", TestInterface::getAge);
		
		assertEquals( "age", ag1.getAttributeName());
		assertEquals( "MIN(age)", ag1.toString());
	}
	
	@Test
	public void testEquals()
	{
		AggregateFunction<TestInterface, Integer, ?, Integer> ag1 = new Minimum<>( "age", TestInterface::getAge);
		AggregateFunction<TestPOJO, Integer, ?, Integer> ag2 = new Minimum<>( "age", TestPOJO::getAge);
		
		assertEquals( ag1, ag2);
		assertNotEquals( ag1, new Object());
	}

	@Test
	public void testMINIMUM()
	{
		AggregateFunction<TestInterface, Integer, ?, Integer> min = new Minimum<>( "age", TestInterface::getAge);
		Integer minAge = Stream.of( t1, t2, t3, t4).collect( min);
		
		assertEquals( Integer.valueOf( t1.getAge()), minAge);
		assertEquals( Integer.valueOf( t1.getAge()), base.aggregate( min, null));
		assertEquals( Integer.valueOf( t1.getAge()), base.getStore().aggregate( base, min, null));
		assertEquals( Integer.valueOf( t1.getAge()), min.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, Scope.DEFAULT).
						values().stream()));
		assertNull( min.aggregate( Stream.empty()));
		
		AggregateFunction<TestInterface, ? extends Number, ?, ? extends Number> min2 = new Minimum<>(new Signum<>("age", TestInterface::getAge));
		assertEquals( 1L, base.aggregate( min2, null).longValue());
	}

	@Test
	public void testMAXIMUM()
	{
		AggregateFunction<TestInterface, Integer, ?, Integer> max = new Maximum<>("age", TestInterface::getAge);
		Integer maxAge = Stream.of( t1, t2, t3, t4).collect( max);
		
		assertEquals( Integer.valueOf( t3.getAge()), maxAge);
		assertEquals( Integer.valueOf( t3.getAge()), base.aggregate( max, null));
		assertEquals( Integer.valueOf( t3.getAge()), base.getStore().aggregate( base, max, null));
		assertEquals( Integer.valueOf( t3.getAge()), max.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, Scope.DEFAULT).
						values().stream()));
		assertNull( max.aggregate( Stream.empty()));
		
		AggregateFunction<TestInterface, ? extends Number, ?, ? extends Number> max2 = new Maximum<>(new Signum<>("age", TestInterface::getAge));
		assertEquals( 1L, base.aggregate( max2, null).longValue());
	}

	@Test
	public void testCOUNT()
	{
		AggregateFunction<TestInterface, Integer, ?, Number> count = new CountNotNull<>("age", TestInterface::getAge);
		Number number = Stream.of( t1, t2, t3, t4).collect( count);
		
		assertEquals( 4L, number.longValue());
		assertEquals( 4L, base.aggregate( count, null).longValue());
		assertEquals( 4L, base.getStore().aggregate( base, count, null).longValue());
		assertEquals( 4L, count.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, Scope.DEFAULT).
						values().stream()).longValue());
		assertEquals(0L, count.aggregate( Stream.empty()));
	}

	@Test
	public void testCOUNT_DISTINCT()
	{
		AggregateFunction<TestInterface, Integer, ?, Number> countDistinct = new CountDistinct<>("age", TestInterface::getAge);
		Number distinctCount = Stream.of( t1, t2, t3, t4).collect( countDistinct);
		
		assertEquals( 3L, distinctCount.longValue());
		assertEquals( 3L, base.aggregate( countDistinct, null).longValue());
		assertEquals( 3L, base.getStore().aggregate( base, countDistinct, null).longValue());
		assertEquals( 3L, countDistinct.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, Scope.DEFAULT).
						values().stream()).longValue());
		assertEquals(0L, countDistinct.aggregate( Stream.empty()));
		
		AggregateFunction<TestInterface, ?, ?, Number> countDistinct2 = new CountDistinct<>(new Signum<>("age", TestInterface::getAge));
		assertEquals( 1L, base.aggregate( countDistinct2, null).longValue());
	}

	@Test
	public void testSUM()
	{
		AggregateFunction<TestInterface, Integer, ?, Number> sum = new Sum<>("age", TestInterface::getAge);
		Number sumAge = Stream.of( t1, t2, t3, t4).collect( sum);
		long otherSum = t1.getAge() + t2.getAge() + t3.getAge() + t4.getAge();
		
		assertEquals( otherSum, sumAge.longValue());
		assertEquals( otherSum, base.aggregate( sum, null).longValue());
		assertEquals( otherSum, base.getStore().aggregate( base, sum, null).longValue());
		assertEquals( otherSum, sum.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, Scope.DEFAULT).
						values().stream()).longValue());
		assertEquals(0L, sum.aggregate( Stream.empty()));
	}

	@Test
	public void testSUM_FLOATING()
	{
		AggregateFunction<TestInterface, Integer, ?, Number> sumFloat = new SumDouble<>("age", TestInterface::getAge);
		Number sumAge = Stream.of( t1, t2, t3, t4).collect( sumFloat);
		double otherSum = t1.getAge() + t2.getAge() + t3.getAge() + t4.getAge();
		
		assertEquals( otherSum, sumAge.doubleValue(), 0.0);
		assertEquals( otherSum, base.aggregate( sumFloat, null).doubleValue(), 0.0);
		assertEquals( otherSum, base.getStore().aggregate( base, sumFloat, null).doubleValue(), 0.0);
		assertEquals( otherSum, sumFloat.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, Scope.DEFAULT).
						values().stream()).doubleValue(), 0.0);
		assertEquals(0.0, sumFloat.aggregate( Stream.empty()));
		
		AggregateFunction<TestInterface, Integer, ?, Number> sumFloat2 = new SumDouble<>(new Signum<>("age", TestInterface::getAge));
		assertEquals( 4.0, base.aggregate( sumFloat2, null).doubleValue(), 0.0001 );
	}

	@Test
	public void testAVERAGE()
	{
		AggregateFunction<TestInterface, Integer, ?, Number> average = new Average<>("age", TestInterface::getAge);
		Number averageAge = Stream.of( t1, t2, t3, t4).collect( average);
		double avgAge = (t1.getAge() + t2.getAge() + t3.getAge() + t4.getAge()) / 4.0;
		
		assertEquals( avgAge, average.apply( Collections.singletonMap( "age", averageAge)));
		assertEquals( t1.getAge(), average.apply( t1).intValue());
		assertEquals( avgAge, averageAge.doubleValue(), 0.0);
		assertEquals( avgAge, base.aggregate( average, null).doubleValue(), 0.0);
		assertEquals( avgAge, base.getStore().aggregate( base, average, null).doubleValue(), 0.0);
		assertEquals( avgAge, average.aggregate( 
				base.getStore().findAllWithData( base, new String[]{"age"}, Scope.DEFAULT).
						values().stream()).doubleValue(), 0.0);
		assertNull( average.aggregate( Stream.empty()));
		
		AggregateFunction<TestInterface, Integer, ?, Number> average2 = new Average<>(new Signum<>("age", TestInterface::getAge));
		assertEquals( 1, base.aggregate( average2, null).intValue());
	}
}
