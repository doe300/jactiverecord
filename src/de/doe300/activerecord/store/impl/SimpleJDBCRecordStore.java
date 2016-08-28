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
package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.util.Pair;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AggregateFunction;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.migration.ManualMigration;
import de.doe300.activerecord.migration.Migration;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.JDBCRecordStore;
import de.doe300.activerecord.store.NoSuchDataSetException;
import de.doe300.activerecord.store.diagnostics.Diagnostics;
import java.util.Optional;

/**
 * Simple non-caching implementation of the RecordStore backed by a JDBC-Connection.
 * @author doe300
 */
public class SimpleJDBCRecordStore implements JDBCRecordStore
{
	@Nonnull
	protected final Connection con;
	@Nonnull
	protected final JDBCDriver driver;
	@Nonnull
	protected final Diagnostics<String> diagnostics;

	/**
	 * @param con
	 */
	public SimpleJDBCRecordStore(@Nonnull final Connection con)
	{
		this.con=con;
		this.driver = JDBCDriver.guessDriver( con );
		this.diagnostics = driver.createDiagnostics( this );
	}

	/**
	 * @param con
	 * @param driver
	 */
	public SimpleJDBCRecordStore(@Nonnull final Connection con, @Nonnull final JDBCDriver driver)
	{
		this.con = con;
		this.driver = driver;
		this.diagnostics = driver.createDiagnostics( this );
	}

	/**
	 * Converts the given Condition to a WHERE clause
	 * @param condition the condition to convert
	 * @param tableName the table-name
	 * @return the resulting WHERE-clause
	 */
	@Nonnull
	protected String toWhereClause(@Nullable final Condition condition, @Nullable String tableName)
	{
		if(condition==null)
		{
			return "";
		}
		String clause =" WHERE ";
		clause += condition.toSQL(driver, tableName);
		return clause;
	}

	@Nonnull
	protected Order toOrder(@Nonnull final RecordBase<?> base, @Nonnull final Scope scope)
	{
		return Optional.ofNullable( scope.getOrder()).orElse( base.getDefaultOrder());
	}

	/**
	 * Converts the given String[] to a comma-separated list of columns to fetch
	 * @param columns the columns to convert
	 * @param primaryColumn the primary column
	 * @param tableName the table-name
	 * @return the comma-separated list
	 */
	@Nonnull
	protected String toColumnsList(@Nonnull final String[] columns, @Nonnull final String primaryColumn, @Nullable final String tableName)
	{
		String tableID = tableName != null ? tableName + "." : "";
		if(columns.length == 0 || (columns.length == 1 && columns[0].equalsIgnoreCase(primaryColumn)))
		{
			return tableID + primaryColumn;
		}
		return tableID + primaryColumn + ", " + Arrays.stream( columns)
				.filter((final String col)-> !col.equalsIgnoreCase(primaryColumn))
				.map( (String col) -> tableID + col).collect( Collectors.joining(", "));
	}
	
