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

import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.record.ActiveRecord;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.UUID;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;

/**
 * Utility class to provide type mappings for commonly used types
 * @author doe300
 */
public final class TypeMappings
{
	private TypeMappings()
	{
	}
	
	////
	//	UUID
	////
	
	/**
	 * Type-name for the {@link UUID}.
	 *  @see Attribute#typeName()
	 */
	public static final String UUID_TYPE_NAME = "CHAR(36)";	//8-4-4-4-12 characters
	
	/**
	 * To be used in the getter-method for UUIDs
	 * @param record
	 * @param columnName
	 * @return the UUID or <code>null</code>
	 */
	public static UUID readUUID(ActiveRecord record, String columnName)
	{
		Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(value instanceof UUID)
		{
			//maybe some driver has a mapping for UUID
			return (UUID)value;
		}
		if(value.getClass().isArray() && value.getClass().getComponentType() == Byte.TYPE)
		{
			return UUID.nameUUIDFromBytes(( byte[] ) value);
		}
		return UUID.fromString( value.toString());
	}
	
	/**
	 * To be used in setter-methods for UUID
	 * @param uuid
	 * @param record
	 * @param columnName
	 */
	public static void writeUUID(UUID uuid, ActiveRecord record, String columnName)
	{
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, uuid == null ? null : uuid.toString());
	}

	////
	// URL / URI
	////
	
	/**
	 * Type-name for a {@link URL}, limited to 255 characters
	 * @see Attribute#typeName() 
	 */
	public static final String URL_TYPE_NAME = "VARCHAR(255)";
	
	/**
	 * Type-name for a {@link URI}, limited to 255 characters
	 * @see Attribute#typeName() 
	 */
	public static final String URI_TYPE_NAME = "VARCHAR(255)";
	
	/**
	 * To be used in attribute-getters for URLs
	 * @param record
	 * @param columnName
	 * @return the URL or <code>null</code>
	 * @throws MalformedURLException 
	 */
	public static URL readURL(ActiveRecord record, String columnName) throws MalformedURLException
	{
		Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(value instanceof URL)
		{
			//maybe some driver has a mapping for URL
			return (URL)value;
		}
		return new URL(value.toString());
	}
	
	/**
	 * To be used in attribute-getter for URIs
	 * @param record
	 * @param columnName
	 * @return the URI or <code>null</code>
	 */
	public static URI readURI(ActiveRecord record, String columnName)
	{
		Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(value instanceof URI)
		{
			//maybe some driver has a mapping for URI
			return (URI)value;
		}
		return URI.create( value.toString());
	}
	
	/**
	 * To be used in attribute-setters for URLs
	 * @param url
	 * @param record
	 * @param columnName 
	 */
	public static void writeURL(URL url, ActiveRecord record, String columnName)
	{
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, url == null ? null : url.toExternalForm());
	}
	
	/**
	 * To be used in attribute-setters for URIs
	 * @param uri
	 * @param record
	 * @param columnName
	 */
	public static void writeURI(URI uri, ActiveRecord record, String columnName)
	{
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, uri == null ? null : uri.toString());
	}
	
	////
	//	XML
	////
	
	/**
	 * Type for XML
	 * @see Attribute#type() 
	 * @see Types#SQLXML
	 */
	public static final int XML_TYPE = Types.SQLXML;
	
	/**
	 * To be used in attribute-getters for XML Sources
	 * @param <T> 
	 * @param sourceType the type of the source
	 * @param record
	 * @param columnName
	 * @return the Source or <code>null</code>
	 * @throws SQLException 
	 * @see SQLXML#getSource(java.lang.Class) 
	 */
	public static <T extends Source> T readXML(Class<T> sourceType, ActiveRecord record, String columnName) throws SQLException
	{
		Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(value instanceof SQLXML)
		{
			return ((SQLXML)value).getSource( sourceType );
		}
		return null;
	}
	
	/**
	 * To be used in attribute-getters for XML InputStreams
	 * @param record
	 * @param columnName
	 * @return the InputStream or <code>null</code>
	 * @throws SQLException 
	 */
	public static InputStream readXML(ActiveRecord record, String columnName) throws SQLException
	{
		Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(value instanceof SQLXML)
		{
			return ((SQLXML)value).getBinaryStream();
		}
		return null;
	}
	
	/**
	 * To be used in attribute-setters for XML DOMResults
	 * @param <T>
	 * @param result the result to copy the content from
	 * @param record
	 * @param columnName
	 * @throws SQLException 
	 */
	public static <T extends Result> void writeXML(DOMResult result, ActiveRecord record, String columnName) throws SQLException
	{
		Connection con = record.getBase().getStore().getConnection();
		if(con == null || con.isClosed())
		{
			throw new RecordException(record, "no JDBC-Connection for this database");
		}
		SQLXML xml = con.createSQLXML();
		DOMResult writeResult = xml.setResult( DOMResult.class);
		writeResult.setNextSibling( result.getNextSibling());
		writeResult.setNode( result.getNode());
		writeResult.setSystemId( result.getSystemId());
		
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, xml);
	}
	
	/**
	 * To be used in attribute-setters for XML-Strings
	 * @param xmlString the XML to store
	 * @param record
	 * @param columnName
	 * @throws SQLException 
	 */
	public static void writeXML(String xmlString, ActiveRecord record, String columnName) throws SQLException
	{
		Connection con = record.getBase().getStore().getConnection();
		if(con == null || con.isClosed())
		{
			throw new RecordException(record, "no JDBC-Connection for this database");
		}
		SQLXML xml = con.createSQLXML();
		xml.setString( xmlString );
		
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, xml);
	}
}
