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

	HSQLDBDriver()
	{
	}
	
	@Override
	public String getAutoIncrementKeyword()
	{
		return "IDENTITY";
	}

	@Override
	public String getStringDataType()
	{
		return "LONGVARCHAR";
	}

	@Override
	public String getSQLType( int jdbcType ) throws IllegalArgumentException
	{
		if(jdbcType == java.sql.Types.SQLXML)
		{
			//see: http://hsqldb.org/doc/src/org/hsqldb/jdbc/JDBCSQLXML.html
			return "LONGVARCHAR";
		}
		return super.getSQLType( jdbcType );
	}
}
