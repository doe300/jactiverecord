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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
}
