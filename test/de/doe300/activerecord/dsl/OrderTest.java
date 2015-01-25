package de.doe300.activerecord.dsl;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class OrderTest extends Assert
{
	
	public OrderTest()
	{
	}

	@Test
	public void testFromSQLString()
	{
		String src = "name DESC, age ASC, title ASC";
		Order o = Order.fromSQLString( src );
		assertEquals( src, o.toSQL() );
	}

	@Test
	public void testCompare()
	{
		Order o = Order.fromSQLString( "name ASC, title DESC, age");
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
		Order o = new Order(new String[]{"name", "age", "title"}, new Order.OrderType[]{Order.OrderType.ASCENDING,Order.OrderType.ASCENDING,Order.OrderType.DESCENDING} );
		String sql = "name ASC, age ASC, title DESC";
		assertEquals( sql, o.toSQL());
	}
}
