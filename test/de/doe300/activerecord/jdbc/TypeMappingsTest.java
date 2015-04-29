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
package de.doe300.activerecord.jdbc;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.migration.AutomaticMigration;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;



public class TypeMappingsTest extends Assert
{
	private static RecordBase<TestTypesInterface> base;
	
	public TypeMappingsTest()
	{
	}
	
	@BeforeClass
	public static void setUpClass() throws SQLException
	{
		Connection con = TestServer.getTestConnection();
		new AutomaticMigration(TestTypesInterface.class, false).apply( con);
		base = RecordCore.fromDatabase( con, false).buildBase( TestTypesInterface.class);
	}
	
	@AfterClass
	public static void tearDownClass() throws SQLException
	{
		new AutomaticMigration(TestTypesInterface.class, false).revert( base.getStore().getConnection());
	}

	@Test
	public void testUUID()
	{
		TestTypesInterface record = base.createRecord();
		UUID id = UUID.randomUUID();
		record.setUUID( id);
		assertEquals( id, record.getUUID());
	}

	@Test
	public void testURL() throws Exception
	{
		TestTypesInterface record = base.createRecord();
		URL url = new URL("http://www.google.de");
		record.setURL( url );
		assertEquals( url, record.getURL());
	}

	@Test
	public void testURI()
	{
		TestTypesInterface record = base.createRecord();
		URI uri = URI.create( "mailto:test@example.com");
		record.setURI( uri );
		assertEquals( uri, record.getURI());
	}

	@Test
	public void testPath()
	{
		TestTypesInterface record = base.createRecord();
		Path path = Paths.get( System.getProperty( "user.home") );
		record.setPath( path );
		assertEquals( path, record.getPath());
	}

	@Test
	public void testEnumValue()
	{
		TestTypesInterface record = base.createRecord();
		RoundingMode mode = RoundingMode.HALF_UP;
		record.setEnum( mode );
		assertEquals( mode, record.getEnum());
	}
	
	@Test
	public void testDBMappable()
	{
		TestTypesInterface record = base.createRecord();
		TestTypesInterface.TestDBMappableImpl impl = new TestTypesInterface.TestDBMappableImpl();
		impl.testString = "Test123";
		impl.testInteger = 456;
		record.setDBMappable( impl );
		assertEquals( impl.testString, record.getDBMappable().testString);
		assertEquals( impl.testInteger, record.getDBMappable().testInteger);
	}
}
