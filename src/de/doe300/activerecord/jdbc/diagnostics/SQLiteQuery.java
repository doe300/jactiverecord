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
 * Implementation of a {@link LoggedQuery} for the SQLite library
 *
 * @author doe300
 * @since 0.8
 */
public class SQLiteQuery extends JDBCQuery
{

	public SQLiteQuery( JDBCRecordStore store, String source, String storeName, long duration )
	{
		super( store, source, storeName, duration );
	}

	@Override
	protected List<String> runExplain( String sqlStatment ) throws Exception
	{
		//https://www.sqlite.org/eqp.html
		//EXPLAIN QUERY PLAN
		try(final Statement stmt = ((JDBCRecordStore)store).getConnection().createStatement();
				final ResultSet result = stmt.executeQuery( "EXPLAIN QUERY PLAN " + sqlStatment))
		{
			//selectid(INTEGER) | order (INTEGER) | from (INTEGER) | detail (VARCHAR)
			final List<String> output = new ArrayList<>(32);
			while(result.next())
			{
				//only detail is of interest
				output.add( result.getString( 4));
			}
			return output;
		}
	}

	@Override
	protected List<QueryRemark<String>> createRemarks( String sqlStatment ) throws Exception
	{
		final List<QueryRemark<String>> remarks = new ArrayList<>(4);
		final Iterator<String> details = explainQuery().iterator();
		while(details.hasNext())
		{
			final String detail = details.next();
			//every detail-line starts with eiter of following:
			//SCAN - full table scan
			//SEARCH - only subset visited
			//also included are information about:
			//[WITH|USING] [INDEX|PRIMARY KEY] - index or primary key (automatic index) is used
			//USE TEMP B-TREE FOR [ORDER BY|GROUP BY|DISTINCT] - temporary B-tree required for sorting/grouping
			//EXECUTE - an additional subquery is run
			//EXECUTE CORRELATED - the subquery is run for every row in the parent query, otherwise the query is only run once
			if(detail.toUpperCase().contains( "INDEX") || detail.toUpperCase().contains( "PRIMARY KEY"))
			{
				//using index, skip
				continue;
			}
			if(detail.toUpperCase().contains( "USE TEMP B-TREE FOR"))
			{
				//temporary b-tree, index is much faster
				final String remainder = detail.toUpperCase().substring( "USE TEMP B-TREE FOR".length() ).trim();
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.INDEX, getStoreName(), "Using temporary B-tree for " + remainder + ". Consider adding an index!"));
			}
			if(detail.toUpperCase().contains( "EXECUTE CORRELATED" ))
			{
				//sub-query run for every row in parent
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.OTHER, getStoreName(), "Sub-query is run for every row in the parent query!"));
			}
		}
		return remarks;
	}

}
