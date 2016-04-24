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
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.functions.Absolute;
import de.doe300.activerecord.dsl.functions.LowerCase;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 * @since 0.7
 */
public class ScalarOrderTest extends Assert
{
	
	private static RecordBase<TestInterface> base;
	
	public ScalarOrderTest()
	{
	}

	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTable(TestInterface.class, ScalarOrderTest.class.getSimpleName());
		RecordCore core = TestServer.getTestCore();
		base = core.getBase( TestInterface.class).getShardBase( ScalarOrderTest.class.getSimpleName());
	}

	@AfterClass
	public static void tearDownClass() throws Exception
	{
		TestServer.destroyTestTable(TestInterface.class, ScalarOrderTest.class.getSimpleName());
	}

	@Test
	public void testCompare_Map_Map()
	{
		Order o = new ScalarOrder(new String[]{"name", "title", "age"}, new ScalarFunction[]{
			new LowerCase<TestInterface>("name", TestInterface::getName),
			null,
			new Absolute<TestInterface, Integer>("age", TestInterface::getAge)
		}, null );
		Map<String, Object> m1 = new HashMap<>(3);
		m1.put( "name", "A");
		m1.put( "title", "A");
		m1.put( "age", 23);
		Map<String, Object> m2 = new HashMap<>(3);
		m2.put( "name", "a");
		m2.put( "title", "Z");
		m2.put( "age", 23);
		Map<String, Object> m3 = new HashMap<>(3);
		m3.put( "name", "a");
		m3.put( "title", "A");
		m3.put( "age", 13);
		
		//same name, but title A before Z
		assertTrue( o.compare( m1, m2) < 0);
		//same name and title, but 13 before 23
		assertTrue( o.compare( m1, m3) > 0);
		
		assertTrue( o.compare( m1, m1) == 0);
	}
	
	@Test
	public void testReversed()
	{
		Order o = new ScalarOrder(new String[]{"name", "title", "age"}, new ScalarFunction[]{
			new LowerCase<TestInterface>("name", TestInterface::getName),
			null,
			new Absolute<TestInterface, Integer>("age", TestInterface::getAge)
		}, null );
		Map<String, Object> m1 = new HashMap<>(3);
		m1.put( "name", "A");
		m1.put( "title", "A");
		m1.put( "age", 23);
		Map<String, Object> m2 = new HashMap<>(3);
		m2.put( "name", "a");
		m2.put( "title", "Z");
		m2.put( "age", 23);
		Map<String, Object> m3 = new HashMap<>(3);
		m3.put( "name", "a");
		m3.put( "title", "A");
		m3.put( "age", 13);
		
		//same name, but title A before Z
		assertTrue( o.compare( m1, m2) < 0);
		//same name and title, but 13 before 23
		assertTrue( o.compare( m1, m3) > 0);
		
		assertTrue( o.compare( m1, m1) == 0);
		
		//reversed test
		Order reversed = o.reversed();
		
		//same name, but title Z before A
		assertTrue( reversed.compare( m1, m2) > 0);
		//same name and title, but 13 before 23
		assertTrue( reversed.compare( m1, m3) < 0);
		
		assertTrue( reversed.compare( m1, m1) == 0);
	}

	@Test
	public void testCompare_ActiveRecord_ActiveRecord()
	{
		Order o = new ScalarOrder(new String[]{"name", "age"}, new ScalarFunction[]{
			new LowerCase<TestInterface>("name", TestInterface::getName),
			new Absolute<TestInterface, Integer>("age", TestInterface::getAge)
		}, null );
		TestInterface i0 = base.createRecord();
		i0.setName( "adam");
		i0.setAge( 23);
		TestInterface i1 = base.createRecord();
		i1.setName( "Adam");
		i1.setAge( -24);
		
		assertTrue( o.compare( i0, i1) < 0);
	}

	@Test
	public void testToSQL()
	{
		Order o = new ScalarOrder(new String[]{"name", "age"}, new ScalarFunction[]{
			new LowerCase<TestInterface>("name", TestInterface::getName),
			new Absolute<TestInterface, Integer>("age", TestInterface::getAge)
		}, null );
		assertEquals( o.toSQL( JDBCDriver.DEFAULT), "LOWER(name) ASC, CAST(ABS(age) AS BIGINT) ASC");
	}
}
