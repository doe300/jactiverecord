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

import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.DBDriver;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.function.Function;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Signed;

/**
 * The abstract driver for JDBC-based storages
 * 
 * @author doe300
 * @since 0.5
 * @see https://en.wikibooks.org/wiki/SQL_Dialects_Reference
 */
public class JDBCDriver implements DBDriver
{
	//TODO unify isTypeSupported(Class)/getSQLType(int/Class) and getJavaType(String)
	public static final int STRING_TYPE_LENGTH = 4096;
	
	public static final String AGGREGATE_COUNT_ALL= "COUNT(*)";
	public static final String AGGREGATE_COUNT_NOT_NULL="COUNT(%column%)";
	public static final String AGGREGATE_COUNT_DISTINCT="COUNT(DISTINCT %column%)";
	public static final String AGGREGATE_SUM="CAST(SUM(%column%) AS BIGINT)";
	public static final String AGGREGATE_SUM_DOUBLE="CAST(SUM(%column%) AS DOUBLE)";
	public static final String AGGREGATE_AVERAGE="AVG(%column%)";
	public static final String AGGREGATE_MINIMUM="MIN(%column%)";
	public static final String AGGREGATE_MAXIMUM="MAX(%column%)";
	
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
	
	@Override
	public boolean isTypeSupported(Class<?> javaType )
	{
		try
		{
			return getSQLType( javaType ) != null;
		}
		catch(final IllegalArgumentException iae)
		{
			return false;
		}
	}
	
	/**
	 * @param offset the offset to start at
	 * @param limit the maximum number of results
	 * @return the SQL-clause to limit the amount of retrieved results
	 */
	@Nonnull
	public String getLimitClause(@Nonnegative final int offset, @Signed final int limit)
	{
		return (offset > 0 ? "OFFSET " + offset + " " : "") + (limit > 0 ? "FETCH FIRST " + limit + " ROWS ONLY" : "");
	}
	
	/**
	 * @param aggregateFunction the aggregate-function to apply
	 * @param column the column to aggregate
	 * @return the SQL aggregate-function for the given column
	 */
	@Nonnull
	public String getAggregateFunction(@Nonnull final String aggregateFunction, @Nonnull final String column)
	{
		return aggregateFunction.replaceAll( "%column%", column);
	}

	/**
	 * For the default-implementation, see: https://en.wikibooks.org/wiki/SQL_Dialects_Reference/Data_structure_definition/Auto-increment_column
	 * 
	 * @return the keyword for an auto-incremental column
	 */
	@Nonnull
	public String getAutoIncrementKeyword()
	{
		return "GENERATED ALWAYS AS IDENTITY";
	}

	/**
	 * The default implementation provides a 4kB string-column
	 * 
	 * @return the default data-type for strings
	 */
	@Nonnull
	public String getStringDataType()
	{
		return "VARCHAR("+STRING_TYPE_LENGTH+")";
	}

	/**
	 * By default, this method acts as the DB supports boolean as data-type
	 * 
	 * @param value
	 * @return the value to write into the DB
	 */
	@Nonnull
	public String convertBooleanToDB(final boolean value)
	{
		return Boolean.toString( value );
	}

	/**
	 * By default, this method acts as the DB supports boolean as data-type
	 * 
	 * @param value
	 * @return the boolean-value from the DB
	 */
	public boolean convertDBToBoolean(@Nonnull final Object value)
	{
		return ( boolean ) value;
	}
	
	/**
	 * NOTE: The result of this method may be inaccurate.
	 *
	 * @param javaType
	 * @return the mapped SQL-type
	 * @throws IllegalArgumentException
	 * @see "http://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/jdbc/getstart/mapping.doc.html"
	 * @see java.sql.Types
	 */
	public String getSQLType( final Class<?> javaType) throws IllegalArgumentException
	{
		//map SQL-types
		if(java.sql.Array.class.isAssignableFrom( javaType ))
		{
			return "ARRAY";
		}
		if(java.sql.Blob.class.isAssignableFrom( javaType ))
		{
			return "BLOB";
		}
		if(java.sql.Clob.class.isAssignableFrom( javaType ))
		{
			return "CLOB";
		}
		if(java.sql.NClob.class.isAssignableFrom( javaType ))
		{
			return "NCLOB";
		}
		if(java.sql.Ref.class.isAssignableFrom( javaType ))
		{
			return "REF";
		}
		if(java.sql.RowId.class.isAssignableFrom( javaType ))
		{
			return "ROWID";
		}
		if(java.sql.SQLXML.class.isAssignableFrom( javaType ))
		{
			return "XML";
		}
		if ( java.sql.Date.class.isAssignableFrom( javaType ) )
		{
			return "DATE";
		}
		if ( java.sql.Time.class.isAssignableFrom( javaType ) )
		{
			return "TIME";
		}
		if ( java.sql.Timestamp.class.isAssignableFrom( javaType ) )
		{
			return "TIMESTAMP";
		}
		//map builting-types
		if ( javaType.equals( String.class ) )
		{
			return getStringDataType();
		}
		if ( BigDecimal.class.isAssignableFrom( javaType ) )
		{
			return "NUMERIC";
		}
		if ( javaType.equals( Boolean.class ) || javaType.equals( Boolean.TYPE ) )
		{
			return "BIT";
		}
		if ( javaType.equals( Byte.class ) || javaType.equals( Byte.TYPE ) )
		{
			return "TINYINT";
		}
		if ( javaType.equals( Short.class ) || javaType.equals( Short.TYPE ) )
		{
			return "SHORTINT";
		}
		if ( javaType.equals( Integer.class ) || javaType.equals( Integer.TYPE ) )
		{
			return "INTEGER";
		}
		if ( javaType.equals( Long.class ) || javaType.equals( Long.TYPE ) )
		{
			return "BIGINT";
		}
		if ( javaType.equals( Float.class ) || javaType.equals( Float.TYPE ) )
		{
			return "REAL";
		}
		if ( javaType.equals( Double.class ) || javaType.equals( Double.TYPE ) )
		{
			return "DOUBLE";
		}
		if ( ActiveRecord.class.isAssignableFrom( javaType ) )
		{
			//for foreign key
			return "INTEGER";
		}
		if ( javaType.isEnum() )
		{
			//for enum-name
			return "VARCHAR(255)";
		}
		throw new IllegalArgumentException( "Type not mapped: " + javaType );
	}

