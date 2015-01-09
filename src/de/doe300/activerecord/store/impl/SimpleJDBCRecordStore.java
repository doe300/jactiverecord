package de.doe300.activerecord.store.impl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.dsl.Condition;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author doe300
 */
public class SimpleJDBCRecordStore implements RecordStore
{
	protected final Connection con;

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
		//make all column names lower case
		data.forEach( (String s,Object obj) -> tmp.put( s.toLowerCase(), obj));
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
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Object getValue(RecordBase<?> base, int primaryKey, String name ) throws IllegalArgumentException
	{
		try(Statement stm = con.createStatement())
		{
			ResultSet res = stm.executeQuery( "SELECT "+name+" FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey);
			if(res.next())
			{
				return res.getObject( name );
			}
			return null;
		}
		catch ( SQLException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}
	
	@Override
	public Map<String, Object> getValues( RecordBase<?> base, int primaryKey, String[] columns ) throws IllegalArgumentException
	{
		try(Statement stm = con.createStatement())
		{
			ResultSet res = stm.executeQuery("SELECT "+toColumnsList(columns, base.getPrimaryColumn() )+" FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey);
			if(res.next())
			{
				Map<String,Object> values=new HashMap<>(columns.length);
				for(String column:columns)
				{
					values.put( column, res.getObject( column));
				}
				return values;
			}
			return Collections.emptyMap();
		}
		catch ( SQLException ex )
		{
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
		try(PreparedStatement stmt = con.prepareStatement( sql ))
		{
			return stmt.executeQuery().next();
		}
		catch ( SQLException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public void destroy( RecordBase<?> base, int primaryKey )
	{
		try(Statement stm = con.createStatement())
		{
			stm.executeUpdate( "DELETE FROM "+base.getTableName()+" WHERE "+base.getPrimaryColumn()+" = "+primaryKey);
		}
		catch ( SQLException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Map<String, Object> findFirstWithData( RecordBase<?> base, String[] columns, Condition condition )
	{
		String sql = "SELECT "+toColumnsList( columns, base.getPrimaryColumn() )+" FROM "+base.getTableName()+" "+toWhereClause( condition )+" ORDER BY "+base.getDefaultOrder().toSQL()+" LIMIT 1";
		try(PreparedStatement stm = con.prepareStatement(sql))
		{
			if(condition!=null)
			{
				fillStatement( stm, condition );
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
			return Collections.emptyMap();
		}
		catch ( SQLException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public int count( RecordBase<?> base, Condition condition )
	{
		String sql = "SELECT COUNT(1) as size FROM "+base.getTableName()+" "+toWhereClause( condition );
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
			return -1;
		}
		catch ( SQLException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public Stream<Map<String, Object>> streamAllWithData( RecordBase<?> base, String[] columns, Condition condition )
	{
		String sql = "SELECT "+toColumnsList( columns, base.getPrimaryColumn() )+" FROM "+base.getTableName()+" "+toWhereClause( condition )+" ORDER BY "+base.getDefaultOrder().toSQL();
		try(PreparedStatement stm = con.prepareStatement(sql))
		{
			if(condition!=null)
			{
				fillStatement( stm, condition );
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
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public boolean exists(String tableName)
	{
		try(ResultSet set = con.getMetaData().getTables(null, null, null, null ))
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
			return false;
		}
	}

	@Override
	public String[] getAllColumnNames( String tableName )
	{
		try(ResultSet set = con.getMetaData().getColumns( null, null, tableName, null))
		{
			List<String> columns = new ArrayList<>(10);
			while(set.next())
			{
				columns.add( set.getString( "COLUMN_NAME"));
			}
			return columns.stream().map( (String s) -> s.toLowerCase()).collect( Collectors.toList()).toArray( new String[columns.size()]);
		}
		catch ( SQLException ex )
		{
			throw new RuntimeException("Failed to retrieve columns for table '"+tableName+"'",ex);
		}
	}

	@Override
	public int insertNewRecord(RecordBase<?> base)
	{
		try
		{
			PreparedStatement stmt;
			if(base.isTimestamped())
			{
				stmt = con.prepareStatement( "INSERT INTO "+base.getTableName()+" ("+COLUMN_CREATED_AT+", "+COLUMN_UPDATED_AT+") VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS );
				long timestamp = System.currentTimeMillis();
				stmt.setTimestamp( 1, new Timestamp(timestamp));
				stmt.setTimestamp( 2, new Timestamp(timestamp));
			}
			else
			{
				//TODO does this even work?
				stmt = con.prepareStatement( "INSERT INTO "+base.getTableName()+" ()", Statement.RETURN_GENERATED_KEYS);
			}
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if(rs.next())
			{
				return rs.getInt( base.getPrimaryColumn());
			}
			return -1;
		}
		catch ( SQLException ex )
		{
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
}
