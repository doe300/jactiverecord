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
import de.doe300.activerecord.store.diagnostics.QueryRemark;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a {@link Query} for MYSQL databases
 *
 * @author doe300
 * @since 0.8
 */
public class MySQLQuery extends JDBCQuery
{
	private List<ResultRow> explanation;
	
	public MySQLQuery( JDBCRecordStore store, String source, String storeName, long duration )
	{
		super( store, source, storeName, duration );
	}
	
	private List<ResultRow> explain(String sqlStatment) throws Exception
	{
		if(explanation != null)
		{
			return explanation;
		}
		explanation = new ArrayList<>(4);
		//https://dev.mysql.com/doc/refman/5.6/en/explain-output.html
		//EXPLAIN EXTENDED
		try(final Statement stmt = ((JDBCRecordStore)store).getConnection().createStatement();
				final ResultSet result = stmt.executeQuery( "EXPLAIN EXTENDED " + sqlStatment))
		{
			while(result.next())
			{
				explanation.add( new ResultRow(result.getString( "select_type"), result.getString( "table"), 
						result.getString( "type"), result.getString( "key"), result.getString( "extra")));
			}
		}
		return explanation;
	}

	@Override
	protected List<String> runExplain( String sqlStatment ) throws Exception
	{
		final List<String> output = new ArrayList<>(32);
		for(ResultRow row : explain( sqlStatment ))
		{
			output.add( row.selectType + " | " + row.table + " | " + row.type + " | " + row.key + " | " + row.extra );
		}
		return output;
	}

	@Override
	protected List<QueryRemark<String>> createRemarks( String sqlStatment ) throws Exception
	{
		final List<QueryRemark<String>> remarks = new ArrayList<>(32);
		for(ResultRow row : explain( sqlStatment ))
		{
			if(row.selectType.toUpperCase().contains( "UNCACHEABLE"))
			{
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.OTHER, row.table, "Uncacheable sub-query!"));
			}
			if(row.selectType.toUpperCase().contains( "DEPENDENT"))
			{
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.INDEX, row.table, "Sub-query is executed for every row of the parent!"));
			}
			if(row.type.toUpperCase().equals( "ALL"))
			{
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.INDEX, row.table, "Scans all rows of the table. Consider adding indices!"));
			}
			if(row.key == null || row.key.isEmpty())
			{
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.INDEX, row.table, "No key found for this query, consider adding indices!"));
			}
			if(row.extra.toUpperCase().contains( "FILESORT"))
			{
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.INDEX, row.table, "Query requires filesort for sorting. Consider adding indices!"));
			}
			if(row.extra.toUpperCase().contains( "IMPOSSIBLE"))
			{
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.OTHER, row.table, "Query has impossible parts, e.g. conflicting conditions!"));
			}
			if(row.extra.toUpperCase().contains( "TEMPORARY"))
			{
				remarks.add( new QueryRemark<String>(this, QueryRemark.RemarkType.OTHER, row.table, "Query requires temporary table to fetch results!"));
			}
		}
		return remarks;
	}

	private static class ResultRow
	{
		final String selectType;
		final String table;
		final String type;
		final String key;
		final String extra;

		ResultRow( String selectType, String table, String type, String key, String extra )
		{
			this.selectType = selectType;
			this.table = table;
			this.type = type;
			this.key = key;
			this.extra = extra;
		}
	}
}
