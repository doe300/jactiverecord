package de.doe300.activerecord;

import de.doe300.activerecord.migration.AutomaticMigrationTest;
import de.doe300.activerecord.migration.ManualMigrationTest;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
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
			//sa without password is the default user
			con = DriverManager.getConnection( "jdbc:hsqldb:hsql://localhost:9999/test", "sa", "" );
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
