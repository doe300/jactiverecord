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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.jdbc.VendorSpecific;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.migration.ManualMigration;
import de.doe300.activerecord.migration.Migration;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.RecordStore;

/**
 * Simple non-caching implementation of the RecordStore backed by a JDBC-Connection.
 * @author doe300
 */
public class SimpleJDBCRecordStore implements RecordStore
{
	protected final Connection con;
	protected final VendorSpecific vendorSpecifics;

	/**
	 * @param con
	 */
	public SimpleJDBCRecordStore(final Connection con)
	{
		this.con=con;
		this.vendorSpecifics = VendorSpecific.guessDatabaseVendor( con );
	}
	
	public SimpleJDBCRecordStore( Connection con, VendorSpecific vendorSpecifics )
	{
		this.con = con;
		this.vendorSpecifics = vendorSpecifics;
	}
	
	protected String toWhereClause(final Condition condition)
	{
		if(condition==null)
		{
			return "";
		}
		String clause =" WHERE ";
		clause += condition.toSQL(vendorSpecifics);
		return clause;
	}

	protected Order toOrder(final RecordBase<?> base, final Scope scope)
	{
		if(scope.getOrder()!=null)
		{
			return scope.getOrder();
		}
		return base.getDefaultOrder();
	}

	protected String toColumnsList(final String[] columns, final String primaryColumn)
	{
		if(columns==null||columns.length==0)
		{
			return primaryColumn;
		}
		if(Arrays.stream( columns ).anyMatch( (final String col)-> col.equalsIgnoreCase(primaryColumn)))
		{
			return Arrays.stream( columns ).collect( Collectors.joining(", "));
		}
		return primaryColumn+", "+Arrays.stream( columns ).collect( Collectors.joining(", "));
	}

