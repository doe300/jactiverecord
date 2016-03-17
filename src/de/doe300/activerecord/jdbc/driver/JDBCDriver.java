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

import de.doe300.activerecord.dsl.joins.JoinType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.function.Function;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Signed;
import javax.annotation.Syntax;

import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.migration.ManualMigration;
import de.doe300.activerecord.migration.Migration;
import de.doe300.activerecord.migration.constraints.IndexType;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.DBDriver;
import de.doe300.activerecord.store.JDBCRecordStore;
import de.doe300.activerecord.store.RecordStore;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The abstract driver for JDBC-based storages
 *
 * @author doe300
 * @since 0.5
 * @see "https://en.wikibooks.org/wiki/SQL_Dialects_Reference"
 */
public class JDBCDriver implements DBDriver
{
	public static final int STRING_TYPE_LENGTH = 4096;

	// TODO move functions out of here
	public static final String AGGREGATE_COUNT_ALL= "COUNT(*)";
	public static final String AGGREGATE_COUNT_NOT_NULL="COUNT(%column%)";
	public static final String AGGREGATE_COUNT_DISTINCT="COUNT(DISTINCT %column%)";
	public static final String AGGREGATE_SUM="CAST(SUM(%column%) AS BIGINT)";
	public static final String AGGREGATE_SUM_DOUBLE="CAST(SUM(%column%) AS DOUBLE)";
	public static final String AGGREGATE_AVERAGE="AVG(%column%)";
	public static final String AGGREGATE_MINIMUM="MIN(%column%)";
	public static final String AGGREGATE_MAXIMUM="MAX(%column%)";

	public static final String SCALAR_VALUE = "%column%";
	public static final String SCALAR_ABS = "CAST(ABS(%column%) AS BIGINT)";
	public static final String SCALAR_ABS_DOUBLE = "CAST(ABS(%column%) AS DOUBLE)";
	public static final String SCALAR_SIGN = "CAST(SIGN(%column%) AS BIGINT)";
	public static final String SCALAR_FLOOR = "FLOOR(%column%)";
	public static final String SCALAR_CEILING = "CEIL(%column%)";
	public static final String SCALAR_ROUND = "CAST(ROUND(%column%) AS BIGINT)";
	public static final String SCALAR_SQRT = "SQRT(%column%)";
	public static final String SCALAR_UPPER = "UPPER(%column%)";
	public static final String SCALAR_LOWER = "LOWER(%column%)";
	public static final String SCALAR_STRING_LENGTH = "CHAR_LENGTH(%column%)";
	public static final String SCALAR_TRIM = "TRIM(%column%)";
	
