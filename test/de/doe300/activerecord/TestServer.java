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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.Assert;

import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import org.junit.Test;

/**
 * Starts the HSQLDB server for testing
 * @author doe300
 */
public class TestServer extends Assert
{
	
	static 
	{
		final Enumeration<Driver> drivers = DriverManager.getDrivers();
		while(drivers.hasMoreElements())
		{
			System.out.println( drivers.nextElement().getClass() );
		}
	}
	
	/*
	 * XXX: Need to test all test-cases for the various record-store implementations
	 */
	public static final Class<? extends RecordStore> testStore = CachedJDBCRecordStore.class;
	
	@Nonnull
	public static RecordCore getTestCore(@Nonnull final Class<? extends RecordStore> storeType) throws SQLException
	{
		RecordCore core = RecordCore.getCore( storeType.getSimpleName());
		if(core == null)
		{
			if(storeType == CachedJDBCRecordStore.class)
			{
				core = RecordCore.fromStore(storeType.getSimpleName(), new CachedJDBCRecordStore(getTestConnection()));
			}
			else if(storeType == SimpleJDBCRecordStore.class)
			{
				core = RecordCore.fromStore( storeType.getSimpleName(), new SimpleJDBCRecordStore(getTestConnection()));
			}
			else if(storeType == MemoryRecordStore.class)
			{
				core = RecordCore.fromStore( storeType.getSimpleName(), new MemoryRecordStore());
			}
			else
			{
				throw new IllegalArgumentException("Unrecognized store-type: " + storeType);
			}
		}
		return core;
	}
	
	public static Connection getTestConnection() throws SQLException
	{
		return DriverManager.getConnection( "jdbc:hsqldb:mem:test", "sa", "");
		//FIXME SQLite errors
		/**
		 */
//		return DriverManager.getConnection("jdbc:sqlite::memory:");
		//FIXME MySQL errors:
		/**
		 * - Seems to not be able to put any index on columns about a certain length (~700 characters/bytes)
		 * - can't use VARCHAR as result-type for CAST, needs to be CHAR for casting to string
		 */
		//start server with "systemctl start mysqld.service"
//		return DriverManager.getConnection( "jdbc:mysql://localhost:3306/mysql", "root", "");

		//FIXME PostgreSQL errors:
		/**
		 */
		//start server with "systemctl start postgresql.service"
//		return DriverManager.getConnection( "jdbc:postgresql:postgres", "postgres", "");
	}
	
	public static void buildTestMappingTable(@Nonnull final RecordStore store, @Nonnull final String tableName) throws Exception
	{
		final Map<String, Class<?>> columns = new HashMap<>(2);
		columns.put( "fk_test1", Integer.class);
		columns.put( "fk_test2", Integer.class);
		Assert.assertTrue( store.getDriver().createMigration( tableName, columns, store).apply());
	}
	
	public static void buildTestMappingTables(@Nonnull final String tableName) throws Exception
	{
		final Map<String, Class<?>> columns = new HashMap<>(2);
		columns.put( "fk_test1", Integer.class);
		columns.put( "fk_test2", Integer.class);
		Assert.assertTrue( getTestCore(SimpleJDBCRecordStore.class).getStore().getDriver().createMigration( tableName, columns, getTestCore(SimpleJDBCRecordStore.class).getStore()).apply());
		try
		{
			//may fail when simple JDBC and cached JDBC use same database - not the case for SQLite
			getTestCore(CachedJDBCRecordStore.class).getStore().getDriver().createMigration( tableName, columns, getTestCore(CachedJDBCRecordStore.class).getStore()).apply();
		}
		catch(Exception e)
		{
			
		}
		Assert.assertTrue( getTestCore(MemoryRecordStore.class).getStore().getDriver().createMigration( tableName, columns, getTestCore(MemoryRecordStore.class).getStore()).apply());
	}
	
	public static void buildTestTable(@Nonnull final RecordStore store, @Nonnull final Class<? extends ActiveRecord> type, @Nonnull final String tableName) throws SQLException, Exception
	{
		Assert.assertTrue( store.getDriver().createMigration( type, tableName, store ).apply());
	}
	
