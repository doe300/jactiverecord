/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.doe300.activerecord.bugs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * For SQLite, ResultSet, Statements and PreparedStatements are leaked (not deleted) for almost every call
 * @author doe300
 * @since 0.9
 */
public class SQLiteStatementLeak
{
	//Occurs with SQLite-JDBC 3.8.11.2
	//Reported at https://github.com/xerial/sqlite-jdbc/issues/147 and https://github.com/xerial/sqlite-jdbc/issues/148
	
	public static void main(String[] args) throws Exception
	{
		final Connection con = DriverManager.getConnection("jdbc:sqlite::memory:");
		
		try(Statement stmt = con.createStatement())
		{
			stmt.execute( "CREATE TABLE TestStatementLeak ( id INTEGER)");
		}
		
		try(Statement stmt = con.createStatement())
		{
			stmt.execute( "INSERT INTO TestStatementLeak (id) VALUES(1);");
			stmt.execute( "INSERT INTO TestStatementLeak (id) VALUES(2);");
		}
		
		testPreparedStatement(con);
		testTryWithPreparedStatement(con);
		testTryWithPreparedStatement2(con);
		testStatement(con);
		testTryWithStatement(con);
		testTryWithStatement2(con);
		
		try(Statement stmt = con.createStatement())
		{
			stmt.execute( "DROP TABLE TestStatementLeak");
		}
	}
	
	private static void testPreparedStatement(final Connection con) throws SQLException
	{
		//closes nothing - "should" leak -> leaks
		final PreparedStatement stmt = con.prepareStatement( "SELECT * FROM TestStatementLeak");
		final ResultSet res = stmt.executeQuery();
		while(res.next())
		{
			//"reads" ResultSet
		}
		System.out.println( "May leak:" );
		System.out.println( "Prepared Statement leaked: " + !stmt.isClosed() );
		System.out.println( "ResultSet leaked: " + !res.isClosed() );
	}
	
	private static void testTryWithPreparedStatement(final Connection con) throws SQLException
	{
		//closes ResultSet - should not leak -> leaks!!
		final PreparedStatement stmt = con.prepareStatement( "SELECT * FROM TestStatementLeak");
		stmt.closeOnCompletion();
		try(final ResultSet res = stmt.executeQuery())
		{
			while(res.next())
			{
				//"reads" ResultSet
			}
		}
		System.err.println( "MUST NOT LEAK:" );
		System.err.println( "Prepared Statement leaked: " + !stmt.isClosed() );
	}
	
	private static void testTryWithPreparedStatement2(final Connection con) throws SQLException
	{
		//closes Statement - should not leak -> does not leak
		try(final PreparedStatement stmt = con.prepareStatement( "SELECT * FROM TestStatementLeak"))
		{
			final ResultSet res = stmt.executeQuery();
			while(res.next())
			{
				//"reads" ResultSet
			}
		}
	}
	
	private static void testStatement(final Connection con) throws SQLException
	{
		//closes nothing - "should" leak -> leaks
		final Statement stmt = con.createStatement();
		final ResultSet res = stmt.executeQuery( "SELECT * FROM TestStatementLeak");
		while(res.next())
		{
			//"reads" ResultSet
		}
		System.out.println( "May leak:" );
		System.out.println( "Statement leaked: " + !stmt.isClosed() );
		System.out.println( "ResultSet leaked: " + !res.isClosed() );
	}
	
	private static void testTryWithStatement(final Connection con) throws SQLException
	{
		//closes ResultSet - should not leak -> leaks!!!
		final Statement stmt = con.createStatement();
		stmt.closeOnCompletion();
		try(final ResultSet res = stmt.executeQuery( "SELECT * FROM TestStatementLeak"))
		{
			while(res.next())
			{
				//"reads" ResultSet
			}
		}
		System.err.println( "MUST NOT LEAK:" );
		System.err.println( "Statement leaked: " + !stmt.isClosed() );
	}
	
	private static void testTryWithStatement2(final Connection con) throws SQLException
	{
		//closes Statement - should not leak -> does not leak
		try(final Statement stmt = con.createStatement())
		{
			final ResultSet res = stmt.executeQuery( "SELECT * FROM TestStatementLeak");
			while(res.next())
			{
				//"reads" ResultSet
			}
		}
	}
}