	public static final String JOIN_INNER = "INNER JOIN";
	public static final String JOIN_LEFT_OUTER = "LEFT OUTER JOIN";
	public static final String JOIN_RIGHT_OUTER = "RIGHT OUTER JOIN";
	public static final String JOIN_FULL_OUTER = "FULL OUTER JOIN";

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
	public boolean isTypeSupported(final Class<?> javaType )
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
	 * NOTE: If the specified {@link IndexType} is not supported by the driver,
	 * fallback to the {@link IndexType#DEFAULT default} index-type is allowed
	 *
	 * @param indexType the index-type to translate to driver-specific keyword
	 * @return the keyword for the given index-type
	 * @since 0.6
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String getIndexKeyword(@Nonnull final IndexType indexType)
	{
		switch(indexType)
		{
			case DEFAULT:
				return "";
			case UNIQUE:
				return "UNIQUE";
			case CLUSTERED:
				return "CLUSTERED";
			default:
				throw new AssertionError( indexType.name() );
		}
	}

	/**
	 * @param offset the offset to start at
	 * @param limit the maximum number of results
	 * @return the SQL-clause to limit the amount of retrieved results
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String getLimitClause(@Nonnegative final int offset, @Signed final int limit)
	{
		return (offset > 0 ? "OFFSET " + offset + " " : "") + (limit > 0 ? "FETCH FIRST " + limit + " ROWS ONLY" : "");
	}

	/**
	 * @param sqlFunction the SQL-function to apply
	 * @param column the column to aggregate
	 * @return the SQL function for the given column
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String getSQLFunction(@Nonnull final String sqlFunction, @Nonnull final String column)
	{
		return sqlFunction.replaceAll( "%column%", column);
	}
	
	/**
	 * @param joinType
	 * @return the correct keyword for the JOIN-type for this SQL-dialect
	 * @since 0.7
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String getSQLJoinKeyword(@Nonnull final JoinType joinType)
	{
		switch(joinType)
		{
			case INNER_JOIN:
				return JDBCDriver.JOIN_INNER;
			case LEFT_OUTER_JOIN:
				return JDBCDriver.JOIN_LEFT_OUTER;
			case RIGHT_OUTER_JOIN:
				return JDBCDriver.JOIN_RIGHT_OUTER;
			case FULL_OUTER_JOIN:
				return JDBCDriver.JOIN_FULL_OUTER;
			default:
				throw new AssertionError( joinType.name() );
		}
	}

	/**
	 * For the default-implementation, see: https://en.wikibooks.org/wiki/SQL_Dialects_Reference/Data_structure_definition/Auto-increment_column
	 *
	 * @param primaryKeyKeywords the previously set keywords
	 * @return the keywords for an auto-incremental primary-key column
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String getPrimaryKeyKeywords(@Nonnull final String primaryKeyKeywords)
	{
		return primaryKeyKeywords + " GENERATED ALWAYS AS IDENTITY PRIMARY KEY";
	}

	/**
	 * The default implementation provides a 4kB string-column
	 *
	 * @return the default data-type for strings
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String getStringDataType()
	{
		return "VARCHAR("+JDBCDriver.STRING_TYPE_LENGTH+")";
	}

	/**
	 * The default implementation inserts a <code>NULL</code>-value for the <code>primaryColumn</code>
	 *
	 * @param primaryColumn the primaryColumn
	 * @return the columns-and-values string for an empty row
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String getInsertDataForEmptyRow(@Nonnull final String primaryColumn)
	{
		return "(" + primaryColumn + ") VALUES (NULL)";
	}

	/**
	 * By default, only the ID-column is returned from the INSERT-statement, so we just return the first value as int
	 *
	 * @param resultSet the ResultSet of the INSERT-statement
	 * @param primaryColumn the primary-column to extract
	 * @return the ID of the newly created row
	 * @throws java.sql.SQLException if a SQL error occurs
	 */
	public int getCreatedRowID(@Nonnull final ResultSet resultSet, @Nonnull final String primaryColumn) throws SQLException
	{
		//the name of the returned column varies from vendor to vendor, so just return the first column
		return resultSet.getInt( 1);
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
	@Syntax(value = "SQL")
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
		//map some common types which could be supported natively by the DBMS
		if ( java.util.UUID.class.equals( javaType))
		{
			return getSQLType( String.class );
		}
		if ( java.net.URL.class.equals( javaType) || java.net.URI.class.equals( javaType) || java.nio.file.Path.class.equals( javaType))
		{
			return getSQLType( String.class);
		}
		if( java.io.Serializable.class.isAssignableFrom( javaType ))
		{
			//any Serializable object can be mapped from and to BLOBs
			return "BLOB";
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
		final String sqlTypeUpper = sqlType.toUpperCase();
		//map SQL-types
		if(sqlTypeUpper.startsWith( "ARRAY"))
		{
			return java.sql.Array.class;
		}
		if(sqlTypeUpper.startsWith( "BLOB"))
		{
			return java.sql.Blob.class;
		}
		if(sqlTypeUpper.startsWith( "CLOB"))
		{
			return java.sql.Clob.class;
		}
		if(sqlTypeUpper.startsWith( "NCLOB"))
		{
			return java.sql.NClob.class;
		}
		if(sqlTypeUpper.startsWith( "REF"))
		{
			return java.sql.Ref.class;
		}
		if(sqlTypeUpper.startsWith( "ROWID"))
		{
			return java.sql.RowId.class;
		}
		if(sqlTypeUpper.startsWith( "XML"))
		{
			return java.sql.SQLXML.class;
		}
		if ( sqlTypeUpper.startsWith( "DATE" ) )
		{
			return Date.class;
		}
		if ( sqlTypeUpper.startsWith( "TIMESTAMP" ) )
		{
			return Timestamp.class;
		}
		if ( sqlTypeUpper.startsWith( "TIME" ) )
		{
			return Time.class;
		}
		//map built-in types
		if ( sqlTypeUpper.startsWith( "VARCHAR" ) || sqlTypeUpper.startsWith( "CHAR" ) )
		{
			return String.class;
		}
		if ( sqlTypeUpper.startsWith( "NUMERIC" ) )
		{
			return BigDecimal.class;
		}
		if ( sqlTypeUpper.startsWith( "BIT" ) )
		{
			return Boolean.class;
		}
		if ( sqlTypeUpper.startsWith( "TINYINT" ) )
		{
			return Byte.class;
		}
		if ( sqlTypeUpper.startsWith( "SHORTINT" ) )
		{
			return Short.class;
		}
		//MySQL has data-type INT
		if ( sqlTypeUpper.startsWith( "INT" ) )
		{
			return Integer.class;
		}
		if ( sqlTypeUpper.startsWith( "BIGINT" ) )
		{
			return Long.class;
		}
		if ( sqlTypeUpper.startsWith( "REAL" ) )
		{
			return Float.class;
		}
		if ( sqlTypeUpper.startsWith( "DOUBLE" ) )
		{
			return Double.class;
		}
		throw new IllegalArgumentException( "Type not mapped: " + sqlTypeUpper );
	}

	/**
	 * @param con
	 * @param term
	 * @return whether the term is a reserved keyword, either in SQL92 or vendor-specific
	 * @throws SQLException
	 */
	public boolean isReservedKeyword(@Nonnull final Connection con, @Nonnull final String term) throws SQLException
	{
		if(Arrays.stream( JDBCDriver.sql92Keywords ).anyMatch( (final String s) -> s.equalsIgnoreCase( term)))
		{
			return true;
		}
		final String[] keyWords = con.getMetaData().getSQLKeywords().split( "\\s*,\\s*");
		return Arrays.stream( keyWords ).anyMatch( (final String s) -> s.equalsIgnoreCase( term));
	}

	@Override
	public Migration createMigration(final Class<? extends ActiveRecord> recordType, final RecordStore store)
	{
		if(!(store instanceof JDBCRecordStore))
			throw new IllegalArgumentException("RecordStore must beof type JDBCRecordStore!");
		return new AutomaticMigration(recordType, ((JDBCRecordStore)store).getConnection());
	}

	@Override
	public Migration createMigration( String storeName, Map<String, Class<?>> columns,  final RecordStore store )
	{
		return createMigration( storeName, columns, null, store );
	}

	@Override
	public Migration createMigration( String storeName, Map<String, Class<?>> columns, Map<Set<String>, IndexType> indices,  
			final RecordStore store ) throws UnsupportedOperationException
	{
		String applyCommand = "CREATE TABLE " + storeName + " ("  + 
				columns.entrySet().stream().map( (Map.Entry<String, Class<?>> column ) -> 
				{
					return column.getKey() + " " + getSQLType( column.getValue());
				}).collect( Collectors.joining( ",\n"))+ ");";
		if(indices != null)
		{
			applyCommand += indices.entrySet().stream().map( (Map.Entry<Set<String>, IndexType> index) -> 
			{
				return "CREATE INDEX ON " + storeName + " (" + index.getKey().stream().collect( Collectors.joining(", ")) + ")";
			}).collect( Collectors.joining(";\n"));
		}
		final String revertCommand = "DROP TABLE " + storeName;
		//does not support update, too complicated
		return createMigration( applyCommand, null, revertCommand, store );
	}

	@Override
	public Migration createMigration( String applyCommand, String updateCommand, String revertCommand,  final RecordStore store )
	{
		if(!(store instanceof JDBCRecordStore))
			throw new IllegalArgumentException("RecordStore must beof type JDBCRecordStore!");
		return new ManualMigration(applyCommand, updateCommand, revertCommand, ((JDBCRecordStore)store).getConnection());
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
				final String productName = con.getMetaData().getDatabaseProductName();
				if(productName.contains( "HSQL"))
				{
					return HSQLDBDriver.INSTANCE;
				}
				if(productName.contains( "MySQL"))
				{
					return MySQLDriver.INSTANCE;
				}
				if(productName.contains( "SQLite"))
				{
					return SQLiteDriver.INSTANCE;
				}
				if(productName.contains( "PostgreSQL"))
				{
					return PostgreSQLDriver.INSTANCE;
				}
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
				return quoteFunc.apply( JDBCDriver.convertIdentifierWithoutQuote(input, con ));
			}
			if(meta.storesLowerCaseIdentifiers())
			{
				return quoteFunc.apply( JDBCDriver.convertIdentifierWithoutQuote( input, con ));
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
			final String numString = currentIdentifier.substring( "associatedTable".length() );
			if(numString.isEmpty())
			{
				return "associatedTable1";
			}
			try
			{
				final Integer num = Integer.parseInt( numString);
				return "associatedTable"+num;
			}
			catch(final NumberFormatException nfe)
			{
				return "otherAssociatedTable";
			}
		}
		if(currentIdentifier.startsWith( "joinedTable"))
		{
			final String numString = currentIdentifier.substring( "joinedTable".length() );
			if(numString.isEmpty())
			{
				return "joinedTable1";
			}
			try
			{
				final Integer num = Integer.parseInt( numString);
				return "joinedTable"+num;
			}
			catch(final NumberFormatException nfe)
			{
				return "otherJoinedTable";
			}
		}
		if("otherTable".equals( currentIdentifier))
		{
			return "associatedTable";
		}
		return "otherTable";
	}
}
