package de.doe300.activerecord.migration;

import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class AutomaticMigrationTest
{
	private static AutomaticMigration mig;
	private static Connection con;
	
	public AutomaticMigrationTest()
	{
	}
	
	@BeforeClass
	public static void init() throws SQLException
	{
		mig = new AutomaticMigration(TestInterface.class, true);
		con = TestServer.getTestConnection();
	}

	@Test
	public void testApply() throws SQLException
	{
		Assert.assertTrue(mig.apply( con ));
	}
	
	@Test
	public void testUpdate() throws SQLException
	{
		Assert.assertFalse(mig.update(con ));
	}

	@Test
	public void testRevert() throws SQLException
	{
		Assert.assertTrue(mig.revert( con ));
	}

	@Test
	public void testGetSQLType_int()
	{
	}

	@Test
	public void testGetSQLType_Class()
	{
	}
	
}
