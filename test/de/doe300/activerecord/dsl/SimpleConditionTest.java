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
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.dsl.functions.Absolute;
import de.doe300.activerecord.dsl.functions.LowerCase;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * "Predicate"-Test and "SQL"-Test are the same.
 * But Predicate is already tested in {@link ComparisonTest}
 * @author doe300
 */
public class SimpleConditionTest extends TestBase implements AssertException
{
	private final RecordBase<TestInterface> base;
	private final TestInterface t1, t2,t3, t4;
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables( TestInterface.class, SimpleConditionTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, SimpleConditionTest.class.getSimpleName());
	}
	
	public SimpleConditionTest(final RecordCore core)
	{
		super(core);
		
		base = core.getBase( TestInterface.class).getShardBase( SimpleConditionTest.class.getSimpleName() );
		base.findAll().parallel().forEach( ActiveRecord::destroy);
		t1 = base.createRecord();
		t1.setName( "123Name1");
		t1.setAge( -912);
		t2 = base.createRecord();
		t2.setName( "123Name1");
		t2.setAge( -913);
		t3 = base.createRecord();
		t3.setName( "123Name4");
		t3.setAge( -913);
		t4 = base.createRecord();
	}
	
	protected Scope toScope(Condition cond)
	{
		return new Scope(cond, null, Scope.NO_LIMIT);
	}
	
	@Test
	public void testAndCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("age", -912, Comparison.IS ).and(new SimpleCondition("name", "123Name1", Comparison.IS))) );
		//test SQL
		assertEquals((Integer)t1.getPrimaryKey()	, base.getStore().findFirst( base,  toScope( new SimpleCondition("age", -912, Comparison.IS ).and(new SimpleCondition("name", "123Name1", Comparison.IS)))));
	}
	
	@Test
	public void testOrCondition()
	{
		//test Predicate
		assertSame( t3, base.findFirst( new SimpleCondition("age", -910, Comparison.IS).or(new SimpleCondition("name",
				"123Name4", Comparison.IS))));
		//test SQL
		assertEquals( (Integer)t3.getPrimaryKey(), base.getStore().findFirst( base, toScope(new SimpleCondition("age", -910, Comparison.IS).or(new SimpleCondition("name",
				"123Name4", Comparison.IS)))));
	}
	
	@Test
	public void testIsCondition()
	{
		//test Predicate
		assertSame( t3, base.findFirst( new SimpleCondition("name", "123Name4", Comparison.IS)));
		//test SQL
		assertEquals( (Integer)t3.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("name", "123Name4", Comparison.IS))));
		//test "a = null" optimization
		assertSame( Comparison.IS_NULL, new SimpleCondition("id", null, Comparison.IS).getComparison());
		//test SQLFunction
		assertTrue( Conditions.is( new Absolute<>("age", TestInterface::getAge), 912).test( t1));
		assertSame( t1, base.findFirst(Conditions.is( new Absolute<>("age", TestInterface::getAge), 912)));
	}
	
	@Test
	public void testIsNotCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("other", null, Comparison.IS_NULL)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("other", null,Comparison.IS_NULL))));
		//test "a != null" optimization
		assertSame( Comparison.IS_NOT_NULL, new SimpleCondition("id", null, Comparison.IS_NOT).getComparison());
		//test SQLFunction
		assertTrue( Conditions.isNot( new Absolute<>("age", TestInterface::getAge), 912).test( t2));
		assertSame(t2, base.findFirst( Conditions.isNot( new Absolute<>("age", TestInterface::getAge), 912)));
	}
	
	@Test
	public void testLikeCondition()
	{
		//test Predicate
		assertSame( t3, base.findFirst( new SimpleCondition("name", "1%3Name4", Comparison.LIKE)));
		//test SQL
		assertEquals( (Integer)t3.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("name", "1%3Name4", Comparison.LIKE))));
	}
	
	@Test
	public void testIsNullCondition()
	{
		//test Predicate
		assertSame( t4, base.findFirst( new SimpleCondition("name", null, Comparison.IS_NULL)));
		//test SQL
		assertEquals( (Integer)t4.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("name", null, Comparison.IS_NULL))));
	}
	
	@Test
	public void testIsNotNullCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("name", null, Comparison.IS_NOT_NULL)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("name", null,Comparison.IS_NOT_NULL))));
	}
	
	@Test
	public void testLargerCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("age", -913, Comparison.LARGER)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("age", -913, Comparison.LARGER))));
	}
	
	@Test
	public void testLargerEqualsCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("age", -913, Comparison.LARGER_EQUALS)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("age", -913, Comparison.LARGER_EQUALS))));
	}
	
	@Test
	public void testSmallerCondition()
	{
		//test Predicate
		assertSame( t2, base.findFirst( new SimpleCondition("age", -912, Comparison.SMALLER)));
		//test SQL
		assertEquals( (Integer)t2.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("age", -912, Comparison.SMALLER))));
	}
	
	@Test
	public void testSmallerEqualsCondition()
	{
		//test Predicate
		assertSame( t2, base.findFirst( new SimpleCondition("age", -913, Comparison.SMALLER_EQUALS)));
		//test SQL
		assertEquals( (Integer)t2.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("age", -913, Comparison.SMALLER_EQUALS))));
	}
	
	@Test
	public void testInCondition()
	{
		//test Predicate
		assertSame( t1, base.findFirst( new SimpleCondition("age", new Integer[]{-912,-913}, Comparison.IN)));
		assertSame( t1, base.findFirst( new SimpleCondition("age", Arrays.asList( -912,-913), Comparison.IN)));
		//test SQL
		assertEquals( (Integer)t1.getPrimaryKey(), base.getStore().findFirst( base, toScope( new SimpleCondition("age", new Integer[]{-912,-913}, Comparison.IN))));
	}
	
	@Test
	public void testLargeInCondition()
	{
		//only fails for SQLite
		final List<Long> ids = Stream.iterate( 0L, (Long i) -> i+1).limit(10000).collect( Collectors.toList());
		final Condition cond = new SimpleCondition("id", ids, Comparison.IN);
		
		assertEquals(base.getAll().size(), base.getForCondition( cond, null).size());
	}
	
	@Test
	public void testSQLFunction()
	{
		//key is SQL function
		final Condition cond = Conditions.is( new LowerCase<>("name", TestInterface::getName), "adam");
		TestInterface t = base.createRecord();
		t.setName( "Adam");
		assertTrue( cond.hasWildcards());
		assertNotNull( cond.getValues());
		assertTrue( cond.test( t));
		assertEquals( base.findFirst( cond ), t );
		
		//value is SQL function
		final Condition cond1 = Conditions.is( "name", new LowerCase<>("name", TestInterface::getName));
		assertFalse( cond1.hasWildcards());
		assertFalse( cond1.test( t));
		TestInterface t1 = base.createRecord();
		t1.setName( "adam");
		assertTrue( cond1.test( t1));
		
		//both are SQL function
		final Condition cond2 = Conditions.is( new LowerCase<>("name", TestInterface::getName), new LowerCase<>("name", TestInterface::getName));
		assertFalse( cond2.hasWildcards());
		assertTrue( cond2.test( t));
		assertTrue( cond2.test( t1));
		assertTrue( cond2.test( Collections.singletonMap( "name", "Eve")));
		assertThrows(IllegalArgumentException.class, () -> cond2.test( Collections.emptyMap()));
		
		
		t.destroy();
		t1.destroy();
	}
	
	@Test
	public void testCheckValue()
	{
		assertThrows( IllegalArgumentException.class, () -> new SimpleCondition("id", "Dummy", Comparison.IN));
		//fails for trying to compare non-comparable value
		assertThrows( IllegalArgumentException.class, () -> new SimpleCondition("age", new Object(), Comparison.SMALLER));
	}
	
	@Test
	public void testNegate()
	{
		SimpleCondition s1 = new SimpleCondition("id", "dummy", Comparison.IS);
		assertSame( s1, s1.negate().negate() );
	}
	
	@Test
	public void testEquals()
	{
		SimpleCondition s1 = new SimpleCondition("id", "dummy", Comparison.IS);
		SimpleCondition s2 = new SimpleCondition("id", "dummy", Comparison.IS);
		
		assertTrue( s1.equals((Object) s1));
		assertFalse( s1.equals((Object) null));
		assertFalse( s1.equals((Condition) null));
		assertTrue( s1.equals( (Object)s2));
	}
	
	@Test
	public void testHashCode()
	{
		SimpleCondition s1 = new SimpleCondition("id", "dummy", Comparison.IS);
		SimpleCondition s2 = new SimpleCondition("id", "dummy", Comparison.IS);
		
		assertEquals( s1.hashCode(), s2.hashCode());
		assertEquals( s1.hashCode(), s2.hashCode());
	}
}
