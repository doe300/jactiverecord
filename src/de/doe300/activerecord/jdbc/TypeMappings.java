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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;

import de.doe300.activerecord.RecordException;
import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.record.ActiveRecord;
import java.io.ByteArrayInputStream;

/**
 * Utility class to provide type mappings for commonly used types
 * @author doe300
 */
public final class TypeMappings
{
	private TypeMappings()
	{
	}

	private static final Map<Class<?>, Class<?>> primitivesToWrapper= new HashMap<>(10);
	static
	{
		TypeMappings.primitivesToWrapper.put(boolean.class, Boolean.class);
		TypeMappings.primitivesToWrapper.put(byte.class, Byte.class);
		TypeMappings.primitivesToWrapper.put(char.class, Character.class);
		TypeMappings.primitivesToWrapper.put(double.class, Double.class);
		TypeMappings.primitivesToWrapper.put(float.class, Float.class);
		TypeMappings.primitivesToWrapper.put(int.class, Integer.class);
		TypeMappings.primitivesToWrapper.put(long.class, Long.class);
		TypeMappings.primitivesToWrapper.put(short.class, Short.class);
		TypeMappings.primitivesToWrapper.put(void.class, Void.class);
	}

	////
	// General
	////

	/**
	 * Coerces the object to the given type. This method will run some vendor-specific conversions
	 * @param <T>
	 * @param obj
	 * @param type
	 * @return the coerced object
	 * @throws ClassCastException
	 */
	@Nullable
	@SuppressWarnings( "unchecked" )
	public static <T> T coerceToType(@Nullable final Object obj, @Nonnull final Class<T> type) throws ClassCastException
	{
		if(obj == null)
		{
			return null;
		}
		if(type.isInstance( obj ))
		{
			return type.cast( obj );
		}
		//there is no int.cast(Integer), so just implicitly cast it
		if(type.isPrimitive() && TypeMappings.primitivesToWrapper.get( type).isInstance( obj ))
		{
			return ( T ) obj;
		}
		//for SQLite, where Booleans are represented as Integer
		if(obj instanceof Number && (Boolean.class.equals( type) || Boolean.TYPE.equals( type)))
		{
			return ( T ) (((Number)obj).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE);
		}
		//implicit conversion date/timestamp -> long
		if(obj instanceof Date && (Long.class.equals( type) || Long.TYPE.equals( type)))
		{
			return (T) Long.valueOf(((Date)obj).getTime());
		}
		//implicit conversion long/Long -> timestamp
		if(obj instanceof Long && Timestamp.class.equals( type) )
		{
			return type.cast( new Timestamp((Long)obj));
		}
		throw new ClassCastException("Can't cast Object of type '"+obj.getClass()+"' to type '"+type+"'");
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
	@Nullable
	public static UUID readUUID(@Nonnull final ActiveRecord record, @Nonnull final String columnName)
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
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
	public static void writeUUID(@Nullable final UUID uuid, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName)
	{
		final Object value;
		if(record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName).isAssignableFrom( UUID.class))
		{
			value = uuid;
		}
		else
		{
			value = uuid == null ? null : uuid.toString();
		}
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, value);
	}

	////
	// URL / URI
	////

	/**
	 * To be used in attribute-getters for URLs
	 * @param record
	 * @param columnName
	 * @return the URL or <code>null</code>
	 * @throws MalformedURLException
	 */
	@Nullable
	public static URL readURL(@Nonnull final ActiveRecord record, @Nonnull final String columnName)
		throws MalformedURLException
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
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
	@Nullable
	public static URI readURI(@Nonnull final ActiveRecord record, @Nonnull final String columnName)
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
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
	public static void writeURL(@Nullable final URL url, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName)
	{
		final Object value;
		if(record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName).isAssignableFrom( URL.class))
		{
			value = url;
		}
		else
		{
			value = url == null ? null : url.toExternalForm();
		}
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, value);
	}

	/**
	 * To be used in attribute-setters for URIs
	 * @param uri
	 * @param record
	 * @param columnName
	 */
	public static void writeURI(@Nullable final URI uri, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName)
	{
		final Object value;
		if(record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName).isAssignableFrom( URI.class))
		{
			value = uri;
		}
		else
		{
			value = uri == null ? null : uri.toString();
		}
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, value);
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
	@Nullable
	public static <T extends Source> T readXML(@Nonnull final Class<T> sourceType, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName) throws SQLException
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(sourceType.isInstance( value))
		{
			return sourceType.cast( value );
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
	@Nullable
	public static InputStream readXML(@Nonnull final ActiveRecord record, @Nonnull final String columnName)
		throws SQLException
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(value instanceof SQLXML)
		{
			return ((SQLXML)value).getBinaryStream();
		}
		if(value instanceof String)
		{
			return new ByteArrayInputStream(((String)value).getBytes());
		}
		return null;
	}

	/**
	 * To be used in attribute-setters for XML DOMResults
	 * @param result the result to copy the content from
	 * @param record
	 * @param columnName
	 * @throws SQLException
	 */
	public static void writeXML(@Nullable final DOMResult result, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName) throws SQLException
	{
		final Object value;
		if(record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName).isAssignableFrom( DOMResult.class))
		{
			value = result;
		}
		else
		{
			final Connection con = record.getBase().getStore().getConnection();
			if(con == null || con.isClosed())
			{
				throw new RecordException(record, "no JDBC-Connection for this database");
			}
			if (result == null)
			{
				record.getBase().getStore().setValue(record.getBase(), record.getPrimaryKey(), columnName, null);
				return;
			}
			final SQLXML xml = con.createSQLXML();
			final DOMResult writeResult = xml.setResult( DOMResult.class);
			writeResult.setNextSibling( result.getNextSibling());
			writeResult.setNode( result.getNode());
			writeResult.setSystemId( result.getSystemId());
			value = xml;
		}
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, value);
	}

	/**
	 * To be used in attribute-setters for XML-Strings
	 * @param xmlString the XML to store
	 * @param record
	 * @param columnName
	 * @throws SQLException
	 */
	public static void writeXML(@Nullable final String xmlString, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName) throws SQLException
	{
		//TODO doesn't support MemoryRecordStore, if column-type is SQLXML, but how to solve??
		final Object xml;
		if(record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName).isAssignableFrom( String.class))
		{
			xml = xmlString;
		}
		else
		{
			final Connection con = record.getBase().getStore().getConnection();
			if(con == null || con.isClosed())
			{
				throw new RecordException(record, "no JDBC-Connection for this database");
			}
			xml = con.createSQLXML();
			((SQLXML)xml).setString(xmlString);
		}
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, xml);
	}

	/**
	 * To be used in attribute-getters for Path
	 * @param record
	 * @param columnName
	 * @return the Path or <code>null</code>
	 */
	@Nullable
	public static Path readPath(@Nonnull final ActiveRecord record, @Nonnull final String columnName)
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(value instanceof Path)
		{
			//maybe some driver has a mapping for Path
			return (Path)value;
		}
		if(value instanceof URI)
		{
			//maybe some driver has a mapping for URI
			return Paths.get((URI)value);
		}
		return Paths.get( (String)value);
	}

	/**
	 * To be used in attribute-setters for Paths
	 * @param path
	 * @param record
	 * @param columnName
	 */
	public static void writePath(@Nullable final Path path, @Nonnull final ActiveRecord record, @Nonnull final String columnName)
	{
		final Object value;
		final Class<?> columnType = record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName);
		if(columnType.isAssignableFrom( Path.class))
		{
			value = path;
		}
		else if(columnType.isAssignableFrom( URI.class))
		{
			value = path == null ? null : path.toUri();
		}
		else
		{
			value = path == null ? null : path.toString();
		}
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, value);
	}

	/**
	 * This method tries to determine the enum-value from the {@link Enum#name()}-method before using the index of {@link Class#getEnumConstants() }
	 * @param <T>
	 * @param enumType
	 * @param record
	 * @param columnName
	 * @return the enum-value or <code>null</code>
	 */
	@Nullable
	public static <T extends Enum<T>> T readEnumValue(@Nonnull final Class<T> enumType,
		@Nonnull final ActiveRecord record, @Nonnull final String columnName)
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(enumType.isInstance( value))
		{
			return enumType.cast( value );
		}
		if(value instanceof String)
		{
			return Enum.valueOf( enumType, value.toString());
		}
		if(value instanceof Number)
		{
			return enumType.getEnumConstants()[((Number)value).intValue()];
		}
		throw new IllegalArgumentException("Can't find enum-constant from value: "+value.toString());
	}

	/**
	 * Writes the enum-value by storing its {@link Enum#name() }
	 * @param <T>
	 * @param enumValue
	 * @param record
	 * @param columnName
	 */
	public static <T extends Enum<T>> void writeEnumValue(@Nullable final T enumValue, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName)
	{
		final Object value;
		if(Enum.class.isAssignableFrom( record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName)))
		{
			value = enumValue;
		}
		else
		{
			value = enumValue == null ? null : enumValue.name();
		}
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, value);
	}

	/**
	 * The <code>objectType</code> is required to have a default constructor
	 * @param <T> the type of the attribute
	 * @param objectType
	 * @param record
	 * @param columnName
	 * @return the read DB-mappable attribute
	 */
	@Nullable
	public static <T extends DBMappable> T readDBMappable(@Nonnull final Class<T> objectType,
		@Nonnull final ActiveRecord record, @Nonnull final String columnName)
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		if(objectType.isInstance( value ))
		{
			return objectType.cast( value );
		}
		try
		{
			final T result = objectType.newInstance();
			result.readFromDBValue( value );
			return result;
		}
		catch(final ReflectiveOperationException roe)
		{
			throw new RuntimeException(roe);
		}
	}

	/**
	 * Reads the DBMappable {@link DBMappable#readFromDBValue(java.lang.Object) reading} into <code>attribute</code>
	 * @param attribute the DBMappable to read into
	 * @param record
	 * @param columnName
	 */
	public static void readDBMappable(@Nonnull final DBMappable attribute, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName)
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return;
		}
		attribute.readFromDBValue( value );
	}

	/**
	 * Writes the DBMappable by storing the {@link DBMappable#toDBValue() db-mapped Object}
	 * @param attribute
	 * @param record
	 * @param columnName
	 */
	public static void writeDBMappable(@Nullable final DBMappable attribute, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName)
	{
		final Object dbMapped = attribute == null ? null : attribute.toDBValue();
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, dbMapped);
	}
}

