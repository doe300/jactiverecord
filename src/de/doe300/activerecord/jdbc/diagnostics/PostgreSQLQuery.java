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

package de.doe300.activerecord.jdbc.diagnostics;

import de.doe300.activerecord.store.JDBCRecordStore;
import de.doe300.activerecord.store.diagnostics.LoggedQuery;
import de.doe300.activerecord.store.diagnostics.QueryRemark;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of a {@link LoggedQuery} for the PostgreSQL database engine
 *
 * @author doe300
 * @since 0.8
 */
public class PostgreSQLQuery extends JDBCQuery
{

	public PostgreSQLQuery( JDBCRecordStore store, String source, String storeName, long duration )
	{
		super( store, source, storeName, duration );
	}

	@Override
	protected List<String> runExplain( String sqlStatment ) throws Exception
	{
		//https://www.postgresql.org/docs/current/static/using-explain.html
		//EXPLAIN
		//EXPLAIN ANALYZE will execute the query resulting in real results as comparison, but is not needed
		try(final Statement stmt = ((JDBCRecordStore)store).getConnection().createStatement();
				final ResultSet result = stmt.executeQuery( "EXPLAIN " + sqlStatment))
		{
			//QUERY PLAN (text)
			final List<String> output = new ArrayList<>(32);
			while(result.next())
			{
				output.add( result.getString( 1));
			}
			return output;
		}
	}

	@Override
	protected List<QueryRemark<String>> createRemarks( String sqlStatment ) throws Exception
	{
		final Iterator<String> details = explainQuery().iterator();
		final List<QueryRemark<String>> remarks = new ArrayList<>(4);
		while(details.hasNext())
		{
			final String detail = details.next();
			//Possible contents of lines:
			//[Seq|Bitmap [Heap|Index]|Index] Scan - "Seq" walks trough complete table, "Bitmap X" only trough results of previous filtering and "Index" uses index
			//Nested Loop - runs the "inner" node for every row of the outer row
			//Sort Method: - "quicksort" is good, anything else not!?
			if(detail.toUpperCase().contains( "SEQ SCAN"))
			{
				//sequential scan, bad
				//Seq Scan on table-name ....
				final String tableName = detail.substring( detail.toUpperCase().indexOf( " ON ") + 4).split( " ")[0];
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.INDEX, tableName, "Using sequential scan over table '" + tableName + "'.Consider adding an index!"));
			}
			if(detail.toUpperCase().contains( "NESTED LOOOP"))
			{
				//nested loop, inner node is executed n-times
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.OTHER, getStoreName(), "Sub-query is run for every row in the parent query!"));
			}
		}
		return remarks;
	}

}
