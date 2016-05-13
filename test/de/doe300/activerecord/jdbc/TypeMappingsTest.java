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

import de.doe300.activerecord.AssertException;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
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
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

public class TypeMappingsTest extends TestBase implements AssertException
{
	private final RecordBase<TestTypesInterface> base;
	
	public TypeMappingsTest(final RecordCore core)
	{
		super(core);
		this.base = core.getBase( TestTypesInterface.class );
		for(Map.Entry<String, Class<?>> type: base.getStore().getAllColumnTypes( base.getTableName() ).entrySet())
		{
			System.out.println( type.getKey()+": " + type.getValue() );
		}
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables( TestTypesInterface.class, "testTypes");
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		TestServer.destroyTestTables(TestTypesInterface.class, "testTypes");
	}

	@Test
	public void testUUID()
	{
		TestTypesInterface record = base.createRecord();
		UUID id = UUID.randomUUID();
		assertNull( record.getUUID());
		record.setUUID( id);
		assertEquals( id, record.getUUID());
	}

	@Test
	public void testURL() throws Exception
	{
		TestTypesInterface record = base.createRecord();
		URL url = new URL("http://www.google.de");
		assertNull( record.getURL());
		record.setURL( url );
		assertEquals( url, record.getURL());
	}

	@Test
	public void testURI()
	{
		TestTypesInterface record = base.createRecord();
		URI uri = URI.create( "mailto:test@example.com");
		assertNull( record.getURI());
		record.setURI( uri );
		assertEquals( uri, record.getURI());
	}

	@Test
	public void testPath()
	{
		TestTypesInterface record = base.createRecord();
		Path path = Paths.get( System.getProperty( "user.home") );
		assertNull( record.getPath());
		record.setPath( path );
		assertEquals( path, record.getPath());
	}

	@Test
	public void testEnumValue()
	{
		TestTypesInterface record = base.createRecord();
		RoundingMode mode = RoundingMode.HALF_UP;
		assertNull( record.getEnum());
		record.setEnum( mode );
		assertEquals( mode, record.getEnum());
		//test ordinal for enum
		record.setEnumOrdinal( mode);
		assertEquals( mode, record.getEnumOrdinal());
		//error-test
		base.getStore().setValue( base, record.getPrimaryKey(), "enum", "no such entry");
		
		assertThrows( IllegalArgumentException.class, () -> record.getEnum());
	}
	
	@Test
	public void testDBMappable()
	{
		TestTypesInterface record = base.createRecord();
		assertNull( record.getDBMappable());
		TestTypesInterface.TestDBMappableImpl impl = new TestTypesInterface.TestDBMappableImpl();
		impl.testString = "Test123";
		impl.testInteger = 456;
		record.setDBMappable( impl );
		assertEquals( impl.testString, record.getDBMappable().testString);
		assertEquals( impl.testInteger, record.getDBMappable().testInteger);
	}
	
	@Test
	public void testXML() throws IOException, SQLException
	{
		String xmlString = "<tag><subtag/>some text</tag>";
		TestTypesInterface record = base.createRecord();
		try
		{
			assertNull( record.readXML());
			record.writeXML( xmlString);
		}
		catch(final UnsupportedOperationException e)
		{
			//allow for exception to be thrown
			return;
		}
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
		assertTrue( TypeMappings.coerceToType( 1, Boolean.class));
		assertEquals( Long.valueOf( 123), TypeMappings.coerceToType( new Date(123), Long.class));
		assertEquals( Long.valueOf( 123), TypeMappings.coerceToType( new Date(123), Long.TYPE));
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
	
	@Test
	public void testSerializable() throws SQLException
	{
		final UUID uuid = UUID.randomUUID();
		TestTypesInterface record = base.createRecord();
		assertNull( record.getSerializable());
		record.setSerializable(uuid );
		assertEquals( uuid, record.getSerializable());
		
	}
}
