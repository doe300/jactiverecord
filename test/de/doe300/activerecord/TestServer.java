package de.doe300.activerecord;

import java.io.PrintWriter;
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
}
