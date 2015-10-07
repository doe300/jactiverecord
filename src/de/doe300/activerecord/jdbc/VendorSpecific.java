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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Function;

import javax.annotation.Nonnull;

import de.doe300.activerecord.logging.Logging;
import javax.annotation.Nullable;

/**
 *
 * @author doe300
 */
public enum VendorSpecific
{
	/**
	 * Vendor-specific settings for MySQL Databases
	 * <br>
	 * These settings include:
	 * <ul>
	 * <li>The keyword for the auto-increment primary key is set to <code>AUTO_INCREMENT</code></li>
	 * <li>The default data-type for strings is set to <code>VARCHAR(4096)</code>.
	 * The maximum limit for a cell width is 65535 which is simultaneously the maximum width for all columns in a row.
	 * Since we don't know how much cells a row will have, I set the limit to 4096 which allows for up to 16 such string-column.
	 * </li>
	 * </ul>
	 */
	MYSQL("AUTO_INCREMENT", "VARCHAR(4096)"),
	/**
	 * Vendor-specific settings for the HSQLDB driver, including
	 * <ul>
	 * <li>The keyword for the auto-increment primary key is set to <code>IDENTITY</code></li>
	 * <li>The default data-type for strings is set to <code>LONGVARCHAR</code></li>.
	 * </ul>
	 */
	HSQLDB("IDENTITY", "LONGVARCHAR"),
	/**
	 * Vendor-specific settings for the SQLite database, including:
	 * <ul>
	 * <li>According to the official documentation (<a href="https://www.sqlite.org/autoinc.html">SQLite doc autoincrement</a>),
	 * the <code>AUTOINCREMENT</code> keyword should be avoided and <code>INTEGER PRIMARY KEY</code> implies an automatic increment.
	 * </li>
	 * <li>According to the official documentation (<a href="https://www.sqlite.org/faq.html#q9">SQLite FAQ</a>),
	 * the length of a <code>VARCHAR</code> is not limited and the length-value is ignored.
	 * </li>
	 * <li>SQLite has no <code>boolean</code>-type (<a href="https://www.sqlite.org/datatype3.html">SQLite data-types</a>),
	 * so the <code>boolean</code> values are stored as integers, zero (0) for <code>false</code>, one (1) for <code>true</code>
	 * </li>
	 * </ul>
	 */
	SQLITE("", "VARCHAR(1)")
	{
		@Override
		public String convertBooleanToDB( final boolean value )
		{
			return value ? "1" : "0";
		}

		@Override
		public boolean convertDBToBoolean( final Object value )
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
	@Nonnull
	private final String autoIncrement;
	@Nonnull
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

	private VendorSpecific(@Nonnull final String autoIncrement, @Nonnull final String stringDefaultType)
	{
		this.autoIncrement = autoIncrement;
		this.stringDataType = stringDefaultType;
	}

	/**
	 * This method tries to guess the vendor-specific settings from reading the
	 * {@link DatabaseMetaData#getDatabaseProductName() database product-name}.
	 * @param con
	 * @return the database vendor specific settings
	 */
	@Nullable
	public static VendorSpecific guessDatabaseVendor(@Nonnull final Connection con)
	{
		try
		{
			final String productName = con.getMetaData().getDatabaseProductName();
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
		catch(final SQLException e)
		{
			Logging.getLogger().error( "VendorSpecific", e);
		}
		return null;
	}

	/**
	 * @param con
	 * @param term
	 * @return whether the term is a reserved keyword, either in SQL92 or vendor-specific
	 * @throws SQLException
	 */
	public boolean isReservedKeyword(@Nonnull final Connection con, @Nonnull final String term) throws SQLException
	{
		if(Arrays.stream( VendorSpecific.sql92Keywords ).anyMatch( (final String s) -> s.equalsIgnoreCase( term)))
		{
			return true;
		}
		final String[] keyWords = con.getMetaData().getSQLKeywords().split( "\\s*,\\s*");
		return Arrays.stream( keyWords ).anyMatch( (final String s) -> s.equalsIgnoreCase( term));
	}

	/**
	 * @return the keyword for an auto-incremental column
	 */
	@Nonnull
	public String getAutoIncrementKeyword()
	{
		return autoIncrement;
	}

	/**
	 * @return the default data-type for strings
	 */
	@Nonnull
	public String getStringDataType()
	{
		return stringDataType;
	}

	/**
	 * @param value
	 * @return the value to write into the DB
	 */
	@Nonnull
	public String convertBooleanToDB(final boolean value)
	{
		return Boolean.toString( value );
	}

	/**
	 * @param value
	 * @return the boolean-value from the DB
	 */
	public boolean convertDBToBoolean(@Nonnull final Object value)
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
	@Nonnull
	public static String convertIdentifier(@Nonnull final String input, @Nonnull final Connection con)
	{
		try
		{
			final DatabaseMetaData meta = con.getMetaData();

			//check for identifier quote string
			final String quote = meta.getIdentifierQuoteString();
			Function<String,String> quoteFunc;
			if(" ".equals( quote))
			{
				//do not enquote
				quoteFunc = (final String id) -> id;
			}
			else
			{
				quoteFunc = (final String id) -> quote+id+quote;
			}
			if(meta.storesMixedCaseIdentifiers())
			{
				return quoteFunc.apply( input);
			}
			if(meta.storesUpperCaseIdentifiers())
			{
				return quoteFunc.apply( VendorSpecific.convertIdentifierWithoutQuote(input, con ));
			}
			if(meta.storesLowerCaseIdentifiers())
			{
				return quoteFunc.apply( VendorSpecific.convertIdentifierWithoutQuote( input, con ));
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
	@Nonnull
	public static String convertIdentifierWithoutQuote(@Nonnull final String input, @Nonnull final Connection con)
	{
		try
		{
			final DatabaseMetaData meta = con.getMetaData();

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
