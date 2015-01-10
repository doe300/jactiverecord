package de.doe300.activerecord.migration;

import de.doe300.activerecord.TestInterface;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class ManualMigrationTest
{
	private static ManualMigration mig;
	private static Connection con;
	
	public ManualMigrationTest()
	{
		//FIXME passes test but does not create or drop table!!
	}
	
	@BeforeClass
	public static void init() throws SQLException
	{
		String build = "CREATE TABLE mappingTable (fk_test1 INTEGER, fk_test2 INTEGER)";
		String update = "ALTER TABLE mappingTable ADD info varchar(255)";
		String crash = "DROP TABLE mappingTable";
		mig = new ManualMigration(build, update, crash);
		con = TestInterface.createTestConnection();
	}

	@Test
	public void testApply() throws Exception
	{
		Assert.assertTrue(mig.apply( con ));
	}
	
	@Test
	public void testUpdate() throws Exception
	{
		Assert.assertTrue(mig.update(con ));
	}

	@Test
	public void testRevert() throws Exception
	{
		Assert.assertTrue(mig.revert( con ));
	}
}