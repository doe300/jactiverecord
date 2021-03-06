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
package de.doe300.activerecord.migration.constraints;

import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;



public class IndexTypeTest extends TestBase
{
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables( TestIndexedRecord.class, "TestIndexedRecord");
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables( TestIndexedRecord.class, "TestIndexedRecord" );
	}
	
	public IndexTypeTest(final RecordCore core)
	{
		super(core);
	}
	
	@Test
	public void test_NON_UNIQUE()
	{
		assertEquals( "CREATE  INDEX index1 ON table1 (column1, column2)", IndexType.DEFAULT.toSQL(JDBCDriver.DEFAULT, "table1", "index1",
				new String[]{"column1", "column2"}));
	}
	
	@Test
	public void test_UNIQUE()
	{
		assertEquals( "CREATE UNIQUE INDEX  ON table1 (column1, column2)", IndexType.UNIQUE.toSQL(JDBCDriver.DEFAULT, "table1", null,
				new String[]{"column1", "column2"}));
	}
	
	@Test
	public void test_CLUSTERED()
	{
		assertEquals( "CREATE CLUSTERED INDEX index1 ON table1 (column1)", IndexType.CLUSTERED.toSQL(JDBCDriver.DEFAULT, "table1", "index1",
				new String[]{"column1"}));
	}
	
	@Indices({
		@Index(name = "name_index", columns = {"name"}, type = IndexType.DEFAULT),
		@Index(name = "age_index", columns = {"age"}, type = IndexType.CLUSTERED),
		@Index(name = "both_index", columns = {"name", "age"}, type = IndexType.UNIQUE)
	})
	public static interface TestIndexedRecord extends ActiveRecord
	{
		public String getName();
		
		public int getAge();
	}
}
