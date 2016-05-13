/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.doe300.activerecord.dsl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 * @since 0.8
 */
public class XorConditionTest extends TestBase
{
	private final RecordBase<TestInterface> base;
	private final TestInterface t1, t2,t3;
	
	public XorConditionTest(final RecordCore core)
	{
		super(core);
		
		base = core.getBase( TestInterface.class).getShardBase( XorConditionTest.class.getSimpleName() );
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
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, XorConditionTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, XorConditionTest.class.getSimpleName());
	}

	@Test
	public void testHasWildcards()
	{
		final Condition cond1 = Conditions.xor( Conditions.is( "name", "Adam"), Conditions.is( "age", 12));
		final Condition cond2 = Conditions.xor( Conditions.isNull( "name"), Conditions.isTrue());
		
		assertTrue( cond1.hasWildcards());
		assertFalse( cond2.hasWildcards());
	}

	@Test
	public void testGetValues()
	{
		final Condition cond1 = Conditions.xor( Conditions.is( "name", "Adam"), Conditions.is( "age", 12));
		assertArrayEquals( new Object[]{"Adam", 12}, cond1.getValues());
	}

	@Test
	public void testTest_ActiveRecord()
	{
		final Condition cond = Conditions.xor( Conditions.is( "name", "123Name1"), Conditions.is( "age", 913));
		
		//one match
		assertTrue( cond.test( t1));
		//both match
		assertFalse( cond.test( t2));
		//none match
		assertFalse( cond.test( t3));
	}

	@Test
	public void testTest_Map()
	{
		final Condition cond = Conditions.xor( Conditions.is( "name", "123Name1"), Conditions.is( "age", 913));
		
		final Map<String, Object> map = new HashMap<>(2);
		map.put( "name", "123Name1");
		map.put( "age", 12);
		
		assertTrue( cond.test( map));
		
		map.put( "age", 913);
		
		assertFalse( cond.test( map));
		
		map.put( "name", "Adam");
		map.put( "age", 12);
		
		assertFalse( cond.test( map));
	}

	@Test
	public void testToSQL()
	{
		final Condition cond1 = Conditions.xor( Conditions.is( "name", "Adam"), Conditions.is( "age", 12));
		
		assertEquals( "((name = ? AND NOT age = ?) OR (NOT name = ? AND age = ?))", cond1.toSQL( JDBCDriver.DEFAULT, null));
	}

	@Test
	public void testEquals()
	{
		final Condition cond1 = Conditions.xor( Conditions.is( "name", "Adam"), Conditions.is( "age", 12)).xor( Conditions.isLarger( "age", 15));
		final Condition cond2 = Conditions.xor( Conditions.is( "name", "Adam"), Conditions.is( "age", 12), Conditions.isLarger( "age", 15));
		final Condition cond3 = Conditions.xor( Conditions.is( "name", "Adam"), Conditions.is( "age", 12));
		
		assertTrue( cond1.equals( cond2));
		assertFalse( cond1.equals( cond3));
		
		assertFalse( cond1.equals( new Object()));
	}
}
