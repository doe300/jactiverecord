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
package de.doe300.activerecord.profiling;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Wrapper around a connection measuring the runtime
 * @author doe300
 */
public class ProfilingConnection implements Connection
{
	private final Connection con;
	private final Map<String, Integer> numberOfRuns;
	private final Map<String, Long> runtimes;

	public ProfilingConnection( Connection con)
	{
		this.con = con;
		this.numberOfRuns = new HashMap<>(60);
		this.runtimes = new HashMap<>(60);
	}
	
	private void increaseRuns(String name)
	{
		numberOfRuns.putIfAbsent( name, 0);
		numberOfRuns.put( name, numberOfRuns.get( name)+1);
	}
	
	private void increaseRuntime(String name, Long time)
	{
		runtimes.putIfAbsent( name, 0L);
		runtimes.put( name, runtimes.get( name)+time);
	}
	
	private <T, E extends Throwable> T profile(final String name, final ThrowingSupplier<T, E> sup) throws E
	{
		increaseRuns( name );
		long time = System.nanoTime();
		T res = sup.get();
		increaseRuntime( name, System.nanoTime()- time);
		return res;
	}
	
	private <E extends Throwable> void profile(final String name, ThrowingRunnable<E> run) throws E
	{
		increaseRuns( name );
		long time = System.nanoTime();
		run.run();
		increaseRuntime( name, System.nanoTime()- time);
	}
	
	public void printStatistics()
	{
		System.err.flush();
		System.out.flush();
		System.out.printf( "%30s|%10s|%16s|%16s%n", "Method", "# of Runs", "Time (in ms)", "Time per run" );
		double totalTime = 0;
		for(Map.Entry<String, Integer> entry : numberOfRuns.entrySet())
		{
			double time = runtimes.get( entry.getKey())/1000_000.0;
			totalTime+= time;
			double timePerRun = time/entry.getValue();
			System.out.printf( "%30s|%10d|%16.3f|%16.3f%n", entry.getKey(), entry.getValue(), time, timePerRun );
		}
		System.out.printf( "Total Time (in ms): %10.3f%n",totalTime );
	}

	@Override
	public Statement createStatement() throws SQLException
	{
		return profile( "createStatement", () -> con.createStatement());
	}

	@Override
	public PreparedStatement prepareStatement( String sql ) throws SQLException
	{
		return profile( "prepareStatement", () -> con.prepareStatement( sql));
	}

	@Override
	public CallableStatement prepareCall( String sql ) throws SQLException
	{
		return profile( "prepareCall", () -> con.prepareCall( sql));
	}

	@Override
	public String nativeSQL( String sql ) throws SQLException
	{
		return profile( "nativeSQL", () -> con.nativeSQL( sql));
	}

	@Override
	public void setAutoCommit( boolean autoCommit ) throws SQLException
	{
		profile( "setAutoCommit", ()->con.setAutoCommit( autoCommit));
	}

	@Override
	public boolean getAutoCommit() throws SQLException
	{
		return profile( "getAutoCommit", () -> con.getAutoCommit());
	}

	@Override
	public void commit() throws SQLException
	{
		profile( "commit", () -> con.commit());
	}

	@Override
	public void rollback() throws SQLException
	{
		profile( "rollback", () -> con.rollback());
	}

	@Override
	public void close() throws SQLException
	{
		printStatistics();
		profile("close", () -> con.close());
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		return profile( "isClosed", () -> con.isClosed());
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException
	{
		return profile( "getMetaData", () -> con.getMetaData());
	}

	@Override
	public void setReadOnly( boolean readOnly ) throws SQLException
	{
		profile( "setReadOnly", () -> con.setReadOnly( readOnly));
	}

	@Override
	public boolean isReadOnly() throws SQLException
	{
		return profile( "isReadOnly", () -> con.isReadOnly());
	}

	@Override
	public void setCatalog( String catalog ) throws SQLException
	{
		profile( "setCatalog", () -> con.setCatalog( catalog));
	}

	@Override
	public String getCatalog() throws SQLException
	{
		return profile( "getCatalog", () -> con.getCatalog());
	}

	@Override
	public void setTransactionIsolation( int level ) throws SQLException
	{
		profile( "setTransactionIsolation", () -> con.setTransactionIsolation( level));
	}

	@Override
	public int getTransactionIsolation() throws SQLException
	{
		return profile( "getTransactionIsolation", () -> con.getTransactionIsolation());
	}

	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		return profile( "getWarning", () -> con.getWarnings());
	}

	@Override
	public void clearWarnings() throws SQLException
	{
		profile("clearWarnings", () -> con.clearWarnings());
	}

	@Override
	public Statement createStatement( int resultSetType, int resultSetConcurrency ) throws SQLException
	{
		return profile("createStatment ResultSet", () -> con.createStatement( resultSetType, resultSetConcurrency ));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency ) throws SQLException
	{
		return profile( "prepareStatement ResultSet", () -> con.prepareStatement( sql, resultSetType,
				resultSetConcurrency));
	}

