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
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class SimpleOrderTest extends TestBase
{
	private final RecordBase<TestInterface> base;
	
	public SimpleOrderTest(final RecordCore core)
	{
		super(core);
		base = core.getBase( TestInterface.class).getShardBase( SimpleOrderTest.class.getSimpleName());
	}

	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, SimpleOrderTest.class.getSimpleName());
	}

	@AfterClass
	public static void tearDownClass() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, SimpleOrderTest.class.getSimpleName());
	}

	@Test
	public void testFromSQLString()
	{
		String src = "name DESC, age ASC, title ASC";
		Order o = Orders.fromSQLString( src );
		assertEquals( src, o.toSQL(JDBCDriver.DEFAULT, null) );
		assertNull(Orders.fromSQLString(null ) );
		assertNull(Orders.fromSQLString("" ) );
		assertEquals(o, Orders.fromSQLString( "ORDER BY name DESC, age ASC, title ASC"));
	}

	@Test
	public void testCompare_Map_Map()
	{
		Order o = Orders.fromSQLString( "name ASC, title DESC, age");
		Map<String, Object> m1 = new HashMap<>(3);
		m1.put( "name", "A");
		m1.put( "title", "A");
		m1.put( "age", 23);
		Map<String, Object> m2 = new HashMap<>(3);
		m2.put( "name", "A");
		m2.put( "title", "Z");
		m2.put( "age", 23);
		Map<String, Object> m3 = new HashMap<>(3);
		m3.put( "name", "A");
		m3.put( "title", "A");
		m3.put( "age", 13);
		
		//same name, but title Z before A
		assertTrue( o.compare( m1, m2) > 0);
		//same name and title, but 13 before 23
		assertTrue( o.compare( m1, m3) > 0);
		
		assertTrue( o.compare( m1, m1) == 0);
	}
	
	@Test
	public void testToSQL()
	{
		Order o = new SimpleOrder(new String[]{"name", "age", "title"}, new SimpleOrder.OrderType[]{SimpleOrder.OrderType.ASCENDING,SimpleOrder.OrderType.ASCENDING,SimpleOrder.OrderType.DESCENDING} );
		String sql = "test.name ASC, test.age ASC, test.title DESC";
		assertEquals( sql, o.toSQL(JDBCDriver.DEFAULT, "test"));
	}
	
	@Test
	public void testLevelTypes()
	{
		Order o = new SimpleOrder(new String[]{"name", "age", "id"}, new SimpleOrder.OrderType[]{SimpleOrder.OrderType.DESCENDING});
		assertEquals( "name DESC, age ASC, id ASC", o.toString());
	}

	@Test
	public void testReversed()
	{
		Order o = Orders.fromSQLString( "name ASC, title DESC, age");
		Map<String, Object> m1 = new HashMap<>(3);
		m1.put( "name", "A");
		m1.put( "title", "A");
		m1.put( "age", 23);
		Map<String, Object> m2 = new HashMap<>(3);
		m2.put( "name", "A");
		m2.put( "title", "Z");
		m2.put( "age", 23);
		Map<String, Object> m3 = new HashMap<>(3);
		m3.put( "name", "A");
		m3.put( "title", "A");
		m3.put( "age", 13);
		
		//same name, but title Z before A
		assertTrue( o.compare( m1, m2) > 0);
		//same name and title, but 13 before 23
		assertTrue( o.compare( m1, m3) > 0);
		
		assertTrue( o.compare( m1, m1) == 0);
		
		//reversed test
		Order reversed = o.reversed();
		
		//same name, but title Z before A
		assertTrue( reversed.compare( m1, m2) < 0);
		//same name and title, but 13 before 23
		assertTrue( reversed.compare( m1, m3) < 0);
		
		assertTrue( reversed.compare( m1, m1) == 0);
	}

	@Test
	public void testCompare_ActiveRecord_ActiveRecord()
	{
		Order o = Orders.fromSQLString( "name ASC, age DESC");
		TestInterface i0 = base.createRecord();
		i0.setName( "Adam");
		i0.setAge( 23);
		TestInterface i1 = base.createRecord();
		i1.setName( "Adam");
		i1.setAge( 24);
		
		assertTrue( o.compare( i0, i1) > 0);
	}
	
	@Test
	public void testEquals()
	{
		Order o = Orders.fromSQLString( "name ASC, age DESC");
		Order o1 = Orders.fromSQLString( "name ASC, age DESC");
		assertTrue( o.equals( (Object)o));
		assertFalse( o.equals( (Object)null));
		assertTrue( o.equals( (Object)o1));
	}
	
	@Test
	public void testHashCode()
	{
		Order o = Orders.fromSQLString( "name ASC, age DESC");
		Order o1 = Orders.fromSQLString( "name ASC, age DESC");
		assertEquals(o.hashCode(), o.hashCode());
		assertEquals( o.hashCode(), o1.hashCode());
	}
}