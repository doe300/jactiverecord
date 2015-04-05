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

import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestPOJO;
import de.doe300.activerecord.TestServer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author daniel
 */
@RunWith(Parameterized.class)
public class AutomaticMigrationTest
{
	private final AutomaticMigration mig;
	private static Connection con;
	static{
		try
		{
			con = TestServer.getTestConnection();
		}
		catch ( SQLException ex )
		{
			throw new RuntimeException(ex);
		}
	}
	
	public AutomaticMigrationTest(AutomaticMigration migration)
	{
		this.mig = migration;
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() throws SQLException
	{
		return Arrays.asList(
			new Object[]{new AutomaticMigration(TestInterface.class, true)},
			new Object[]{new AutomaticMigration(TestPOJO.class, true)}
		);
	}
	
	//is used by other tests to build/drop test table
	public static final AutomaticMigrationTest testDataMigration = 
			new AutomaticMigrationTest(new AutomaticMigration(TestInterface.class, true));

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
		//TODO
	}
	
}
