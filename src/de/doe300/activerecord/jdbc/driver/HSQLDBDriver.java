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
package de.doe300.activerecord.jdbc.driver;

import de.doe300.activerecord.migration.constraints.IndexType;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Vendor-specific driver for the HSQLDB driver, including
 * <ul>
 * <li>The keyword for the auto-increment primary key is set to <code>IDENTITY</code></li>
 * <li>The default data-type for strings is set to <code>LONGVARCHAR</code></li>.
 * </ul>
 *
 * @author doe300
 * @since 0.5
 */
public class HSQLDBDriver extends JDBCDriver
{
	static final HSQLDBDriver INSTANCE = new HSQLDBDriver();

	private HSQLDBDriver()
	{
	}

	@Override
	public String getIndexKeyword( IndexType indexType )
	{
		//does not support any special index-types
		return "";
	}
	
	@Override
	public String getPrimaryKeyKeywords(@Nonnull final String primaryKeyKeywords)
	{
		return primaryKeyKeywords + " IDENTITY PRIMARY KEY";
	}

	@Override
	public String getStringDataType()
	{
		return "LONGVARCHAR";
	}

	@Override
	public String getSQLType(Class<?> javaType ) throws IllegalArgumentException
	{
		if(java.sql.SQLXML.class.isAssignableFrom( javaType ))
		{
			//see: http://hsqldb.org/doc/src/org/hsqldb/jdbc/JDBCSQLXML.html
			return "LONGVARCHAR";
		}
//		if(java.util.UUID.class.isAssignableFrom( javaType ))
//		{
//			//TODO HsqlDB 2.3.4+ supports UUID, but currently type-error on read (a BinaryData is returned)
//			return "UUID";
//		}
		if(Serializable.class.isAssignableFrom( javaType) && super.getSQLType( javaType ).equals( super.getSQLType( 
				Serializable.class)))	//makes sure, no special treatments are overridden
		{
			return "VARBINARY(" + Integer.MAX_VALUE + ")";
		}
		return super.getSQLType( javaType );
	}
}
