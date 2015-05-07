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

import de.doe300.activerecord.logging.Logging;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Function;

/**
 *
 * @author doe300
 */
public enum VendorSpecific
{
	//originally the limit is 65535 (the maximum length of the row), but with other columns existing, we need to choose a smaller length
	MYSQL("AUTO_INCREMENT", "VARCHAR(4096)"),
	HSQLDB("IDENTITY", "LONGVARCHAR"),
	//Not strictly required for keeping an unique ID - https://www.sqlite.org/autoinc.html
	//SQLite has no Limit on VARCHAR - https://www.sqlite.org/faq.html#q9
	//SQLite has no boolean data-type - https://www.sqlite.org/datatype3.html
	SQLITE("", "VARCHAR(1)")
	{
		@Override
		public String convertBooleanToDB( boolean value )
		{
			return value ? "1" : "0";
		}

		@Override
		public boolean convertDBToBoolean( Object value )
		{
			int val;
			if(value instanceof Number)
			{
				val = ((Number)value).intValue();
			}
			else
			{
				val = Integer.valueOf( value.toString() );
			}
			//value of 1 is true, 0 is false
			return val != 0;
		}
		
	};
		
	private final String autoIncrement;
	private final String stringDataType;
	
	private static final String[] sql92Keywords = {
		"absolute", "action", "allocate", "are", "assertion",
		"bit", "bit_length", "both", "cascaded", "case", "cast", "catalog", "char", "char_length", "character",
		"character_length", "coalesce", "collate", "collation", "column", "connection", "constraints", "corresponding",
		"cross", "current_date", "current_time", "current_timestamp", "current_user", "date", "day", "dec", "decimal",
		"deferrable", "deferred", "describe", "descriptor", "diagnostics", "disconnect", "domain",
		"end-exec", "exception", "extract", "false", "first", "float", "found", "full", "get", "global", "go", "hour",
		"immediate", "indicator", "initially", "inner", "input", "insensitive", "int", "integer", "interval",
		"join", "language", "last", "leading", "left", "local", "lower", "match", "minute", "module", "month",
		"names", "natural", "nchar", "next", "no", "nullif", "numeric", "octet_length", "outer", "output", "overlaps",
		"pad", "partial", "position", "preserve", "prior", "real", "relative", "restrict", "right", "scroll", "second",
		"section", "session_user", "size", "smallint", "space", "sql", "sqlcode", "sqlerror", "sqlstate", "substring",
		"system_user", "then", "time", "timestamp", "timezone_hour", "timezone_minute", "trailing", "translate",
		"translation", "trim", "true", "unknown", "upper", "usage", "value", "varchar", "when", "whenever", "write",
		"year", "zone"
	};

	private VendorSpecific( String autoIncrement, String stringDefaultType)
	{
		this.autoIncrement = autoIncrement;
		this.stringDataType = stringDefaultType;
	}
	
	public static VendorSpecific guessDatabaseVendor(Connection con)
	{
		try
		{
			String productName = con.getMetaData().getDatabaseProductName();
			if(productName.contains( "HSQL"))
			{
				return HSQLDB;
			}
			if(productName.contains( "MySQL"))
			{
				return MYSQL;
			}
			if(productName.contains( "SQLite"))
			{
				return SQLITE;
			}
			//TODO postgres
		}
		catch(SQLException e)
		{
			Logging.getLogger().error( "VendorSpecific", e);
		}
		return null;
	}
	
	/**
	 * @param con
	 * @param term
	 * @return whether the therm is a reserved keyword, either in SQL92 or vendor-specific
	 * @throws SQLException 
	 */
	public boolean isReservedKeyword(Connection con, String term) throws SQLException
	{
		if(Arrays.stream( sql92Keywords ).anyMatch( (String s) -> s.equalsIgnoreCase( term)))
		{
			return true;
		}
		String[] keyWords = con.getMetaData().getSQLKeywords().split( "\\s*,\\s*");
		return Arrays.stream( keyWords ).anyMatch( (String s) -> s.equalsIgnoreCase( term));
	}
	
	/**
	 * @return the keyword for an auto-incremental column
	 */
	public String getAutoIncrementKeyword()
	{
		return autoIncrement;
	}
	
	/**
	 * @return the default data-type for strings
	 */
	public String getStringDataType()
	{
		return stringDataType;
	}

	/**
	 * @param value
	 * @return the value to write into the DB
	 */
	public String convertBooleanToDB(boolean value)
	{
		return Boolean.toString( value );
	}
	
	/**
	 * @param value
	 * @return the boolean-value from the DB
	 */
	public boolean convertDBToBoolean(Object value)
	{
		return ( boolean ) value;
	}
	
	
	/**
	 * Converts the input identifier to the case used in the DB
	 * Also adds quotes, if the database-vendor supports them
	 * @param input
	 * @param con
	 * @return the input in the correct case
	 */
	public static String convertIdentifier(final String input, final Connection con)
	{
		try
		{
			DatabaseMetaData meta = con.getMetaData();
			
			//check for identifier quote string
			String quote = meta.getIdentifierQuoteString();
			Function<String,String> quoteFunc;
			if(" ".equals( quote))
			{
				//do not enquote
				quoteFunc = (String id) -> id;
			}
			else
			{
				quoteFunc = (String id) -> quote+id+quote;
			}
			if(meta.storesUpperCaseIdentifiers())
			{
				return quoteFunc.apply( convertIdentifierWithoutQuote(input, con ));
			}
			if(meta.storesLowerCaseIdentifiers())
			{
				return quoteFunc.apply( convertIdentifierWithoutQuote( input, con ));
			}

		}
		catch ( final SQLException ex )
		{
			throw new RuntimeException(ex);
		}
		return input;
	}
	
	/**
	 * Converts the input identifier to the case used in the DB. Does not quote the input string
	 * @param input
	 * @param con
	 * @return the input in the correct case
	 */
	public static String convertIdentifierWithoutQuote(String input, Connection con)
	{
		try
		{
			DatabaseMetaData meta = con.getMetaData();
			
			if(meta.storesUpperCaseIdentifiers())
			{
				return input.toUpperCase();
			}
			if(meta.storesLowerCaseIdentifiers())
			{
				return input.toLowerCase();
			}
		}
		catch ( final SQLException ex )
		{
			throw new RuntimeException(ex);
		}
		return input;
	}
}
