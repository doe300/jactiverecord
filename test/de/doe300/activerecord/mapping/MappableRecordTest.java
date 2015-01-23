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
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), false).buildBase( TestMappableRecord.class);
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