	protected void fillStatement(final PreparedStatement stm, final Condition condition) throws SQLException
	{
		if(condition.hasWildcards())
		{
			final Object[] values = condition.getValues();
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
	protected String convertIdentifier(final String input)
	{
		return VendorSpecific.convertIdentifier( input, con );
	}
	
	protected void checkTableExists(RecordBase<?> base) throws IllegalStateException
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
					mig = new ManualMigration(createSQL, null, null);
				}
				else
				{
					mig = new AutomaticMigration(base.getRecordType(), false);
				}
				try
				{
					if(!mig.apply( con ))
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
				throw new IllegalStateException("Table doesn't exists: "+base.getTableName());
			}
		}
	}

	@Override
	public Connection getConnection()
	{
		return con;
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
		final Map<String,Object> tmp = new HashMap<>(data.size());
		//convert all column names to correct case
		data.forEach( (final String s,final Object obj) -> tmp.put( convertIdentifier( s), obj));
		//add timestamp if not present
		if(base.isTimestamped() && !data.containsKey( TimestampedRecord.COLUMN_UPDATED_AT))
		{
			tmp.put( TimestampedRecord.COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		//Don't update ID
		tmp.remove( base.getPrimaryColumn());
		if(tmp.isEmpty())
		{
			//cancel, if only ID was to be updated
			return;
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
		//2. create statement
		String sql = "UPDATE "+base.getTableName()+" SET ";
		sql+= Arrays.stream( columns).map( (final String s)-> s+ " = ? ").collect( Collectors.joining(", "));
		sql += " WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement( sql ))
		{
			for(int i=0;i<values.length;i++)
			{
				stm.setObject( i+1, values[i]);
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
			"SELECT " + toColumnsList(columns, base.getPrimaryColumn()) + " FROM " + base.getTableName() + " WHERE "
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
	public boolean isSynchronized( final RecordBase<?> base, final int primaryKey)
	{
		return true;
	}

	@Override
	public boolean containsRecord( final RecordBase<?> base, final Integer primaryKey )
	{
		checkTableExists( base );
		final String sql = "SELECT "+base.getPrimaryColumn()+" FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stmt = con.prepareStatement( sql ))
		{
			return stmt.executeQuery().next();
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
		try(Statement stm = con.createStatement())
		{
			Logging.getLogger().debug( "JDBCStore", sql);
			stm.executeUpdate( sql);
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
		final String sql = "SELECT "+toColumnsList( columns, base.getPrimaryColumn() )+" FROM "+base.getTableName()+" "+toWhereClause( scope.getCondition() )+" ORDER BY "+toOrder( base, scope ).toSQL(vendorSpecifics)+" LIMIT 1";
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement(sql))
		{
			if(scope.getCondition()!=null)
			{
				fillStatement( stm, scope.getCondition() );
			}
			try (final ResultSet res = stm.executeQuery())
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
		final String sql = "SELECT COUNT(1) as size FROM "+base.getTableName()+" "+toWhereClause( condition );
		Logging.getLogger().debug( "JDBCStore", sql);
		//column name can't be count, because its a keyword
		try(PreparedStatement stm = con.prepareStatement(sql))
		{
			if(condition!=null)
			{
				fillStatement( stm, condition );
			}
			try (final ResultSet res = stm.executeQuery())
			{
				if (res.next())
				{
					return res.getInt("size");
				}
				Logging.getLogger().debug("JDBCStore", "No matching rows found");
				return 0;
			}

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
	public Stream<Map<String, Object>> streamAllWithData( final RecordBase<?> base, final String[] columns, final Scope scope )
	{
		String sql = "SELECT "+toColumnsList( columns, base.getPrimaryColumn() )+" FROM "+base.getTableName()+" "+toWhereClause( scope.getCondition() )+" ORDER BY "+toOrder( base, scope ).toSQL(vendorSpecifics);
		if(scope.getLimit()!=Scope.NO_LIMIT)
		{
			sql += " LIMIT "+scope.getLimit();
		}
		Logging.getLogger().debug( "JDBCStore", sql);
		try
		{
			//this statement can't be try-with-resource because it would close the result-set
			PreparedStatement stm = con.prepareStatement(sql);
			if(scope.getCondition()!=null)
			{
				fillStatement( stm, scope.getCondition() );
			}
			//this result-set can't be try-with-resource because it is required to stay open for asynchronous call
			final ResultSet res = stm.executeQuery();
			return allWithDataStream(columns, res);
		}
		catch ( final SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to find matches!");
			Logging.getLogger().error( "JDBCStore", sql);
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	private Stream<Map<String, Object>> allWithDataStream(final String[] columns, final ResultSet res)
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
					final Map<String,Object> values=new HashMap<>(columns.length);
					for(final String column:columns)
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
		},false);
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
	public Set<String> getAllColumnNames( final String tableName )
	{
		try(ResultSet set = con.getMetaData().getColumns(con.getCatalog(), con.getSchema(), VendorSpecific.convertIdentifierWithoutQuote(tableName, con), null))
		{
			final Set<String> columns = new HashSet<>(10);
			while(set.next())
			{
				columns.add( set.getString( "COLUMN_NAME").toLowerCase());
			}
			return columns;
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
			rowData.put( base.getPrimaryColumn(), null);
			//add timestamp
			if(base.isTimestamped())
			{
				final long timestamp = System.currentTimeMillis();
				rowData.putIfAbsent(TimestampedRecord.COLUMN_CREATED_AT, new Timestamp(timestamp ));
				rowData.putIfAbsent(TimestampedRecord.COLUMN_UPDATED_AT, new Timestamp(timestamp ));
			}

			final String sql = "INSERT INTO "+base.getTableName()+
					" ("+rowData.entrySet().stream().map((final Map.Entry<String,Object> e) -> e.getKey()).map( this::convertIdentifier).collect( Collectors.joining( ", "))+
					") VALUES ("+rowData.entrySet().stream().map((final Map.Entry<String,Object> e) -> "?").collect( Collectors.joining( ", "))+")";
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
						//the name of the returned column varies from vendor to vendor, so just return the first column
						return rs.getInt(1);
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
	public Stream<Object> getValues( final String tableName, final String column, final String condColumn, final Object condValue ) throws
			IllegalArgumentException
	{
		final String sql = "SELECT "+column+" FROM " +tableName+ " WHERE "+condColumn+" = ?";
		Logging.getLogger().debug( "JDBCStore", sql);
		try
		{
			//can't use try-with-resource here, because result-set is required to stay open
			PreparedStatement stmt = con.prepareStatement( sql);
			stmt.setObject( 1, condValue);
			final ResultSet res = stmt.executeQuery();
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

	private Stream<Object> valuesStream(final ResultSet res)
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
				return Spliterator.DISTINCT|Spliterator.IMMUTABLE|Spliterator.NONNULL|Spliterator.ORDERED;
			}
		}, false);
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
	public boolean addRow( final String tableName, final String[] rows, final Object[] values ) throws IllegalArgumentException
	{
		if(!exists( tableName ))
		{
			throw new IllegalArgumentException("Table doesn't exists: "+tableName);
		}
		final String sql = "INSERT INTO "+tableName+" ("+Arrays.stream( rows).collect( Collectors.joining(", "))+") VALUES ("+
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
	public boolean removeRow( final String tableName, final Condition cond ) throws IllegalArgumentException
	{
		final String sql = "DELETE FROM "+tableName+toWhereClause( cond );
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement( sql ))
		{
			fillStatement( stm, cond );
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
}
