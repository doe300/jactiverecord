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

import de.doe300.activerecord.migration.MigrationTest;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.annotation.Nonnull;
import org.junit.Assert;

/**
 * Starts the HSQLDB server for testing
 * @author doe300
 */
public class TestServer extends Assert
{
	
	static 
	{
		Enumeration<Driver> drivers = DriverManager.getDrivers();
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
	public static RecordCore getTestCore() throws SQLException
	{
		return getTestCore( testStore );
	}
	
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
	
	private static Connection con;
	
	public static Connection getTestConnection() throws SQLException
	{
		if(con == null || con.isClosed())
		{
			con = DriverManager.getConnection( "jdbc:hsqldb:mem:test", "sa", "");
			//FIXME SQLite errors:
			/**
			 * - At AutomaticMigration#revert throws "database table is locked"
			 *		Some Query not yet closed? Perhaps some query with stream, they could stay open if not read to the end
			 */
//			con = DriverManager.getConnection("jdbc:sqlite::memory:");
			//FIXME MySQL errors:
			/**
			 * - ???
			 */
			//start server with "systemctl start mysqld.service"
//			con = DriverManager.getConnection( "jdbc:mysql://localhost:3306/mysql", "root", "");
			
			//FIXME PostgreSQL errors:
			/**
			 * none currently...
			 */
			//start server with "systemctl start postgresql.service"
//			con = DriverManager.getConnection( "jdbc:postgresql:postgres", "postgres", "");
			
		}
		printMetaData( con );
		return con;
	}
	
	public static void buildTestTables() throws SQLException, Exception
	{
		buildTestTables(testStore);
	}
	
	public static void buildTestTables(@Nonnull final Class<? extends RecordStore> testStore) throws SQLException, Exception
	{
		MigrationTest.init();
		new MigrationTest().testApply();
	}
	
	public static void destroyTestTables() throws SQLException, Exception
	{
		destroyTestTables(testStore);
	}
	
	public static void destroyTestTables(@Nonnull final Class<? extends RecordStore> testStore) throws SQLException, Exception
	{
		new MigrationTest().testRevert();
	}
	
	static void printMetaData(Connection con) throws SQLException
	{
		DatabaseMetaData data = con.getMetaData();
		System.out.println( "Driver:" );
		System.out.println( "- Name: "+data.getDriverName());
		System.out.println( "- Version: "+data.getDriverVersion() );
		
		System.out.println( "Database: " );
		System.out.println( "- Name: " +data.getDatabaseProductName() );
		System.out.println( "- Version: "+data.getDatabaseProductVersion() );
	}	
}
