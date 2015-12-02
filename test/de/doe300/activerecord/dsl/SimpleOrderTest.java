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

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class SimpleOrderTest extends Assert
{
	
	public SimpleOrderTest()
	{
	}

	@BeforeClass
	public static void setUpClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownClass() throws Exception
	{
	}

	@Test
	public void testFromSQLString()
	{
		String src = "name DESC, age ASC, title ASC";
		Order o = SimpleOrder.fromSQLString( src );
		assertEquals( src, o.toSQL(new JDBCDriver()) );
	}

	@Test
	public void testCompare()
	{
		Order o = SimpleOrder.fromSQLString( "name ASC, title DESC, age");
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
		String sql = "name ASC, age ASC, title DESC";
		assertEquals( sql, o.toSQL(new JDBCDriver()));
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
		Order o = SimpleOrder.fromSQLString( "name ASC, title DESC, age");
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
}