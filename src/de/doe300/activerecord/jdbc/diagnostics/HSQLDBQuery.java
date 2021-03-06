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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Implementation for {@link LoggedQuery} for HSQLDB driver
 *
 * @author doe300
 * @since 0.8
 */
public class HSQLDBQuery extends JDBCQuery
{

	public HSQLDBQuery(@Nonnull final JDBCRecordStore store,  String source, String storeName, long duration )
	{
		super( store, source, storeName, duration );
	}

	@Override
	protected List<String> runExplain( String sqlStatment ) throws Exception
	{
		try(final Statement stmt = ((JDBCRecordStore)store).getConnection().createStatement();
				final ResultSet result = stmt.executeQuery( "EXPLAIN PLAN FOR " + sqlStatment))
		{
			//HSQLDB EXPLAIN PLAN has only one column called OPERATION of type VARCHAR
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
		final Iterator<String> operations = explainQuery().iterator();
		
		while(operations.hasNext())
		{
			///TODO analyze operations and search for select without index, ...
			operations.next();
		}
		return Collections.emptyList();
	}
}
