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
