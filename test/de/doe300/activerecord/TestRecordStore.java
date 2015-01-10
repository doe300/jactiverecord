package de.doe300.activerecord;

import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.MapRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author doe300
 */
@RunWith(Parameterized.class)
@Deprecated
public class TestRecordStore
{
	//TODO move to recordstore-impls tests
	private final RecordBase<TestInterface> base;

	public TestRecordStore( RecordStore store )
	{
		this.base = RecordCore.fromStore( store.getClass().getName(), store).buildBase(TestInterface.class);
	}
	
	public static void main(String[] args)
	{
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while(drivers.hasMoreElements())
		{
			System.out.println(drivers.nextElement() );
		}
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParams() throws SQLException
	{
		final Object[][] params=new Object[][]
		{
			{new MapRecordStore()},
			{new SimpleJDBCRecordStore(TestServer.getTestConnection())},
			{new CachedJDBCRecordStore(TestServer.getTestConnection())},
		};
		return Arrays.asList( params );
	}
	
	@AfterClass
	public static void printTable() throws SQLException
	{
		TestServer.printTestTable();
	}
	
	@Test
	public void testAddRecord() throws Exception
	{
		Assert.assertNotNull( base.createRecord());
		Assert.assertNotNull( base.findFirst( new SimpleCondition(base.getPrimaryColumn(), null, Comparison.IS_NOT_NULL)));
	}
	
	@Test
	public void testDestroyRecord() throws Exception
	{
		ActiveRecord record = base.createRecord();
		Assert.assertNotNull(record );
		int primaryKey = record.getPrimaryKey();
		Assert.assertNotNull( base.getRecord( primaryKey) );
		base.destroy( primaryKey);
		Assert.assertNull( base.getRecord( primaryKey) );
		Assert.assertFalse( record.isSynchronized());
		Assert.assertFalse( record.inRecordStore());
	}
	
	@Test
	public void testSetValue()
	{
		
	}
	
	@Test
	public void testGetValue()
	{
		
	}
}