	public static void buildTestTables(@Nonnull final Class<? extends ActiveRecord> type, @Nonnull final String tableName) throws SQLException, Exception
	{
		Assert.assertTrue( getTestCore(SimpleJDBCRecordStore.class).getStore().getDriver().createMigration( type, tableName, getTestCore(SimpleJDBCRecordStore.class).getStore() ).apply());
		try
		{
			//may fail when simple JDBC and cached JDBC use same database - not the case for SQLite
			getTestCore(CachedJDBCRecordStore.class).getStore().getDriver().createMigration( type, tableName, getTestCore(CachedJDBCRecordStore.class).getStore() ).apply();
		}
		catch(Exception e)
		{
			
		}
		Assert.assertTrue( getTestCore(MemoryRecordStore.class).getStore().getDriver().createMigration( type, tableName, getTestCore(MemoryRecordStore.class).getStore() ).apply());
	}
	
	public static void destroyTestMappingTable(@Nonnull final RecordStore store, @Nonnull final String tableName) throws Exception
	{
		final Map<String, Class<?>> columns = new HashMap<>(2);
		columns.put( "fk_test1", Integer.class);
		columns.put( "fk_test2", Integer.class);
		Assert.assertTrue( store.getDriver().createMigration( tableName, columns, store).revert());
	}
	
	public static void destroyTestMappingTables(@Nonnull final String tableName) throws Exception
	{
		final Map<String, Class<?>> columns = new HashMap<>(2);
		columns.put( "fk_test1", Integer.class);
		columns.put( "fk_test2", Integer.class);
		Assert.assertTrue( getTestCore(SimpleJDBCRecordStore.class).getStore().getDriver().createMigration( tableName, columns, getTestCore(SimpleJDBCRecordStore.class).getStore()).revert());
		try
		{
			//may fail when simple JDBC and cached JDBC use same database - not the case for SQLite
			getTestCore(CachedJDBCRecordStore.class).getStore().getDriver().createMigration( tableName, columns, getTestCore(CachedJDBCRecordStore.class).getStore()).revert();
		}
		catch(Exception e)
		{
			
		}
		Assert.assertTrue( getTestCore(MemoryRecordStore.class).getStore().getDriver().createMigration( tableName, columns, getTestCore(MemoryRecordStore.class).getStore()).revert());
	}
	
	public static void destroyTestTable(@Nonnull final RecordStore store, @Nonnull final Class<? extends ActiveRecord> type, @Nonnull final String tableName) throws SQLException, Exception
	{
		Assert.assertTrue( store.getDriver().createMigration( type, tableName, store ).revert());
	}
	
	public static void destroyTestTables(@Nonnull final Class<? extends ActiveRecord> type, @Nonnull final String tableName) throws SQLException, Exception
	{
		Assert.assertTrue( getTestCore(SimpleJDBCRecordStore.class).getStore().getDriver().createMigration( type, tableName, getTestCore(SimpleJDBCRecordStore.class).getStore() ).revert());
		try
		{
			//may fail when simple JDBC and cached JDBC use same database - not the case for SQLite
			getTestCore(CachedJDBCRecordStore.class).getStore().getDriver().createMigration( type, tableName, getTestCore(CachedJDBCRecordStore.class).getStore() ).revert();
		}
		catch(Exception e)
		{
			
		}
		Assert.assertTrue( getTestCore(MemoryRecordStore.class).getStore().getDriver().createMigration( type, tableName, getTestCore(MemoryRecordStore.class).getStore() ).revert());
	}
	
	@Test
	public void printMetaData() throws Exception
	{
		final Connection con = getTestConnection();
		final DatabaseMetaData data = con.getMetaData();
		System.out.println( "Driver:" );
		System.out.println( "- Name: "+data.getDriverName());
		System.out.println( "- Version: "+data.getDriverVersion() );
		
		System.out.println( "Database: " );
		System.out.println( "- Name: " +data.getDatabaseProductName() );
		System.out.println( "- Version: "+data.getDatabaseProductVersion() );
		
		try(final ResultSet res = data.getTypeInfo())
		{
			System.out.println( "Data Types:" );
			while(res.next())
			{
				System.out.println( "- " + res.getString( "TYPE_NAME" ) + " = " + getFieldName( res.getInt( "DATA_TYPE")));
			}
		}
	}
	
	private String getFieldName(int value) throws IllegalArgumentException, IllegalAccessException
	{
		for(final Field f : java.sql.Types.class.getFields())
		{
			if(f.getType() == Integer.TYPE && ((int)f.get( null)) == value)
			{
				return java.sql.Types.class.getCanonicalName() + "#" + f.getName();
			}
		}
		return null;
	}
}
