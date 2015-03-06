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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.migration.ManualMigration;
import de.doe300.activerecord.migration.Migration;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.scope.Scope;
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

/**
 * Simple non-caching implementation of the RecordStore backed by a JDBC-Connection.
 * @author doe300
 */
public class SimpleJDBCRecordStore implements RecordStore
{
	protected final Connection con;

	/**
	 * @param con 
	 */
	public SimpleJDBCRecordStore(Connection con)
	{
		this.con=con;
	}
	
	protected String toWhereClause(Condition condition)
	{
		if(condition==null)
		{
			return "";
		}
		String clause =" WHERE ";
		clause += condition.toSQL();
		return clause;
	}
	
	protected Order toOrder(RecordBase<?> base, Scope scope)
	{
		if(scope.getOrder()!=null)
		{
			return scope.getOrder();
		}
		return base.getDefaultOrder();
	}
	
	protected String toColumnsList(String[] columns, String primaryColumn)
	{
		if(columns==null||columns.length==0)
		{
			return primaryColumn;
		}
		if(Arrays.stream( columns ).anyMatch( (String col)-> col.equalsIgnoreCase(primaryColumn)))
		{
			return Arrays.stream( columns ).collect( Collectors.joining(", "));
		}
		return primaryColumn+", "+Arrays.stream( columns ).collect( Collectors.joining(", "));
	}
	
