/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 doe300
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
package de.doe300.activerecord.performance;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import java.sql.Connection;
import java.sql.Statement;

import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.logging.NoLogger;
import de.doe300.activerecord.store.JDBCRecordStore;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;

/**
 *
 * @author doe300
 * @since 0.7
 */
public class PerformanceComparison
{
	private static RecordCore core;
	
	//TODO possible improvements:
	//reuse statements
	//	problem: only works, if number and names of columns match
	//-> for cached store, always write all columns??
	//-> at least for tables up to a certain size (and without any too big column-types)
			
	public static void main(final String[] args) throws Exception
	{
		//init core
		core = TestServer.getTestCore(SimpleJDBCRecordStore.class);
		TestServer.getTestCore( CachedJDBCRecordStore.class);
		//hide log
		Logging.setLogger( new NoLogger());
		System.err.println( "" );
		testInsertRow( 1_000_000);
		System.err.println( "" );
		testUpdateRow( 1_000_000);
		System.err.println( "" );
		testResultSetTypes( 1_000_000);
	}

	private static void testInsertRow(final int numIterations) throws Exception
	{
		System.err.println( "Insert new row: " + numIterations );
		final Connection con = ((JDBCRecordStore)core.getStore()).getConnection();
		final String tableName = core.getBase( TestInterface.class).getTableName();
		final String[] columns = {"name", "age"};
		final Object[] values = {"Adam", 24};
		final String plainSQL = "INSERT INTO " + tableName + " (" + columns[0] + ", " + columns[1] + " ) VALUES ('" +
			values[0] + "', " + values[1] + ')';
		final String preparedSQL = "INSERT INTO " + tableName + " (" + columns[0] + ", " + columns[1] + " ) VALUES (?, ?)";

		//1. plain statement
		core.createTable( TestInterface.class);
		final long startPlain = System.currentTimeMillis();
		for(int i = 0 ; i < numIterations; ++i)
		{
			try(final Statement stmt = con.createStatement())
			{
				stmt.execute( plainSQL );
			}
		}
		final long plainDuration = System.currentTimeMillis() - startPlain;
		System.err.println("Plain Statement: " + plainDuration + "ms (" + plainDuration / (double) numIterations + ")");
		core.dropTable( TestInterface.class);
		
		//2. prepared statement
		core.createTable( TestInterface.class);
		final long startPrepared = System.currentTimeMillis();
		for(int i = 0 ; i < numIterations; ++i)
		{
			try(final PreparedStatement stmt = con.prepareStatement( preparedSQL))
			{
				stmt.setString( 1, (String)values[0]);
				stmt.setInt( 2, (Integer)values[1]);
				stmt.execute( );
			}
		}
		final long preparedDuration = System.currentTimeMillis() - startPrepared;
		System.err.println("Prepared Statement: " + preparedDuration + "ms (" + preparedDuration / (double) numIterations + ")");
		core.dropTable( TestInterface.class);
		
		//3. reuse plain statement
		core.createTable( TestInterface.class);
		final long startReusePlain = System.currentTimeMillis();
		try(final Statement stmt = con.createStatement())
		{
			for(int i = 0 ; i < numIterations; ++i)
			{
				stmt.execute( plainSQL );
			}
		}
		final long plainReuseDuration = System.currentTimeMillis() - startReusePlain;
		System.err.println("Reuse Plain Statement: " + plainReuseDuration + "ms (" + plainReuseDuration / (double) numIterations + ")");
		core.dropTable( TestInterface.class);
		
		//4. reuse prepared statement
		core.createTable( TestInterface.class);
		final long startReusePrepared = System.currentTimeMillis();
		try(final PreparedStatement stmt = con.prepareStatement( preparedSQL))
		{
			for(int i = 0 ; i < numIterations; ++i)
			{
				stmt.setString( 1, (String)values[0]);
				stmt.setInt( 2, (Integer)values[1]);
				stmt.execute( );
			}
		}
		final long preparedReuseDuration = System.currentTimeMillis() - startReusePrepared;
		System.err.println("Reuse Prepared Statement: " + preparedReuseDuration + "ms (" + preparedReuseDuration / (double) numIterations + ")");
		core.dropTable( TestInterface.class);
		
		//5. batch plain statement
		core.createTable( TestInterface.class);
		final long startBatchPlain = System.currentTimeMillis();
		try(final Statement stmt = con.createStatement())
		{
			for(int i = 0 ; i < numIterations; ++i)
			{
				stmt.addBatch( plainSQL );
			}
			stmt.executeBatch();
		}
		final long plainBatchDuration = System.currentTimeMillis() - startBatchPlain;
		System.err.println("Batch Plain Statement: " + plainBatchDuration + "ms (" + plainBatchDuration / (double) numIterations + ")");
		core.dropTable( TestInterface.class);
		
		//6. batch prepared statement
		core.createTable( TestInterface.class);
		final long startBatchPrepared = System.currentTimeMillis();
		try(final PreparedStatement stmt = con.prepareStatement( preparedSQL))
		{
			for(int i = 0 ; i < numIterations; ++i)
			{
				stmt.setString( 1, (String)values[0]);
				stmt.setInt( 2, (Integer)values[1]);
				stmt.addBatch();
			}
			stmt.executeBatch();
		}
		final long preparedBatchDuration = System.currentTimeMillis() - startBatchPrepared;
		System.err.println("Batch Prepared Statement: " + preparedBatchDuration + "ms (" + preparedBatchDuration / (double) numIterations + ")");
		core.dropTable( TestInterface.class);
		
		//7. simple JDBCRecordStore
		core.createTable( TestInterface.class);
		final long startRecordStore = System.currentTimeMillis();
		final RecordStore store = core.getStore();
		final RecordBase<TestInterface> base = core.getBase( TestInterface.class);
		for(int i = 0 ; i < numIterations; ++i)
		{
			final Map<String, Object> data = new HashMap<>(2);
			data.put(columns[0], values[0]);
			data.put(columns[1], values[1]);
			store.insertNewRecord( base, data );
		}
		final long recordStoreDuration = System.currentTimeMillis() - startRecordStore;
		System.err.println("simple RecordStore: " + recordStoreDuration + "ms (" + recordStoreDuration / (double) numIterations + ")");
		core.dropTable( TestInterface.class);
		
		//8. cached JDBCRecordStore
		core.createTable( TestInterface.class);
		final long startCachedRecordStore = System.currentTimeMillis();
		final RecordBase<TestInterface> cachedBase = TestServer.getTestCore( CachedJDBCRecordStore.class).getBase( TestInterface.class);
		final RecordStore cachedStore = cachedBase.getStore();
		for(int i = 0 ; i < numIterations; ++i)
		{
			final Map<String, Object> data = new HashMap<>(2);
			data.put(columns[0], values[0]);
			data.put(columns[1], values[1]);
			cachedStore.insertNewRecord( cachedBase, data );
		}
		final long cachedRecordStoreDuration = System.currentTimeMillis() - startCachedRecordStore;
		System.err.println("cached RecordStore: " + cachedRecordStoreDuration + "ms (" + cachedRecordStoreDuration / (double) numIterations + ")");
		
	}
	
