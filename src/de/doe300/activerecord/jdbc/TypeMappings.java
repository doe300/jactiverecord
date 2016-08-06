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

import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.JDBCRecordStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

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
		//converts various numerical types
		if((type.isPrimitive() || Number.class.isAssignableFrom( type)) && obj instanceof Number)
		{
			return convertNumber( (Number)obj, type);
		}
		//implicit conversion date/timestamp -> long
		if(obj instanceof Date && (Long.class.equals( type) || Long.TYPE.equals( type)))
		{
			return (T) Long.valueOf(((Date)obj).getTime());
		}
		//implicit conversion number -> timestamp
		if(obj instanceof Number && Timestamp.class.equals( type) )
		{
			return type.cast( new Timestamp(((Number)obj).longValue()));
		}
		//implicit conversion number -> java.sql.Date
		if(obj instanceof Number && java.sql.Date.class.equals( type) )
		{
			return type.cast( new java.sql.Date(((Number)obj).longValue()));
		}
		//implicit conversion number -> java.sql.Time
		if(obj instanceof Number && java.sql.Time.class.equals( type) )
		{
			return type.cast( new java.sql.Time(((Number)obj).longValue()));
		}
		//TODO String -> Timestamp ??
		throw new ClassCastException("Can't cast Object of type '"+obj.getClass()+"' to type '"+type+"'");
	}
	
	/**
	 * Converts a number to a given type. This method also converts between primitive and wrapper-types
	 * 
	 * NOTE: Conversions between numerical types can result in loss of precision!
	 * 
	 * @param <T> the numerical type
	 * @param obj the original Number to convert
	 * @param type the target type
	 * @return the converted number
	 * @throws ClassCastException 
	 * @since 0.8
	 */
	@Nullable
	@SuppressWarnings( "unchecked" )
	public static <T> T convertNumber(@Nullable final Number obj, @Nonnull final Class<T> type) throws ClassCastException
	{
		if(obj == null)
		{
			return null;
		}
		if(!(type.isPrimitive() || Number.class.isAssignableFrom( type )))
		{
			throw new IllegalArgumentException("Can only convert Numbers!");
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
		//allows for Integer -> byte, Integer -> long, ...
		if(Byte.TYPE.equals( type) || Byte.class.equals( type))
		{
			return (T)(Byte)obj.byteValue();
		}
		if(Short.TYPE.equals(type) || Short.class.equals( type))
		{
			return (T)(Short)obj.shortValue();
		}
		if(Integer.TYPE.equals( type) || Integer.class.equals( type))
		{
			return (T)(Integer)obj.intValue();
		}
		if(Long.TYPE.equals( type) || Long.class.equals( type))
		{
			return (T)(Long)obj.longValue();
		}
		if(Float.TYPE.equals( type) || Float.class.equals( type))
		{
			return (T)(Float)obj.floatValue();
		}
		if(Double.TYPE.equals( type) || Double.class.equals( type))
		{
			return (T)(Double)obj.doubleValue();
		}
		throw new IllegalArgumentException("Invalid numerical type: " + type.getCanonicalName());
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
	 * To be used in attribute-setters for XML-Strings
	 * @param xmlString the XML to store
	 * @param record
	 * @param columnName
	 * @throws SQLException
	 */
	public static void writeXML(@Nullable final String xmlString, @Nonnull final ActiveRecord record,
		@Nonnull final String columnName) throws SQLException
	{
		final Object xml;
		final Class<?> columnType = record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName);
		if(columnType.isAssignableFrom( String.class))
		{
			xml = xmlString;
		}
		else if(SQLXML.class.isAssignableFrom( columnType ))
		{
			throw new UnsupportedOperationException("Not supported!");
			//TODO doesn't support MemoryRecordStore, if column-type is SQLXML, but how to solve??
		}
		else if(record.getBase().getStore() instanceof JDBCRecordStore)
		{
			final Connection con = ((JDBCRecordStore)record.getBase().getStore()).getConnection();
			xml = con.createSQLXML();
			((SQLXML)xml).setString(xmlString);
		}
		else
		{
			throw new IllegalArgumentException("Can't store XML in given RecordStore!");
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
	
	/**
	 * Reads a serializable object from it serialized form.
	 * 
	 * Supports reading from {@link Byte byte[]} or {@link java.sql.Blob}
	 * @param <T>
	 * @param serializableType
	 * @param record
	 * @param columnName
	 * @return the read serializable object of the correct type
	 * @throws java.sql.SQLException
	 * @since 0.7
	 */
	@Nullable
	public static <T extends Serializable> T readSerializable(@Nonnull final Class<T> serializableType, @Nonnull final ActiveRecord record, @Nonnull final String columnName) throws SQLException
	{
		final Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName);
		if(value == null)
		{
			return null;
		}
		else if(value.getClass().isArray() && (value.getClass().getComponentType().equals( Byte.class) || value.getClass().getComponentType().equals( Byte.TYPE)))
		{
			try(final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream((byte[])value)))
			{
				return serializableType.cast(ois.readObject());
			}
			catch(final IOException ioe)
			{
				throw new SQLException(ioe);
			}
			catch (final ClassNotFoundException ex )
			{
				throw new IllegalArgumentException("Can't deserialize VARBINARY to nonexisting type '"+serializableType+"'");
			}
		}
		else if(value instanceof java.sql.Blob)
		{
			//extract Serializable from BLOB-field
			try(final ObjectInputStream ois = new ObjectInputStream(((java.sql.Blob)value).getBinaryStream() ))
			{
				return serializableType.cast(ois.readObject());
			}
			catch(final IOException ioe)
			{
				throw new SQLException(ioe);
			}
			catch (final ClassNotFoundException ex )
			{
				throw new IllegalArgumentException("Can't deserialize BLOB to nonexisting type '"+serializableType+"'");
			}
		}
		throw new IllegalArgumentException("Illegal column-type!");
	}
	
	/**
	 * Writes a serializable object by serializing it and storing it in the database.
	 * 
	 * Supports writing to {@link Byte byte[]} or {@link java.sql.Blob}
	 * @param <T>
	 * @param serializableObject
	 * @param record
	 * @param columnName 
	 * @throws java.sql.SQLException 
	 * @since 0.7
	 */
	public static <T extends Serializable> void writeSerializable(@Nullable final T serializableObject, @Nonnull final ActiveRecord record, @Nonnull final String columnName) throws SQLException
	{
		final Class<?> columnType = record.getBase().getStore().getAllColumnTypes( record.getBase().getTableName()).get( columnName);
		if(columnType.isArray() && (Byte.class.isAssignableFrom( columnType.getComponentType()) || Byte.TYPE.isAssignableFrom( columnType.getComponentType()))) 
		{
			try(final ByteArrayOutputStream bos = new ByteArrayOutputStream(); final ObjectOutputStream oos = new ObjectOutputStream(bos ))
			{
				oos.writeObject( serializableObject);
				record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), columnName, bos.toByteArray());
			}
			catch ( final IOException ex )
			{
				throw new SQLException(ex);
			}
		}
		else if(java.sql.Blob.class.isAssignableFrom( columnType))
		{
			try(final OutputStream stream = ((java.sql.Blob)record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), columnName)).setBinaryStream( 0);
					final ObjectOutputStream oos = new ObjectOutputStream(stream))
			{
				//XXX doesn't work if object in DB is null
				oos.writeObject(serializableObject);
			}
			catch ( final IOException ex )
			{
				throw new SQLException(ex);
			}
		}
		else
		{
			throw new IllegalArgumentException("Illegal column-type!");
		}
	}
}

