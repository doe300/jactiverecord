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
package de.doe300.activerecord.migration;

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
public class ManualMigrationTest
{
	private static ManualMigration mig;
	private static Connection con;
	
	public ManualMigrationTest()
	{
	}
	
	@BeforeClass
	public static void init() throws SQLException
	{
		String build = "CREATE TABLE mappingTable (fk_test1 INTEGER, fk_test2 INTEGER)";
		String update = "ALTER TABLE mappingTable ADD info varchar(255)";
		String crash = "DROP TABLE mappingTable";
		mig = new ManualMigration(build, update, crash);
		con = TestServer.getTestConnection();
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