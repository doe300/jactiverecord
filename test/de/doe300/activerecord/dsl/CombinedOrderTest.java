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

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author doe300
 * @since 0.7
 */
public class CombinedOrderTest extends Assert
{
	public CombinedOrderTest()
	{
	}
	
	@Test
	public void testCombine()
	{
		Order o = Orders.fromSQLString( "name ASC, age DESC");
		
		assertEquals(o, Orders.combine( o, o, o, o));
		assertEquals(o, Orders.combine( o));
		assertEquals(o, Orders.combine( o, null));
		assertNotSame(o, Orders.combine( o, Orders.sortAscending( "age")));
	}

	@Test
	public void testToSQL()
	{
		Order o = Orders.combine(Orders.fromSQLString( "name ASC"), Orders.fromSQLString( "age DESC") );
		assertEquals( o.toSQL( JDBCDriver.DEFAULT, null), "name ASC, age DESC");
	}

	@Test
	public void testReversed()
	{
		Order o = Orders.combine(Orders.fromSQLString( "name ASC"), Orders.fromSQLString( "age DESC") );
		assertEquals(o.reversed(), Orders.fromSQLString( "name DESC, age asc"));
	}

	@Test
	public void testCompare_Map_Map()
	{
		Order o = Orders.combine(Orders.fromSQLString( "name ASC"), Orders.fromSQLString( "age DESC") );
		final Map<String, Object> obj1 = new HashMap<>(2);
		obj1.put( "name", "Adam");
		obj1.put( "age", 112);
		final Map<String, Object> obj2 = new HashMap<>(2);
		obj2.put( "name", "Adam");
		obj2.put( "age", 113);
		assertTrue( o.compare( obj1, obj2) > 0);
	}

	@Test
	public void testCompare_ActiveRecord_ActiveRecord()
	{
		//TODO
	}

	@Test
	public void testEquals()
	{
		Order o1 = Orders.combine(Orders.fromSQLString( "name ASC"), Orders.fromSQLString( "age DESC") );
		Order o2 = Orders.combine(Orders.fromSQLString( "name ASC, age DESC"));
		
		assertTrue( o1.equals( (Object)o1));
		assertFalse( o1.equals( (Object)null));
		assertFalse( o1.equals( (Order)null));
		assertTrue( o1.equals( (Object)o2));
	}
	
	@Test
	public void testHashCode()
	{
		Order o1 = Orders.combine(Orders.fromSQLString( "name ASC"), Orders.fromSQLString( "age DESC") );
		Order o2 = Orders.combine(Orders.fromSQLString( "name ASC, age DESC"));
		
		assertEquals( o1.hashCode(), o1.hashCode());
		assertEquals( o1.hashCode(), o2.hashCode());
	}
}
