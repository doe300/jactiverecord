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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import org.hsqldb.Server;

/**
 * Starts the HSQLDB server for testing
 * @author doe300
 */
public class TestServer
{
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
	
	private static Connection con;
	
	public static Connection getTestConnection() throws SQLException
	{
		if(con == null || con.isClosed())
		{
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while(drivers.hasMoreElements())
			{
				System.out.println( drivers.nextElement().getClass() );
			}
			//sa without password is the default user
			//con = DriverManager.getConnection( "jdbc:hsqldb:hsql://localhost:9999/test", "sa", "" );
			con = DriverManager.getConnection( "jdbc:hsqldb:mem:test", "sa", "");
		}
		return con;
	}
	
	public static void printTestTable() throws SQLException
	{
		ResultSet set = getTestConnection().prepareCall( "SELECT * FROM TESTTABLE").executeQuery();
		while(set.next())
		{
			System.out.println( set.getInt( 1)+", "+set.getString( 2)+", "+set.getInt( 3)+", "+set.getTimestamp( "created_at")+", "+set.getTimestamp( "updated_at") );
		}
	}
	
	public static void buildTestTables() throws SQLException, Exception
	{
		AutomaticMigrationTest.init();
		new AutomaticMigrationTest().testApply();
		ManualMigrationTest.init();
		new ManualMigrationTest().testApply();
		
	}
	
	public static void destroyTestTables() throws SQLException, Exception
	{
		new AutomaticMigrationTest().testRevert();
		new ManualMigrationTest().testRevert();
	}
}