	@Override
	public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency ) throws SQLException
	{
		return profile( "prepareCall ResultSet", () -> con.prepareCall( sql, resultSetType, resultSetConcurrency));
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException
	{
		return profile( "getTypeMap", () -> con.getTypeMap());
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map ) throws SQLException
	{
		profile( "setTypeMap", () -> con.setTypeMap( map));
	}

	@Override
	public void setHoldability( int holdability ) throws SQLException
	{
		profile( "setHoldability", () -> con.setHoldability( holdability));
	}

	@Override
	public int getHoldability() throws SQLException
	{
		return profile( "getHoldability", () -> con.getHoldability());
	}

	@Override
	public Savepoint setSavepoint() throws SQLException
	{
		return profile( "setSavepoint", () -> con.setSavepoint());
	}

	@Override
	public Savepoint setSavepoint( String name ) throws SQLException
	{
		return profile( "setSavePoint name", () -> con.setSavepoint( name));
	}

	@Override
	public void rollback( Savepoint savepoint ) throws SQLException
	{
		profile( "rollback Savepoint", () -> con.rollback( savepoint));
	}

	@Override
	public void releaseSavepoint( Savepoint savepoint ) throws SQLException
	{
		profile( "releaseSavepoint", () -> con.releaseSavepoint( savepoint ));
	}

	@Override
	public Statement createStatement( int resultSetType, int resultSetConcurrency, int resultSetHoldability ) throws SQLException
	{
		return profile( "createStatement holdability", () -> con.createStatement( resultSetType,
				resultSetConcurrency, resultSetHoldability));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability ) throws SQLException
	{
		return profile( "prepareStatement holdability", () -> con.prepareStatement(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability ));
	}

	@Override
	public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability ) throws SQLException
	{
		return profile( "prepareCall", () -> con.prepareCall( sql, resultSetType, resultSetConcurrency,
				resultSetHoldability));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, int autoGeneratedKeys ) throws SQLException
	{
		return profile( "prepareStatement generateKeys", () -> con.prepareStatement( sql, autoGeneratedKeys));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, int[] columnIndexes ) throws SQLException
	{
		return profile( "prepareStatement columnIndices", () -> con.prepareStatement( sql, columnIndexes));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, String[] columnNames ) throws SQLException
	{
		return profile( "prepareStatment columnNames", () -> con.prepareStatement( sql, columnNames));
	}

	@Override
	public Clob createClob() throws SQLException
	{
		return profile( "createClob", () -> con.createClob());
	}

	@Override
	public Blob createBlob() throws SQLException
	{
		return profile( "createBlob", () -> con.createBlob());
	}

	@Override
	public NClob createNClob() throws SQLException
	{
		return profile( "createNClob", () -> con.createNClob());
	}

	@Override
	public SQLXML createSQLXML() throws SQLException
	{
		return profile( "createSQLXML", () -> con.createSQLXML());
	}

	@Override
	public boolean isValid( int timeout ) throws SQLException
	{
		return profile( "isValid", () -> con.isValid( timeout));
	}

	@Override
	public void setClientInfo( String name, String value ) throws SQLClientInfoException
	{
		profile( "setClientInfo", () -> con.setClientInfo( name, value));
	}

	@Override
	public void setClientInfo( Properties properties ) throws SQLClientInfoException
	{
		profile( "setClientInfo Properties", () -> con.setClientInfo( properties));
	}

	@Override
	public String getClientInfo( String name ) throws SQLException
	{
		return profile( "getClientInfo", () -> con.getClientInfo( name));
	}

	@Override
	public Properties getClientInfo() throws SQLException
	{
		return profile( "getClientInfo Properties", () -> con.getClientInfo());
	}

	@Override
	public Array createArrayOf( String typeName, Object[] elements ) throws SQLException
	{
		return profile( "createArrayOf", () -> con.createArrayOf( typeName, elements));
	}

	@Override
	public Struct createStruct( String typeName, Object[] attributes ) throws SQLException
	{
		return profile( "createStruct", () -> con.createStruct( typeName, attributes));
	}

	@Override
	public void setSchema( String schema ) throws SQLException
	{
		profile( "setSchema", () -> con.setSchema( schema));
	}

	@Override
	public String getSchema() throws SQLException
	{
		return profile( "getSchema", () -> con.getSchema());
	}

	@Override
	public void abort( Executor executor ) throws SQLException
	{
		profile( "abort", () -> con.abort( executor ));
	}

	@Override
	public void setNetworkTimeout( Executor executor, int milliseconds ) throws SQLException
	{
		profile( "setNetworkTimeout", () -> con.setNetworkTimeout( executor, milliseconds));
	}

	@Override
	public int getNetworkTimeout() throws SQLException
	{
		return profile( "getNetworkTimeout", () -> con.getNetworkTimeout());
	}

	@Override
	public <T> T unwrap( Class<T> iface ) throws SQLException
	{
		return profile( "unwrap", () -> con.unwrap( iface));
	}

	@Override
	public boolean isWrapperFor(
			Class<?> iface ) throws SQLException
	{
		return profile( "isWrapperFor", () -> con.isWrapperFor( iface));
	}

	@FunctionalInterface
	private static interface ThrowingSupplier<T, E extends Throwable>
	{
		public T get() throws E;
	}
	
	@FunctionalInterface
	private static interface ThrowingRunnable<E extends Throwable>
	{
		public void run() throws E;
	}
}
