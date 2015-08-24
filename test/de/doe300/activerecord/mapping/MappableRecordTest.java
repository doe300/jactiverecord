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
package de.doe300.activerecord.mapping;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.migration.AutomaticMigration;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class MappableRecordTest extends Assert
{
	private static AutomaticMigration migration;
	private static RecordBase<TestMappableRecord> base;
	private static TestMappableRecord record;
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		migration = new AutomaticMigration(TestMappableRecord.class, false);
		migration.apply( TestServer.getTestConnection() );
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), false).getBase( TestMappableRecord.class);
		record = base.createRecord();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		migration.revert( TestServer.getTestConnection());
	}
	
	public static interface TestMappableRecord extends MappableRecord
	{
		public String getName();
		
		public void setName(String name);
		
		public int getAge();
		
		public void setAge(int age);
	}

	@Test
	public void testGetAttributes()
	{
		record.setAge( 23);
		record.setName( "Adam");
		Map<String,Object> map = record.getAttributes();
		assertEquals( "Adam", map.get( "name"));
		assertEquals( 23, map.get( "age"));
	}

	@Test
	public void testGetAttribute()
	{
		record.setAge( 100);
		record.setName( "Eve");
		assertEquals( 100, record.getAttribute( "age"));
		assertEquals( "Eve", record.getAttribute( "name"));
	}

	@Test
	public void testSetAttributes()
	{
		Map<String,Object> map = new HashMap<>(2);
		map.put( "name", "Alex");
		map.put( "age", 112);
		record.setAttributes( map );
		assertEquals( "Alex", record.getName());
		assertEquals( 112, record.getAge());
	}

	@Test
	public void testSetAttribute()
	{
		record.setAttribute("age", 10);
		record.setAttribute( "name", "Johnny");
		assertEquals( 10, record.getAge());
		assertEquals( "Johnny", record.getName());
	}
}