	/**
	 * This is the inverted method of {@link #getSQLType(java.lang.Class) }
	 *
	 * @param sqlType
	 * @return the java class-type
	 * @throws IllegalArgumentException
	 * @since 0.3
	 */
	public Class<?> getJavaType( final String sqlType ) throws IllegalArgumentException
	{
		//map SQL-types
		if(sqlType.startsWith( "ARRAY"))
		{
			return java.sql.Array.class;
		}
		if(sqlType.startsWith( "BLOB"))
		{
			return java.sql.Blob.class;
		}
		if(sqlType.startsWith( "CLOB"))
		{
			return java.sql.Clob.class;
		}
		if(sqlType.startsWith( "NCLOB"))
		{
			return java.sql.NClob.class;
		}
		if(sqlType.startsWith( "REF"))
		{
			return java.sql.Ref.class;
		}
		if(sqlType.startsWith( "ROWID"))
		{
			return java.sql.RowId.class;
		}
		if(sqlType.startsWith( "XML"))
		{
			return java.sql.SQLXML.class;
		}
		if ( sqlType.startsWith( "DATE" ) )
		{
			return Date.class;
		}
		if ( sqlType.startsWith( "TIMESTAMP" ) )
		{
			return Timestamp.class;
		}
		if ( sqlType.startsWith( "TIME" ) )
		{
			return Time.class;
		}
		//map built-in types
		if ( sqlType.startsWith( "VARCHAR" ) || sqlType.startsWith( "CHAR" ) )
		{
			return String.class;
		}
		if ( sqlType.startsWith( "NUMERIC" ) )
		{
			return BigDecimal.class;
		}
		if ( sqlType.startsWith( "BIT" ) )
		{
			return Boolean.class;
		}
		if ( sqlType.startsWith( "TINYINT" ) )
		{
			return Byte.class;
		}
		if ( sqlType.startsWith( "SHORTINT" ) )
		{
			return Short.class;
		}
		//MySQL has data-type INT
		if ( sqlType.startsWith( "INT" ) )
		{
			return Integer.class;
		}
		if ( sqlType.startsWith( "BIGINT" ) )
		{
			return Long.class;
		}
		if ( sqlType.startsWith( "REAL" ) )
		{
			return Float.class;
		}
		if ( sqlType.startsWith( "DOUBLE" ) )
		{
			return Double.class;
		}
		throw new IllegalArgumentException( "Type not mapped: " + sqlType );
	}
	
	/**
	 * @param con
	 * @param term
	 * @return whether the term is a reserved keyword, either in SQL92 or vendor-specific
	 * @throws SQLException
	 */
	public boolean isReservedKeyword(@Nonnull final Connection con, @Nonnull final String term) throws SQLException
	{
		if(Arrays.stream( sql92Keywords ).anyMatch( (final String s) -> s.equalsIgnoreCase( term)))
		{
			return true;
		}
		final String[] keyWords = con.getMetaData().getSQLKeywords().split( "\\s*,\\s*");
		return Arrays.stream( keyWords ).anyMatch( (final String s) -> s.equalsIgnoreCase( term));
	}
	
	/**
	 * This method tries to guess the vendor-specific driver from reading the
	 * {@link DatabaseMetaData#getDatabaseProductName() database product-name}.
	 * @param con
	 * @return the database vendor specific driver
	 */
	@Nonnull
	public static JDBCDriver guessDriver(@Nullable final Connection con)
	{
		if(con != null)
		{
			try
			{
				//TODO make singletons of drivers??
				final String productName = con.getMetaData().getDatabaseProductName();
				if(productName.contains( "HSQL"))
				{
					return new HSQLDBDriver();
				}
				if(productName.contains( "MySQL"))
				{
					return new MySQLDriver();
				}
				if(productName.contains( "SQLite"))
				{
					return new SQLiteDriver();
				}
				//TODO postgres
				Logging.getLogger().info( "JDBCDriver", "No vendor-specific driver found for: " + productName);
			}
			catch(final SQLException e)
			{
				Logging.getLogger().error( "JDBCDriver", e);
			}
		}
		return new JDBCDriver();
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
