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
	private final Profiler profiler;

	public ProfilingConnection( Connection con)
	{
		this.con = con;
		this.profiler = new Profiler(60);
	}

	public Profiler getProfiler()
	{
		return profiler;
	}
	
	@Override
	public Statement createStatement() throws SQLException
	{
		return profiler.profile( "createStatement", () -> con.createStatement());
	}

	@Override
	public PreparedStatement prepareStatement( String sql ) throws SQLException
	{
		return profiler.profile( "prepareStatement", () -> con.prepareStatement( sql));
	}

	@Override
	public CallableStatement prepareCall( String sql ) throws SQLException
	{
		return profiler.profile( "prepareCall", () -> con.prepareCall( sql));
	}

	@Override
	public String nativeSQL( String sql ) throws SQLException
	{
		return profiler.profile( "nativeSQL", () -> con.nativeSQL( sql));
	}

	@Override
	public void setAutoCommit( boolean autoCommit ) throws SQLException
	{
		profiler.profile( "setAutoCommit", ()->con.setAutoCommit( autoCommit));
	}

	@Override
	public boolean getAutoCommit() throws SQLException
	{
		return profiler.profile( "getAutoCommit", () -> con.getAutoCommit());
	}

	@Override
	public void commit() throws SQLException
	{
		profiler.profile( "commit", () -> con.commit());
	}

	@Override
	public void rollback() throws SQLException
	{
		profiler.profile( "rollback", () -> con.rollback());
	}

	@Override
	public void close() throws SQLException
	{
		profiler.profile("close", () -> con.close());
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		return profiler.profile( "isClosed", () -> con.isClosed());
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException
	{
		return profiler.profile( "getMetaData", () -> con.getMetaData());
	}

	@Override
	public void setReadOnly( boolean readOnly ) throws SQLException
	{
		profiler.profile( "setReadOnly", () -> con.setReadOnly( readOnly));
	}

	@Override
	public boolean isReadOnly() throws SQLException
	{
		return profiler.profile( "isReadOnly", () -> con.isReadOnly());
	}

	@Override
	public void setCatalog( String catalog ) throws SQLException
	{
		profiler.profile( "setCatalog", () -> con.setCatalog( catalog));
	}

	@Override
	public String getCatalog() throws SQLException
	{
		return profiler.profile( "getCatalog", () -> con.getCatalog());
	}

	@Override
	public void setTransactionIsolation( int level ) throws SQLException
	{
		profiler.profile( "setTransactionIsolation", () -> con.setTransactionIsolation( level));
	}

	@Override
	public int getTransactionIsolation() throws SQLException
	{
		return profiler.profile( "getTransactionIsolation", () -> con.getTransactionIsolation());
	}

	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		return profiler.profile( "getWarning", () -> con.getWarnings());
	}

	@Override
	public void clearWarnings() throws SQLException
	{
		profiler.profile("clearWarnings", () -> con.clearWarnings());
	}

	@Override
	public Statement createStatement( int resultSetType, int resultSetConcurrency ) throws SQLException
	{
		return profiler.profile("createStatment ResultSet", () -> con.createStatement( resultSetType, resultSetConcurrency ));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency ) throws SQLException
	{
		return profiler.profile( "prepareStatement ResultSet", () -> con.prepareStatement( sql, resultSetType,
				resultSetConcurrency));
	}

	@Override
	public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency ) throws SQLException
	{
		return profiler.profile( "prepareCall ResultSet", () -> con.prepareCall( sql, resultSetType, resultSetConcurrency));
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException
	{
		return profiler.profile( "getTypeMap", () -> con.getTypeMap());
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map ) throws SQLException
	{
		profiler.profile( "setTypeMap", () -> con.setTypeMap( map));
	}

	@Override
	public void setHoldability( int holdability ) throws SQLException
	{
		profiler.profile( "setHoldability", () -> con.setHoldability( holdability));
	}

	@Override
	public int getHoldability() throws SQLException
	{
		return profiler.profile( "getHoldability", () -> con.getHoldability());
	}

	@Override
	public Savepoint setSavepoint() throws SQLException
	{
		return profiler.profile( "setSavepoint", () -> con.setSavepoint());
	}

	@Override
	public Savepoint setSavepoint( String name ) throws SQLException
	{
		return profiler.profile( "setSavePoint name", () -> con.setSavepoint( name));
	}

	@Override
	public void rollback( Savepoint savepoint ) throws SQLException
	{
		profiler.profile( "rollback Savepoint", () -> con.rollback( savepoint));
	}

	@Override
	public void releaseSavepoint( Savepoint savepoint ) throws SQLException
	{
		profiler.profile( "releaseSavepoint", () -> con.releaseSavepoint( savepoint ));
	}

	@Override
	public Statement createStatement( int resultSetType, int resultSetConcurrency, int resultSetHoldability ) throws SQLException
	{
		return profiler.profile( "createStatement holdability", () -> con.createStatement( resultSetType,
				resultSetConcurrency, resultSetHoldability));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability ) throws SQLException
	{
		return profiler.profile( "prepareStatement holdability", () -> con.prepareStatement(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability ));
	}

	@Override
	public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability ) throws SQLException
	{
		return profiler.profile( "prepareCall", () -> con.prepareCall( sql, resultSetType, resultSetConcurrency,
				resultSetHoldability));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, int autoGeneratedKeys ) throws SQLException
	{
		return profiler.profile( "prepareStatement generateKeys", () -> con.prepareStatement( sql, autoGeneratedKeys));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, int[] columnIndexes ) throws SQLException
	{
		return profiler.profile( "prepareStatement columnIndices", () -> con.prepareStatement( sql, columnIndexes));
	}

	@Override
	public PreparedStatement prepareStatement( String sql, String[] columnNames ) throws SQLException
	{
		return profiler.profile( "prepareStatment columnNames", () -> con.prepareStatement( sql, columnNames));
	}

	@Override
	public Clob createClob() throws SQLException
	{
		return profiler.profile( "createClob", () -> con.createClob());
	}

	@Override
	public Blob createBlob() throws SQLException
	{
		return profiler.profile( "createBlob", () -> con.createBlob());
	}

	@Override
	public NClob createNClob() throws SQLException
	{
		return profiler.profile( "createNClob", () -> con.createNClob());
	}

	@Override
	public SQLXML createSQLXML() throws SQLException
	{
		return profiler.profile( "createSQLXML", () -> con.createSQLXML());
	}

	@Override
	public boolean isValid( int timeout ) throws SQLException
	{
		return profiler.profile( "isValid", () -> con.isValid( timeout));
	}

	@Override
	public void setClientInfo( String name, String value ) throws SQLClientInfoException
	{
		profiler.profile( "setClientInfo", () -> con.setClientInfo( name, value));
	}

	@Override
	public void setClientInfo( Properties properties ) throws SQLClientInfoException
	{
		profiler.profile( "setClientInfo Properties", () -> con.setClientInfo( properties));
	}

	@Override
	public String getClientInfo( String name ) throws SQLException
	{
		return profiler.profile( "getClientInfo", () -> con.getClientInfo( name));
	}

	@Override
	public Properties getClientInfo() throws SQLException
	{
		return profiler.profile( "getClientInfo Properties", () -> con.getClientInfo());
	}

	@Override
	public Array createArrayOf( String typeName, Object[] elements ) throws SQLException
	{
		return profiler.profile( "createArrayOf", () -> con.createArrayOf( typeName, elements));
	}

	@Override
	public Struct createStruct( String typeName, Object[] attributes ) throws SQLException
	{
		return profiler.profile( "createStruct", () -> con.createStruct( typeName, attributes));
	}

	@Override
	public void setSchema( String schema ) throws SQLException
	{
		profiler.profile( "setSchema", () -> con.setSchema( schema));
	}

	@Override
	public String getSchema() throws SQLException
	{
		return profiler.profile( "getSchema", () -> con.getSchema());
	}

	@Override
	public void abort( Executor executor ) throws SQLException
	{
		profiler.profile( "abort", () -> con.abort( executor ));
	}

	@Override
	public void setNetworkTimeout( Executor executor, int milliseconds ) throws SQLException
	{
		profiler.profile( "setNetworkTimeout", () -> con.setNetworkTimeout( executor, milliseconds));
	}

	@Override
	public int getNetworkTimeout() throws SQLException
	{
		return profiler.profile( "getNetworkTimeout", () -> con.getNetworkTimeout());
	}

	@Override
	public <T> T unwrap( Class<T> iface ) throws SQLException
	{
		return profiler.profile( "unwrap", () -> con.unwrap( iface));
	}

	@Override
	public boolean isWrapperFor(
			Class<?> iface ) throws SQLException
	{
		return profiler.profile( "isWrapperFor", () -> con.isWrapperFor( iface));
	}
}