	private static void testUpdateRow(final int numIterations) throws Exception
	{
		//first row starts with 1 not 0
		System.err.println( "Update row: " + (numIterations - 1) );
		final Connection con = ((JDBCRecordStore)core.getStore()).getConnection();
		final String tableName = core.getBase( TestInterface.class).getTableName();
		final String[] columns = {"name", "age"};
		final Object[] values = {"Adam", 24};
		final String plainSQL = "UPDATE " + tableName + " SET " + columns[0] + " = '" + values[0] +"', " + columns[1] + " = " + values[1] +
				" WHERE id = ";
		final String preparedSQL = "UPDATE " + tableName + " SET " + columns[0] + " = ?, " + columns[1] + " = ? WHERE id = ?";
		
		//1. plain statement
		final long startPlain = System.currentTimeMillis();
		for(int i = 1 ; i < numIterations; ++i)
		{
			try(final Statement stmt = con.createStatement())
			{
				stmt.execute( plainSQL + i );
			}
		}
		final long plainDuration = System.currentTimeMillis() - startPlain;
		System.err.println("Plain Statement: " + plainDuration + "ms (" + plainDuration / (double) numIterations + ")");
		
		//2. prepared statement
		final long startPrepared = System.currentTimeMillis();
		for(int i = 1 ; i < numIterations; ++i)
		{
			try(final PreparedStatement stmt = con.prepareStatement( preparedSQL))
			{
				stmt.setString( 1, (String)values[0]);
				stmt.setInt( 2, (Integer)values[1]);
				stmt.setInt( 3, i);
				stmt.execute( );
			}
		}
		final long preparedDuration = System.currentTimeMillis() - startPrepared;
		System.err.println("Prepared Statement: " + preparedDuration + "ms (" + preparedDuration / (double) numIterations + ")");
		
		//3. reuse plain statement
		final long startReusePlain = System.currentTimeMillis();
		try(final Statement stmt = con.createStatement())
		{
			for(int i = 1 ; i < numIterations; ++i)
			{
				stmt.execute( plainSQL + i );
			}
		}
		final long plainReuseDuration = System.currentTimeMillis() - startReusePlain;
		System.err.println("Reuse Plain Statement: " + plainReuseDuration + "ms (" + plainReuseDuration / (double) numIterations + ")");
		
		//4. reuse prepared statement
		final long startReusePrepared = System.currentTimeMillis();
		try(final PreparedStatement stmt = con.prepareStatement( preparedSQL))
		{
			for(int i = 1 ; i < numIterations; ++i)
			{
				stmt.setString( 1, (String)values[0]);
				stmt.setInt( 2, (Integer)values[1]);
				stmt.setInt( 3, i);
				stmt.execute( );
			}
		}
		final long preparedReuseDuration = System.currentTimeMillis() - startReusePrepared;
		System.err.println("Reuse Prepared Statement: " + preparedReuseDuration + "ms (" + preparedReuseDuration / (double) numIterations + ")");
		
		//5. batch plain statement
		final long startBatchPlain = System.currentTimeMillis();
		try(final Statement stmt = con.createStatement())
		{
			for(int i = 1 ; i < numIterations; ++i)
			{
				stmt.addBatch( plainSQL + i );
			}
			stmt.executeBatch();
		}
		final long plainBatchDuration = System.currentTimeMillis() - startBatchPlain;
		System.err.println("Batch Plain Statement: " + plainBatchDuration + "ms (" + plainBatchDuration / (double) numIterations + ")");
		
		//6. batch prepared statement
		final long startBatchPrepared = System.currentTimeMillis();
		try(final PreparedStatement stmt = con.prepareStatement( preparedSQL))
		{
			for(int i = 1 ; i < numIterations; ++i)
			{
				stmt.setString( 1, (String)values[0]);
				stmt.setInt( 2, (Integer)values[1]);
				stmt.setInt( 3, i);
				stmt.addBatch();
			}
			stmt.executeBatch();
		}
		final long preparedBatchDuration = System.currentTimeMillis() - startBatchPrepared;
		System.err.println("Batch Prepared Statement: " + preparedBatchDuration + "ms (" + preparedBatchDuration / (double) numIterations + ")");
		
		//7. simple JDBCRecordStore
		final long startRecordStore = System.currentTimeMillis();
		final RecordStore store = core.getStore();
		final RecordBase<TestInterface> base = core.getBase( TestInterface.class);
		for(int i = 1 ; i < numIterations; ++i)
		{
			store.setValues(base, i, columns, values);
		}
		final long recordStoreDuration = System.currentTimeMillis() - startRecordStore;
		System.err.println("simple RecordStore: " + recordStoreDuration + "ms (" + recordStoreDuration / (double) numIterations + ")");
		
		//7. cached JDBCRecordStore
		final long startCachedRecordStore = System.currentTimeMillis();
		final RecordBase<TestInterface> cachedBase = TestServer.getTestCore( CachedJDBCRecordStore.class).getBase( TestInterface.class);
		final RecordStore cachedStore = cachedBase.getStore();
		for(int i = 1 ; i < numIterations; ++i)
		{
			cachedStore.setValues(cachedBase, i, columns, values);
		}
		final long cachedRecordStoreDuration = System.currentTimeMillis() - startCachedRecordStore;
		System.err.println("cached RecordStore: " + cachedRecordStoreDuration + "ms (" + cachedRecordStoreDuration / (double) numIterations + ")");
	}
	
