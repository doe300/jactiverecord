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
package de.doe300.activerecord.store.impl.memory;

import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.dsl.SimpleOrder;
import de.doe300.activerecord.scope.Scope;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class MemoryTableTest extends Assert
{
	private static final MemoryTable table = new MemoryTable("id", new MemoryColumn[]{
		new MemoryColumn("name", String.class),
		new MemoryColumn("age", Integer.class)
	});
	
	public MemoryTableTest()
	{
	}

	@Test
	public void testGetColumnNames()
	{
		assertTrue( table.getColumnNames().contains( "name"));
		assertFalse( table.getColumnNames().contains( "noSuchColumn"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutValue()
	{
		int row = table.insertRow();
		table.putValue( row, "name", "Adam");
		assertNotNull( table.getValue( row, "name"));
		
		//failure-tests
		assertFalse( table.putValue( 120431230, "name", "Adam"));
		table.putValue( row, "noSuchColumn", "Steve");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutValues_3args()
	{
		int row = table.insertRow();
		table.putValues( row, new String[]{"name", "age"}, new Object[]{"Eve", 42} );
		assertNotNull( table.getValue( row, "name"));
		
		//failure-test
		table.putValues( row, new String[]{"name", "noSuchColumn"}, new Object[]{"Eve", "42"} );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutValues_int_Map()
	{
		int row = table.insertRow();
		table.putValues( row, Collections.singletonMap( "name", "Adam"));
		assertEquals( "Adam", table.getValue( row, "name"));
		
		//failure tests
		assertFalse( table.putValues( 10112203, Collections.singletonMap( "name", "Adam")));
		table.putValues( row, Collections.singletonMap( "age", "Eve"));
	}

	@Test
	public void testGetValue()
	{
		int row = table.insertRow();
		assertNull( table.getValue( row, "name"));
		table.putValue( row, "name", "Steve");
		assertNotNull( table.getValue( row, "name"));
	}
	
	@Test
	public void testGetValues_3args()
	{
		int row = table.insertRow();
		table.putValue( row, "name", "Steve");
		assertTrue( table.getValues( "age", "name", "Steve").count() >= 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetValues_int_StringArr()
	{
		int row = table.insertRow();
		table.putValue( row, "name", "Steve");
		assertTrue( table.getValues( row, new String[]{"name", "age"}).size() >= 2);
		assertTrue( table.getValues( row, new String[]{"name", "age"}).get( "name").equals( "Steve"));
		
		//failure-test
		table.getValues( row, new String[]{"name", "noSuchColumn"});
	}

	@Test
	public void testInsertRow()
	{
		int row = table.insertRow();
		assertTrue( row >= 0);
	}

	@Test
	public void testRemoveRow()
	{
		int row = table.insertRow();
		assertTrue( row >= 0);
		table.putValue( row, "name", "Eve");
		table.removeRow( row );
		
		assertNull( table.getValue( row, "name"));
	}

	@Test
	public void testFindFirstRow()
	{
		int row = table.insertRow();
		table.putValue( row, "age", -123);
		assertTrue( Objects.equals( row, table.findFirstRow( new Scope(new SimpleCondition("age", -123, Comparison.IS), null, Scope.NO_LIMIT)).getKey()));
		assertTrue( Objects.equals( row, table.findFirstRow( new Scope(new SimpleCondition("age", -123, Comparison.IS), SimpleOrder.fromSQLString( "age DESC"), Scope.NO_LIMIT)).getKey()));
	}

	@Test
	public void testFindAllRows()
	{
		int row = table.insertRow();
		table.putValue( row, "age", -123);
		table.putValue( row, "name", "Steve");
		assertTrue( table.findAllRows( new Scope(new SimpleCondition("name", "Steve", Comparison.IS), null, row)).
				anyMatch( (Map.Entry<Integer, MemoryRow> e) -> Objects.equals( e.getValue().getRowValue( "age"), -123) ));
	}
	
}
