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
package de.doe300.activerecord.dsl;

import de.doe300.activerecord.jdbc.VendorSpecific;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Syntax;

/**
 *
 * @author doe300
 */
public interface SQLCommand
{
	/**
	 * @param vendorSpecifics the vendor-specifics, may be <code>null</code>
	 * @param tableName the name to use to uniquely identify the table
	 * @return the sQL representation of this statement
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String toSQL(@Nullable final VendorSpecific vendorSpecifics, @Nullable final String tableName);
	
	/**
	 * This method is used to make sure, every table in a condition is uniquely identified
	 * @param currentIdentifier
	 * @return the identifier for an associated table
	 */
	@Nonnull
	public static String getNextTableIdentifier(@Nullable final String currentIdentifier)
	{
		if(currentIdentifier == null)
		{
			return "thisTable";
		}
		if("thisTable".equals( currentIdentifier))
		{
			return "associatedTable";
		}
		if(currentIdentifier.startsWith( "associatedTable"))
		{
			String numString = currentIdentifier.substring( "associatedTable".length() );
			if(numString.isEmpty())
			{
				return "associatedTable1";
			}
			try
			{
				Integer num = Integer.parseInt( numString);
				return "associatedTable"+num;
			}
			catch(NumberFormatException nfe)
			{
				return "otherAssociatedTable";
			}
		}
		if("otherTable".equals( currentIdentifier))
		{
			return "associatedTable";
		}
		return "otherTable";
	}
}
