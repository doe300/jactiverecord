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
import de.doe300.activerecord.TestServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;
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
		base = TestServer.getTestCore().getBase( TestTypesInterface.class);
		base.getCore().createTable( TestTypesInterface.class);
		for(Map.Entry<String, Class<?>> type: base.getStore().getAllColumnTypes( base.getTableName() ).entrySet())
		{
			System.out.println( type.getKey()+": " + type.getValue() );
		}
	}
	
	@AfterClass
	public static void tearDownClass() throws SQLException
	{
		base.getCore().dropTable(TestTypesInterface.class);
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

	@Test(expected = IllegalArgumentException.class)
	public void testEnumValue()
	{
		TestTypesInterface record = base.createRecord();
		RoundingMode mode = RoundingMode.HALF_UP;
		record.setEnum( mode );
		assertEquals( mode, record.getEnum());
		//test ordinal for enum
		record.setEnumOrdinal( mode);
		assertEquals( mode, record.getEnumOrdinal());
		//error-test
		base.getStore().setValue( base, record.getPrimaryKey(), "enum", "no such entry");
		record.getEnum();
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
	
	@Test
	public void testXML() throws SQLException, IOException
	{
		String xmlString = "<tag><subtag/>some text</tag>";
		TestTypesInterface record = base.createRecord();
		record.writeXML( xmlString);
		String resultXML;
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(xmlString.getBytes().length); InputStream is = record.readXML())
		{
			int b;
			while((b = is.read()) != -1)
			{
				bos.write( b);
			}
			resultXML = bos.toString();
		}
		assertEquals( xmlString, resultXML);
	}
	
	@Test
	public void testCoerceToType()
	{
		assertTrue(TypeMappings.coerceToType( true, Boolean.class));
		assertTrue(TypeMappings.coerceToType( 1, Boolean.TYPE));
		assertEquals( Long.valueOf( 123), TypeMappings.coerceToType( new Date(123), Long.class));
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		assertEquals( ts, TypeMappings.coerceToType( ts.getTime(), Timestamp.class));
		try{
			TypeMappings.coerceToType( new TreeSet<Object>(), String.class);
			fail( "Failed to throw exception");
		}
		catch(ClassCastException e)
		{
			
		}
	}
}