	private static void testResultSetTypes(final long numIterations) throws SQLException
	{
		System.err.println( "Query row: " + (numIterations - 1) );
		final Connection con = ((JDBCRecordStore)core.getStore()).getConnection();
		final String tableName = core.getBase( TestInterface.class).getTableName();
		final String preparedSQL = "SELECT * FROM " +tableName + " WHERE id = ?";
		
		//1. "simple" prepared statement
		final long startSimpleQuery = System.currentTimeMillis();
		try(final PreparedStatement stmt = con.prepareStatement( preparedSQL))
		{
			for(int i = 1 ; i < numIterations; ++i)
			{
				stmt.setInt( 1, i);
				try(final ResultSet set = stmt.executeQuery())
				{
					Assert.assertTrue( set.next());
				}
			}
		}
		final long simpleQueryDuration = System.currentTimeMillis() - startSimpleQuery;
		System.err.println("simple Query: " + simpleQueryDuration + "ms (" + simpleQueryDuration / (double) numIterations + ")");
		
		//2. with result set type and concurrency set
		final long startTypedQuery = System.currentTimeMillis();
		try(final PreparedStatement stmt = con.prepareStatement( preparedSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
		{
			for(int i = 1 ; i < numIterations; ++i)
			{
				stmt.setInt( 1, i);
				try(final ResultSet set = stmt.executeQuery())
				{
					Assert.assertTrue( set.next());
				}
			}
		}
		final long typedQueryDuration = System.currentTimeMillis() - startTypedQuery;
		System.err.println("typed and concurrency Query: " + typedQueryDuration + "ms (" + typedQueryDuration / (double) numIterations + ")");
		
		//3. with additionally holdability set
		final long startHoldabilityQuery = System.currentTimeMillis();
		try(final PreparedStatement stmt = con.prepareStatement( preparedSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT))
		{
			for(int i = 1 ; i < numIterations; ++i)
			{
				stmt.setInt( 1, i);
				try(final ResultSet set = stmt.executeQuery())
				{
					Assert.assertTrue( set.next());
				}
			}
		}
		final long holdabilityDuration = System.currentTimeMillis() - startHoldabilityQuery;
		System.err.println("additionally holdability Query: " + holdabilityDuration + "ms (" + holdabilityDuration / (double) numIterations + ")");
	}
}