	/**
	 * Helper method to support large queries not supported by the underlying JDBC driver
	 * @param query the query to check and potentially convert
	 * @param cond the condition to extract the wildcards from
	 * @return the resulting ResultSet
	 * @throws SQLException
	 */
	@Nonnull
	@WillNotClose
	protected ResultSet queryStatement(@Nonnull final String query, @Nullable final Condition cond) throws SQLException
	{
		if(cond != null && cond.hasWildcards())
		{
			if(cond.getValues().length > driver.getParametersLimit())
			{
				final String preparedQuery = StatementUtil.prepareQuery(driver, query, cond );
				return diagnostics.profileQuery( () -> con.createStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY).executeQuery( preparedQuery ), () -> preparedQuery).get();
			}
		}
		final PreparedStatement stm = con.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if(cond != null)
		{
			fillStatement( stm, cond );
		}
		return diagnostics.profileQuery(() -> stm.executeQuery(), () -> StatementUtil.prepareQuery(driver, query, cond )).get();
	}
	
	/**
	 * Fills a statement with the wildcards extracted from the given Condition
	 * @param stm the Statement to fill
	 * @param condition the Condition holding the values
	 * @throws SQLException
	 */
	protected void fillStatement(@Nonnull final PreparedStatement stm, @Nonnull final Condition condition) throws SQLException
	{
		if(condition.hasWildcards())
		{
			final Object[] values = condition.getValues();
			if (values == null)
			{
				throw new IllegalArgumentException("Wildcard-Condition without any Arguments");
			}
			if(values.length > driver.getParametersLimit())
			{
				throw new IllegalArgumentException("Too many parameters in statement!");
			}
			for(int i=0;i<values.length;i++)
			{
				stm.setObject( i+1, values[i]);
			}
		}
	}

	/**
	 * Converts the input identifier to the case used in the DB
	 * @param input
	 * @return the input in the correct case
	 */
	@Nonnull
	protected String convertIdentifier(@Nonnull final String input)
	{
		return JDBCDriver.convertIdentifier( input, con );
	}

	/**
	 * Checks whether the table for this RecordBase exists.
	 *
	 * If the base is {@link RecordBase#isAutoCreate() auto-create}, the table is created, if necessary
	 *
	 * @param base
	 * @throws IllegalArgumentException
	 */
	protected void checkTableExists(@Nonnull final RecordBase<?> base) throws IllegalArgumentException
	{
		if(!exists( base.getTableName()))
		{
			if(base.isAutoCreate())
			{
				Logging.getLogger().info( "JDBCStore", "auto creating table "+base.getTableName());
				final String createSQL = base.getRecordType().getAnnotation( RecordType.class).autoCreateSQL();
				Migration mig;
				if(!createSQL.isEmpty())
				{
					mig = new ManualMigration(createSQL, null, null, con);
				}
				else
				{
					mig = new AutomaticMigration(base.getRecordType(), base.getTableName(), this, driver);
				}
				try
				{
					if(!mig.apply( ))
					{
						throw new RuntimeException("Migration failed");
					}
				}
				catch(final Exception e)
				{
					Logging.getLogger().error( "JDBCStore", "Failed to create table");
					Logging.getLogger().error( "JDBCStore", createSQL);
					throw new RuntimeException("Failed to create table", e);
				}
			}
			else
			{
				throw new NoSuchDataSetException(base.getTableName());
			}
		}
	}

	/**
	 * Checks and converts the given values and returns a pair of arrays for the columns and their values to be set
	 * @param base
	 * @param data
	 * @return the resulting columns and values
	 * @since 0.7
	 */
	@Nonnull
	protected Pair<String[], Object[]> prepareWriteValues(@Nonnull final RecordBase<?> base, @Nonnull final Map<String,Object> data)
	{
		final Map<String,Object> tmp = new HashMap<>(data.size());
		//convert all column names to correct case
		data.forEach( (final String s,final Object obj) -> tmp.put( convertIdentifier( s), obj));
		//add timestamp if not present
		if(base.isTimestamped() && !data.containsKey( TimestampedRecord.COLUMN_UPDATED_AT))
		{
			tmp.put( TimestampedRecord.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		//Don't update ID
		tmp.remove( convertIdentifier( base.getPrimaryColumn()));
		if(tmp.isEmpty())
		{
			//cancel, if only ID was to be updated
			return Pair.EMPTY;
		}
		final Iterator<Map.Entry<String,Object>> entries = tmp.entrySet().iterator();
		final String[] columns = new String[tmp.size()];
		final Object[] values = new Object[tmp.size()];
		for(int i=0;i<columns.length && entries.hasNext();i++)
		{
			final Map.Entry<String,Object> e = entries.next();
			columns[i] = e.getKey();
			values[i] = e.getValue();
		}
		return Pair.createPair( columns, values);
	}
	
	@Override
	public Connection getConnection()
	{
		return con;
	}

	@Override
	public JDBCDriver getDriver()
	{
		return driver;
	}

	@Override
	public Diagnostics<String> getDiagnostics()
	{
		return diagnostics;
	}

	@Override
	public void setValue(final RecordBase<?> base, final int primaryKey, final String name, final Object value ) throws IllegalArgumentException
	{
		setValues( base, primaryKey, Collections.singletonMap( name, value));
	}

	@Override
	public void setValues(final RecordBase<?> base, final int primaryKey, final String[] names, final Object[] values ) throws IllegalArgumentException
	{
		final Map<String,Object> map = new HashMap<>(names.length);
		for(int i = 0;i<names.length;i++)
		{
			map.put( names[i], values[i]);
		}
		setValues( base, primaryKey, map );
	}

	@Override
	public void setValues(final RecordBase<?> base, final int primaryKey, final Map<String,Object> data) throws IllegalArgumentException
	{
		//1. get updated columns
		final Pair<String[], Object[]> values = prepareWriteValues( base, data );
		if(!values.hasFirst())
		{
			return;
		}
		//2. create statement
		String sql = "UPDATE "+base.getTableName()+" SET ";
		sql+= Arrays.stream( values.getFirstOrThrow()).map( (final String s)-> s+ " = ? ").collect( Collectors.joining(", "));
		sql += " WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement( sql ))
		{
			for(int i=0;i<values.getSecondOrThrow().length;i++)
			{
				stm.setObject( i+1, values.getSecondOrThrow()[i]);
			}
			//3. execute
			stm.execute();
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to set values!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Object getValue(final RecordBase<?> base, final int primaryKey, final String name ) throws IllegalArgumentException
	{
		final String sql =
			"SELECT " + name + " FROM " + base.getTableName() + " WHERE " + base.getPrimaryColumn() + " = "
				+ primaryKey;
		Logging.getLogger().debug("JDBCStore", sql);
		try (Statement stm = con.createStatement(); ResultSet res = stm.executeQuery(sql))
		{
			if(res.next())
			{
				return res.getObject( name );
			}
			Logging.getLogger().debug( "JDBCStore", "No value found");
			return null;
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to get value!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Map<String, Object> getValues( final RecordBase<?> base, final int primaryKey, final String[] columns ) throws IllegalArgumentException
	{
		final String sql =
			"SELECT " + toColumnsList(columns, base.getPrimaryColumn(), null) + " FROM " + base.getTableName() + " WHERE "
				+ base.getPrimaryColumn() + " = " + primaryKey;
		Logging.getLogger().debug("JDBCStore", sql);
		try (Statement stm = con.createStatement(); final ResultSet res = stm.executeQuery(sql))
		{
			if(res.next())
			{
				final Map<String,Object> values=new HashMap<>(columns.length);
				for(final String column:columns)
				{
					values.put( column, res.getObject( column));
				}
				return values;
			}
			Logging.getLogger().debug( "JDBCStore", "no values found");
			return Collections.emptyMap();
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to get values!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Map<String, Object> getAllValues(final RecordBase<?> base, int primaryKey ) throws NoSuchDataSetException
	{
		final String sql = "SELECT * FROM " + base.getTableName() + " WHERE " + base.getPrimaryColumn() + " = " + primaryKey;
		Logging.getLogger().debug("JDBCStore", sql);
		try (Statement stm = con.createStatement(); final ResultSet res = stm.executeQuery(sql))
		{
			if(res.next())
			{
				final int columnCount = res.getMetaData().getColumnCount();
				final Map<String,Object> values=new HashMap<>(columnCount);
				for(int i = 1; i <= columnCount; ++i)
				{
					values.put( res.getMetaData().getColumnName( i), res.getObject( i ));
				}
				return values;
			}
			Logging.getLogger().debug( "JDBCStore", "no values found");
			return Collections.emptyMap();
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to get values!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}
	
	@Override
	public boolean isSynchronized( final RecordBase<?> base, final int primaryKey)
	{
		return true;
	}

	@Override
	public boolean containsRecord( final RecordBase<?> base, final int primaryKey )
	{
		checkTableExists( base );
		final String sql = "SELECT "+base.getPrimaryColumn()+" FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
		Logging.getLogger().debug( "JDBCStore", sql);
		try(final PreparedStatement stmt = con.prepareStatement( sql ); 
				final ResultSet result = stmt.executeQuery())
		{
			return result.next();
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to get row!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public void destroy( final RecordBase<?> base, final int primaryKey )
	{
		checkTableExists( base );
		final String sql = "DELETE FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
		try(final PreparedStatement stm = con.prepareStatement( sql ))
		{
			Logging.getLogger().debug( "JDBCStore", sql);
			stm.executeUpdate();
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to destroy row!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Map<String, Object> findFirstWithData( final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		checkTableExists( base );
		String tableID = JDBCDriver.getNextTableIdentifier( null );
		final String sql = "SELECT "+toColumnsList( columns, base.getPrimaryColumn(), tableID )
				+" FROM "+base.getTableName()+" AS " + tableID
				+toWhereClause( scope.getCondition(), tableID )
				+" ORDER BY "+toOrder( base, scope ).toSQL(driver, tableID)+" " + driver.getLimitClause( 0, 1);
		Logging.getLogger().debug( "JDBCStore", sql);
		try (final ResultSet res = queryStatement( sql, scope.getCondition()))
		{
			if (res.next())
			{
				final Map<String, Object> values = new HashMap<>(columns.length);
				for (final String column : columns)
				{
					values.put(column, res.getObject(column));
				}
				return values;
			}
			Logging.getLogger().debug("JDBCStore", "No matching rows found");
			return Collections.emptyMap();
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to find matches!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public int count( final RecordBase<?> base, final Condition condition )
	{
		checkTableExists( base );
		String tableID = JDBCDriver.getNextTableIdentifier( null );
		final String sql = "SELECT " + driver.getSQLFunction( JDBCDriver.AGGREGATE_COUNT_ALL, base.getPrimaryColumn())+ " as size FROM "+base.getTableName()+" AS "+tableID+toWhereClause( condition, tableID );
		Logging.getLogger().debug( "JDBCStore", sql);
		//column name can't be count, because its a keyword
		try (final ResultSet res = queryStatement( sql, condition ))
		{
			if (res.next())
			{
				return res.getInt("size");
			}
			Logging.getLogger().debug("JDBCStore", "No matching rows found");
			return 0;
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to count matches!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public <R> R aggregate(RecordBase<?> base, AggregateFunction<?, ?, ?, R> aggregateFunction, Condition condition )
	{
		checkTableExists( base );
		String tableID = JDBCDriver.getNextTableIdentifier( null );
		final String sql = "SELECT " + aggregateFunction.toSQL(driver, tableID) + " as result FROM "+base.getTableName()+" AS "+tableID+toWhereClause( condition, tableID );
		Logging.getLogger().debug( "JDBCStore", sql);
		try (final ResultSet res = queryStatement( sql, condition ))
		{
			if (res.next())
			{
				return (R)res.getObject("result");
			}
			Logging.getLogger().debug("JDBCStore", "No matching rows found");
			return null;
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to aggregate matches!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	@WillNotClose
	public Stream<Map<String, Object>> streamAllWithData( final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		String tableID = JDBCDriver.getNextTableIdentifier( null );
		String sql = "SELECT "+toColumnsList( columns, base.getPrimaryColumn(), tableID )
				+" FROM "+base.getTableName()+" AS "+tableID
				+toWhereClause( scope.getCondition(), tableID )
				+" ORDER BY "+toOrder( base, scope ).toSQL(driver, tableID)
				+" " + driver.getLimitClause( 0, scope.getLimit());
		Logging.getLogger().debug( "JDBCStore", sql);
		try
		{
			//this result-set can't be try-with-resource because it is required to stay open for asynchronous call
			final ResultSet res = queryStatement( sql, scope.getCondition());
			//we must add the primary-key to the list of columns, because it is not guaranteed to be in there
			//and it is needed to prevent row-maps with same values to be counted as the same
			Set<String> columnWithKey = new HashSet<>(Arrays.asList( columns ));
			columnWithKey.add( base.getPrimaryColumn());
			return allWithDataStream(columnWithKey, res);
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to find matches!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Nonnull
	private static Stream<Map<String, Object>> allWithDataStream(@Nonnull final Set<String> columns, @Nonnull final ResultSet res)
	{
		return StreamSupport.stream( new Spliterator<Map<String,Object>>()
		{

			@Override
			public boolean tryAdvance(final Consumer<? super Map<String, Object>> action )
			{
				try
				{
					if(!res.next())
					{
						//result-set is no longer needed, can be closed
						res.close();
						return false;
					}
					final Map<String,Object> values=new HashMap<>(columns.size());
					for(final String column : columns)
					{
						values.put( column, res.getObject( column));
					}
					action.accept( values);
					return true;
				}
				catch ( final SQLException ex )
				{
					return false;
				}
			}

			@Override
			public Spliterator<Map<String, Object>> trySplit()
			{
				return null;
			}

			@Override
			public long estimateSize()
			{
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics()
			{
				return Spliterator.DISTINCT|Spliterator.IMMUTABLE|Spliterator.NONNULL|Spliterator.ORDERED;
			}
		},false).onClose( () ->
		{
			try
			{
				res.close();
			}
			catch ( final SQLException ex )
			{
				throw new RuntimeException(ex);
			}
		});
	}

	@Override
	public boolean exists(final String tableName)
	{
		try(ResultSet set = con.getMetaData().getTables(con.getCatalog(), con.getSchema(), null, null ))
		{
			while(set.next())
			{
				if(set.getString( "TABLE_NAME").equalsIgnoreCase(tableName))
				{
					return true;
				}
			}
			return false;
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to find table!");
			Logging.getLogger().error( "JDBCStore", ex);
			return false;
		}
	}

	@Override
	public Map<String, Class<?>> getAllColumnTypes( String tableName ) throws IllegalArgumentException
	{
		if(!exists( tableName ))
		{
			throw new IllegalArgumentException("Table does not exist: " + tableName);
		}
		try(ResultSet set = con.getMetaData().getColumns(con.getCatalog(), con.getSchema(), JDBCDriver.convertIdentifierWithoutQuote(tableName, con), null))
		{
			Map<String, Class<?>> columnTypes = new HashMap<>(10);
			while(set.next())
			{
				columnTypes.put(set.getString( "COLUMN_NAME").toLowerCase(), driver.getJavaType( set.getString( "TYPE_NAME")));
			}
			return columnTypes;
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to retrieve columns for table!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new RuntimeException("Failed to retrieve columns for table '"+tableName+"'",ex);
		}
	}

	@Override
	public int insertNewRecord(final RecordBase<?> base, final Map<String,Object> columns)
	{
		try
		{
			checkTableExists( base );
			final Map<String,Object> rowData = new HashMap<>(columns != null ? columns.size() : 3 );
			//set initial values
			if(columns != null)
			{
				rowData.putAll( columns );
			}
			//make sure, primary key is not set
			rowData.remove( base.getPrimaryColumn() );
			//add timestamp
			if(base.isTimestamped())
			{
				final long timestamp = System.currentTimeMillis();
				rowData.putIfAbsent(TimestampedRecord.COLUMN_CREATED_AT, new Timestamp(timestamp ));
				rowData.putIfAbsent(TimestampedRecord.COLUMN_UPDATED_AT, new Timestamp(timestamp ));
			}

			final String sql;
			if(rowData.isEmpty())
			{
				sql = "INSERT INTO " + base.getTableName() + " " + driver.getInsertDataForEmptyRow(convertIdentifier( base.getPrimaryColumn()));
			}
			else
			{
				sql = "INSERT INTO "+base.getTableName()+
				" ("+rowData.entrySet().stream().map((final Map.Entry<String,Object> e) -> e.getKey()).map( this::convertIdentifier).collect( Collectors.joining( ", "))+
				") VALUES ("+rowData.entrySet().stream().map((final Map.Entry<String,Object> e) -> "?").collect( Collectors.joining( ", "))+")";
			}
			
			Logging.getLogger().debug( "JDBCStore", sql);
			try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
			{
				int i = 0;
				for (final Map.Entry<String, Object> e : rowData.entrySet())
				{
					stmt.setObject(i + 1, e.getValue());
					i++;
				}

				stmt.executeUpdate();
				try (final ResultSet rs = stmt.getGeneratedKeys())
				{
					if (rs.next())
					{
						return driver.getCreatedRowID( rs, base.getPrimaryColumn() );
					}
					Logging.getLogger().error("JDBCStore", "Failed to insert new row!");
					return -1;
				}
			}

		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to insert new row!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException("Failed to insert new row",ex);
		}
	}

	@Override
	public void close() throws Exception
	{
		con.close();
	}

	@Override
	@WillNotClose
	public Stream<Object> getValues( final String tableName, final String column, final String condColumn, final Object condValue ) throws
	IllegalArgumentException
	{
		//FIXME ResultSet is never closed if Stream is not read to the end!! (in all methods returning a Stream)
		final String sql = "SELECT "+column+" FROM " +tableName+ " WHERE "+condColumn+" = ?";
		Logging.getLogger().debug( "JDBCStore", sql);
		try
		{
			//can't use try-with-resource here, because result-set is required to stay open
			final ResultSet res = queryStatement( sql, Conditions.is( condColumn, condValue));
			return valuesStream(res);
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to retrieve values!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Nonnull
	@WillNotClose
	private static Stream<Object> valuesStream(@Nonnull final ResultSet res)
	{
		return StreamSupport.stream( new Spliterator<Object>()
		{
			@Override
			public boolean tryAdvance(final Consumer<? super Object> action )
			{
				try
				{
					if(!res.next())
					{
						//result-set is no longer needed, can be closed
						res.close();
						return false;
					}
					action.accept( res.getObject( 1));
					return true;
				}
				catch ( final SQLException ex )
				{
					return false;
				}
			}

			@Override
			public Spliterator<Object> trySplit()
			{
				return null;
			}

			@Override
			public long estimateSize()
			{
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics()
			{
				return Spliterator.IMMUTABLE|Spliterator.ORDERED;
			}
		}, false).onClose( () -> {
			try
			{
				res.close();
			}
			catch ( final SQLException ex )
			{
				throw new RuntimeException(ex);
			}
		});
	}

	@Override
	public boolean isCached()
	{
		return false;
	}

	@Override
	public boolean save(final RecordBase<?> base, final int primaryKey )
	{
		return false;
	}

	@Override
	public boolean saveAll(final RecordBase<?> base )
	{
		return false;
	}

	@Override
	public void clearCache(final RecordBase<?> base, final int primaryKey )
	{
		// no cache to clear
	}

	@Override
	public boolean loadIntoCache(RecordBase<?> base, int primaryKey )
	{
		//nothing to do
		return false;
	}

	@Override
	public boolean addRow( final String tableName, final String[] columns, final Object[] values ) throws IllegalArgumentException
	{
		if(!exists( tableName ))
		{
			throw new NoSuchDataSetException(tableName);
		}
		final String sql = "INSERT INTO "+tableName+" ("+Arrays.stream( columns).collect( Collectors.joining(", "))+") VALUES ("+
			Arrays.stream( values ).map( (final Object obj) -> "?").collect( Collectors.joining(", "))+")";
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement( sql ))
		{
			for(int i = 0;i<values.length;i++)
			{
				stm.setObject( i+1, values[i]);
			}
			return stm.executeUpdate() == 1;
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to add row");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException("Failed to insert new row",ex);
		}
	}

	@Override
	public boolean removeRow(@Nonnull final String tableName, @Nullable final Condition cond)
		throws IllegalArgumentException
	{
		final String sql = "DELETE FROM "+tableName + toWhereClause( cond, tableName );
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement( sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
		{
			if (cond != null)
			{
				fillStatement(stm, cond);
			}
			return stm.executeUpdate() >= 1;
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to remove row");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException("Failed to insert new row",ex);
		}
	}

	@Override
	public boolean dropTable( String tableName ) throws SQLException
	{
		final String sql = "DROP TABLE "+tableName;
		Logging.getLogger().info("JDBCStore", "Executing automatic table-drop...");
		Logging.getLogger().info("JDBCStore", sql);
		try(Statement stm = con.createStatement( ))
		{
			if(stm.executeUpdate(sql) < 0)
			{
				Logging.getLogger().error("JDBCStore", "Automatic table-drop failed!");
				return false;
			}
		}
		catch(final SQLException e)
		{
			Logging.getLogger().error( "JDBCStore", "Automatic table-drop failed with error!");
			Logging.getLogger().error( "JDBCStore", e);
			throw e;
		}
		return true;
	}
}
