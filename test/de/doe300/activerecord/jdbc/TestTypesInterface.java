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

import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.record.ActiveRecord;
import java.io.InputStream;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * Test record-type for type-mappings
 * @author doe300
 * @see TypeMappings
 */
public interface TestTypesInterface extends ActiveRecord
{
	@Attribute(type = Types.CHAR, typeName = TypeMappings.UUID_TYPE_NAME, name = "uuid")
	public default UUID getUUID()
	{
		return TypeMappings.readUUID( this, "uuid" );
	}
	
	public default void setUUID(UUID id)
	{
		TypeMappings.writeUUID( id, this, "uuid");
	}
	
	@Attribute(type = Types.VARCHAR, typeName = TypeMappings.URL_TYPE_NAME, name = "url")
	public default URL getURL() throws MalformedURLException
	{
		return TypeMappings.readURL( this, "url");
	}
	
	public default void setURL(URL url)
	{
		TypeMappings.writeURL( url, this, "url");
	}
	
	public default URI getURI()
	{
		return TypeMappings.readURI( this, "url");
	}
	
	public default void setURI(URI uri)
	{
		TypeMappings.writeURI( uri, this, "url");
	}	
	
	@Attribute(type =  Types.VARCHAR, typeName = TypeMappings.PATH_TYPE_NAME, name = "path")
	public default Path getPath()
	{
		return TypeMappings.readPath( this, "path");
	}
	
	public default void setPath(Path path)
	{
		TypeMappings.writePath( path, this, "path");
	}
	
	@Attribute(type = Types.VARCHAR, typeName = TypeMappings.ENUM_TYPE_NAME, name = "enum")
	public default RoundingMode getEnum()
	{
		return TypeMappings.readEnumValue( RoundingMode.class, this, "enum");
	}
	
	public default void setEnum(RoundingMode mode)
	{
		TypeMappings.writeEnumValue( mode, this, "enum");
	}
	
	@Attribute(type = Types.INTEGER, name = "enum_ordinal")
	public  default RoundingMode getEnumOrdinal()
	{
		return TypeMappings.readEnumValue( RoundingMode.class, this, "enum_ordinal");
	}
	
	public default void setEnumOrdinal(RoundingMode mode)
	{
		getBase().getStore().setValue( getBase(), getPrimaryKey(), "enum_ordinal", mode.ordinal());
	}
	
	@Attribute(type = Types.VARCHAR, typeName = "VARCHAR(100)", name = "db_mappable")
	public default TestDBMappableImpl getDBMappable()
	{
		return TypeMappings.readDBMappable( TestDBMappableImpl.class, this, "db_mappable");
	}
	
	public default void setDBMappable(TestDBMappableImpl obj)
	{
		TypeMappings.writeDBMappable( obj, this, "db_mappable");
	}
	
	@Attribute(name = "xml", type = Types.SQLXML)
	public default InputStream readXML() throws SQLException
	{
		return TypeMappings.readXML( this, "xml");
	}
	
	public default void writeXML(String xml) throws SQLException
	{
		TypeMappings.writeXML( xml, this, "xml");
	}
			
	static class TestDBMappableImpl implements DBMappable
	{
		 String testString;
		int testInteger;

		public TestDBMappableImpl()
		{
		}

		/*
		Our db-value is a string separated by semicolon ';'
		*/
		@Override
		public void readFromDBValue( Object dbValue )
		{
			String[] parts = ((String)dbValue).split( "\\;");
			testString = parts[0];
			testInteger = Integer.valueOf( parts[1]);
		}

		@Override
		public Object toDBValue()
		{
			return testString+";"+testInteger;
		}
		
	}
}

