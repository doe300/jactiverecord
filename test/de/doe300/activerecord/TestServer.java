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

import de.doe300.activerecord.migration.AutomaticMigrationTest;
import de.doe300.activerecord.migration.ManualMigrationTest;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryMigrationTest;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.annotation.Nonnull;
import org.hsqldb.Server;
import org.junit.Assert;
import org.junit.Test;

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
	
	public static void main(String[] args)
	{
		Server hsqlServer = new Server();
		hsqlServer.setLogWriter(new PrintWriter(System.out));
		hsqlServer.setErrWriter( new PrintWriter(System.err));
        hsqlServer.setSilent(false);
        hsqlServer.setDatabaseName(0, "test");
        hsqlServer.setDatabasePath(0, "file:test/test");
		hsqlServer.setNoSystemExit( true);
		hsqlServer.setPort( 9999);
		
		hsqlServer.start();
	}
	
	/*
	 * XXX: Need to test all test-cases for the various record-store implementations
	 */
	public static final Class<? extends RecordStore> testStore = SimpleJDBCRecordStore.class;
	
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
			 * - Can't find column '*' -> does not interpret as all-columns
			 * - At AutomaticMigration#revert throws "database table is locked"
			 *		Some Query not yet closed? Perhaps some query with stream, they could stay open if not read to the end
			 */
//			con = DriverManager.getConnection("jdbc:sqlite::memory:");
			//FIXME MySQL errors:
			/**
			 * - Table-names are case-sensitive, so all access must use same case
			 *		Fails at creation of testTable and trying to write/read TESTTABLE
			 *		But method calls are exactly the same for different test, which do no fail?!?
			 */
			//start server with "systemctl start mysqld.service"
//			con = DriverManager.getConnection( "jdbc:mysql://localhost:3306/mysql", "root", "");
			
			//FIXME need testing:
//			con = DriverManager.getConnection( "jdbc:postgresql:database");
			
			//TODO run all tests with MemoryRecordStore
			
		}
		return con;
	}
	
	public static void printTestTable() throws SQLException
	{
		try(ResultSet set = getTestConnection().prepareCall( "SELECT * FROM TESTTABLE").executeQuery())
		{
			while(set.next())
			{
				System.out.println( set.getInt( 1)+", "+set.getString( 2)+", "+set.getInt( 3)+", "+set.getTimestamp( "created_at")+", "+set.getTimestamp( "updated_at") );
			}
		}
	}
	
	public static void buildTestTables() throws SQLException, Exception
	{
		if(testStore == MemoryRecordStore.class )
		{
			new MemoryMigrationTest().testApply();
		}
		else
		{
			AutomaticMigrationTest.testDataMigration.testApply();
			ManualMigrationTest.init();
			new ManualMigrationTest().testApply();
		}
	}
	
	public static void destroyTestTables() throws SQLException, Exception
	{
		if(testStore == MemoryRecordStore.class)
		{
			new MemoryMigrationTest().testRevert();
		}
		else
		{
			AutomaticMigrationTest.testDataMigration.testRevert();
			new ManualMigrationTest().testRevert();
		}
	}
	
	@Test
	public void printMetaData() throws SQLException
	{
		DatabaseMetaData data = getTestConnection().getMetaData();
		System.out.println( "Driver:" );
		System.out.println( "- Name: "+data.getDriverName());
		System.out.println( "- Version: "+data.getDriverVersion() );
		
		System.out.println( "Datenbank: " );
		System.out.println( "- Name: " +data.getDatabaseProductName() );
		System.out.println( "- version: "+data.getDatabaseProductVersion() );
	}	
}