	protected void fillStatement(PreparedStatement stm, Condition condition) throws SQLException
	{
		if(condition.hasWildcards())
		{
			Object[] values = condition.getValues();
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
	protected String convertIdentifier(String input)
	{
		try
		{
			if(con.getMetaData().storesUpperCaseIdentifiers())
			{
				return input.toUpperCase();
			}
			if(con.getMetaData().storesLowerCaseIdentifiers())
			{
				return input.toLowerCase();
			}
			
		}
		catch ( SQLException ex )
		{
			//TODO do something
		}
		return input;
	}

	@Override
	public Connection getConnection()
	{
		return con;
	}
	
	@Override
	public void setValue(RecordBase<?> base, int primaryKey, String name, Object value ) throws IllegalArgumentException
	{
		setValues( base, primaryKey, Collections.singletonMap( name, value));
	}
	
	@Override
	public void setValues(RecordBase<?> base, int primaryKey, String[] names, Object[] values ) throws IllegalArgumentException
	{
		Map<String,Object> map = new HashMap<>(names.length);
		for(int i = 0;i<names.length;i++)
		{
			map.put( names[i], values[i]);
		}
		setValues( base, primaryKey, map );
	}
	
	@Override
	public void setValues(RecordBase<?> base, int primaryKey, Map<String,Object> data) throws IllegalArgumentException
	{
		//1. get updated columns
		Map<String,Object> tmp = new HashMap<>(data.size());
		//convert all column names to correct case
		data.forEach( (String s,Object obj) -> tmp.put( convertIdentifier( s), obj));
		//add timestamp if not present
		if(base.isTimestamped() && !data.containsKey( COLUMN_UPDATED_AT))
		{
			tmp.put( COLUMN_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
		}
		//Don't update ID
		tmp.remove( base.getPrimaryColumn());
		Iterator<Map.Entry<String,Object>> entries = tmp.entrySet().iterator();
		String[] columns = new String[tmp.size()];
		Object[] values = new Object[tmp.size()];
		for(int i=0;i<columns.length && entries.hasNext();i++)
		{
			Map.Entry<String,Object> e = entries.next();
			columns[i] = e.getKey();
			values[i] = e.getValue();
		}
		//2. create statement
		String sql = "UPDATE "+base.getTableName()+" SET ";
		sql+= Arrays.stream( columns).map( (String s)-> s+ " = ? ").collect( Collectors.joining(", "));
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
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to set values!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Object getValue(RecordBase<?> base, int primaryKey, String name ) throws IllegalArgumentException
	{
		try(Statement stm = con.createStatement())
		{
			String sql = "SELECT "+name+" FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
			Logging.getLogger().debug( "JDBCStore", sql);
			ResultSet res = stm.executeQuery( sql );
			if(res.next())
			{
				return res.getObject( name );
			}
			Logging.getLogger().debug( "JDBCStore", "no value found");
			return null;
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to get value!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}
	
	@Override
	public Map<String, Object> getValues( RecordBase<?> base, int primaryKey, String[] columns ) throws IllegalArgumentException
	{
		try(Statement stm = con.createStatement())
		{
			String sql = "SELECT "+toColumnsList(columns, base.getPrimaryColumn() )+" FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
			Logging.getLogger().debug( "JDBCStore", sql);
			ResultSet res = stm.executeQuery(sql);
			if(res.next())
			{
				Map<String,Object> values=new HashMap<>(columns.length);
				for(String column:columns)
				{
					values.put( column, res.getObject( column));
				}
				return values;
			}
			Logging.getLogger().debug( "JDBCStore", "no values found");
			return Collections.emptyMap();
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to get values!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}
	
	@Override
	public boolean isSynchronized( RecordBase<?> base, int primaryKey)
	{
		return true;
	}

	@Override
	public boolean containsRecord( RecordBase<?> base, Integer primaryKey )
	{
		String sql = "SELECT "+base.getPrimaryColumn()+" FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stmt = con.prepareStatement( sql ))
		{
			return stmt.executeQuery().next();
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to get row!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public void destroy( RecordBase<?> base, int primaryKey )
	{
		try(Statement stm = con.createStatement())
		{
			String sql = "DELETE FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey;
			Logging.getLogger().debug( "JDBCStore", sql);
			stm.executeUpdate( sql);
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to destroy row!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Map<String, Object> findFirstWithData( RecordBase<?> base, String[] columns, Scope scope )
	{
		String sql = "SELECT "+toColumnsList( columns, base.getPrimaryColumn() )+" FROM "+base.getTableName()+" "+toWhereClause( scope.getCondition() )+" ORDER BY "+toOrder( base, scope ).toSQL()+" LIMIT 1";
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement(sql))
		{
			if(scope.getCondition()!=null)
			{
				fillStatement( stm, scope.getCondition() );
			}
			ResultSet res = stm.executeQuery();
			if(res.next())
			{
				Map<String,Object> values=new HashMap<>(columns.length);
				for(String column:columns)
				{
					values.put( column, res.getObject( column));
				}
				return values;
			}
			Logging.getLogger().debug( "JDBCStore", "no matching rows found");
			return Collections.emptyMap();
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to find matches!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public int count( RecordBase<?> base, Condition condition )
	{
		String sql = "SELECT COUNT(1) as size FROM "+base.getTableName()+" "+toWhereClause( condition );
		Logging.getLogger().debug( "JDBCStore", sql);
		//column name can't be count, because its a keyword
		try(PreparedStatement stm = con.prepareStatement(sql))
		{
			if(condition!=null)
			{
				fillStatement( stm, condition );
			}
			ResultSet res = stm.executeQuery();
			if(res.next())
			{
				return res.getInt( "size");
			}
			Logging.getLogger().debug( "JDBCStore", "no matching rows found");
			return -1;
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to count matches!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData( RecordBase<?> base, String[] columns, Scope scope )
	{
		String sql = "SELECT "+toColumnsList( columns, base.getPrimaryColumn() )+" FROM "+base.getTableName()+" "+toWhereClause( scope.getCondition() )+" ORDER BY "+toOrder( base, scope ).toSQL();
		if(scope.getLimit()!=Scope.NO_LIMIT)
		{
			sql += " LIMIT "+scope.getLimit();
		}
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement(sql))
		{
			if(scope.getCondition()!=null)
			{
				fillStatement( stm, scope.getCondition() );
			}
			final ResultSet res = stm.executeQuery();
			return StreamSupport.stream( new Spliterator<Map<String,Object>>()
			{

				@Override
				public boolean tryAdvance(Consumer<? super Map<String, Object>> action )
				{
					try
					{
						if(!res.next())
						{
							return false;
						}
						Map<String,Object> values=new HashMap<>(columns.length);
						for(String column:columns)
						{
							values.put( column, res.getObject( column));
						}
						action.accept( values);
						return true;
					}
					catch ( SQLException ex )
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
					return DISTINCT|IMMUTABLE|NONNULL|ORDERED;
				}
			},false);
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to find matches!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public boolean exists(String tableName)
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
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to find table!");
			Logging.getLogger().error( "JDBCStore", ex);
			return false;
		}
	}

	@Override
	public Set<String> getAllColumnNames( String tableName )
	{
		try(ResultSet set = con.getMetaData().getColumns(con.getCatalog(), con.getSchema(), convertIdentifier( tableName ), null))
		{
			Set<String> columns = new HashSet<>(10);
			while(set.next())
			{
				columns.add( set.getString( "COLUMN_NAME").toLowerCase());
			}
			return columns;
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to retrieve columns for table!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new RuntimeException("Failed to retrieve columns for table '"+tableName+"'",ex);
		}
	}

	@Override
	public int insertNewRecord(RecordBase<?> base)
	{
		try
		{
			if(base.isAutoCreate() && !exists( base.getTableName()))
			{
				Logging.getLogger().info( "JDBCStore", "auto creating table "+base.getTableName());
				String createSQL = base.getRecordType().getAnnotation( RecordType.class).autoCreateSQL();
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
						return -1;
					}
				}
				catch(Exception e)
				{
					Logging.getLogger().error( "JDBCStore", "Failed to create table");
					throw new RuntimeException("Failed to create table", e);
				}
			}
			PreparedStatement stmt;
			if(base.isTimestamped())
			{
				String sql = "INSERT INTO "+base.getTableName()+" ("+COLUMN_CREATED_AT+", "+COLUMN_UPDATED_AT+") VALUES (?, ?)";
				Logging.getLogger().debug( "JDBCStore", sql);
				stmt = con.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS );
				long timestamp = System.currentTimeMillis();
				stmt.setTimestamp( 1, new Timestamp(timestamp));
				stmt.setTimestamp( 2, new Timestamp(timestamp));
			}
			else
			{
				String sql = "INSERT INTO "+base.getTableName()+"( "+base.getPrimaryColumn()+") VALUES (NULL)";
				Logging.getLogger().debug( "JDBCStore", sql);
				stmt = con.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS);
			}
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if(rs.next())
			{
				return rs.getInt( base.getPrimaryColumn());
			}
			Logging.getLogger().error( "JDBCStore", "Failed to insert new row!");
			return -1;
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to insert new row!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new RuntimeException("Failed to insert new row",ex);
		}
	}

	@Override
	public void close() throws Exception
	{
		con.close();
	}

	@Override
	public Stream<Object> getValues( String tableName, String column, String condColumn, Object condValue ) throws
			IllegalArgumentException
	{
		String sql = "SELECT "+column+" FROM " +tableName+ " WHERE "+condColumn+" = ?";
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stmt = con.prepareStatement( sql))
		{
			stmt.setObject( 1, condValue);
			ResultSet res = stmt.executeQuery();
			return StreamSupport.stream( new Spliterator<Object>()
			{
				@Override
				public boolean tryAdvance(Consumer<? super Object> action )
				{
					try
					{
						if(!res.next())
						{
							return false;
						}
						action.accept( res.getObject( 1));
						return true;
					}
					catch ( SQLException ex )
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
					return DISTINCT|IMMUTABLE|NONNULL|ORDERED;
				}
			}, false);
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to retrieve values!");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public boolean isCached()
	{
		return false;
	}

	@Override
	public boolean save(RecordBase<?> base, int primaryKey )
	{
		return false;
	}

	@Override
	public boolean saveAll(RecordBase<?> base )
	{
		return false;
	}

	@Override
	public void clearCache(RecordBase<?> base, int primaryKey )
	{
	}

	@Override
	public boolean addRow( String tableName, String[] rows, Object[] values ) throws IllegalArgumentException
	{
		String sql = "INSERT INTO "+tableName+" ("+Arrays.stream( rows).collect( Collectors.joining(", "))+") VALUES ("+
				Arrays.stream( values ).map( (Object obj) -> "?").collect( Collectors.joining(", "))+")";
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement( sql ))
		{
			for(int i = 0;i<values.length;i++)
			{
				stm.setObject( i+1, values[i]);
			}
			return stm.executeUpdate() == 1;
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to add row");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new RuntimeException("Failed to insert new row",ex);
		}
	}

	@Override
	public boolean removeRow( String tableName, Condition cond ) throws IllegalArgumentException
	{
		String sql = "DELETE FROM "+tableName+toWhereClause( cond );
		Logging.getLogger().debug( "JDBCStore", sql);
		try(PreparedStatement stm = con.prepareStatement( sql ))
		{
			fillStatement( stm, cond );
			return stm.executeUpdate() >= 1;
		}
		catch ( SQLException ex )
		{
			Logging.getLogger().error( "JDBCStore", "Failed to remove row");
			Logging.getLogger().error( "JDBCStore", ex);
			throw new RuntimeException("Failed to insert new row",ex);
		}
	}
}
