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
import de.doe300.activerecord.store.diagnostics.Query;
import de.doe300.activerecord.store.diagnostics.QueryRemark;
import java.util.List;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation for a {@link Query} for JDBC drivers
 *
 * @author doe300
 * @since 0.8
 */
public abstract class JDBCQuery extends Query<String>
{
	private List<String> explainResult;
	private List<QueryRemark<String>> remarks;
	
	/**
	 *
	 * @param store the RecordStore this Query was run on
	 * @param source the originating SQL command
	 * @param storeName the name of the table 
	 * @param duration the duration of this query
	 */
	public JDBCQuery(@Nonnull final JDBCRecordStore store, @Nonnull final String source, @Nullable final String storeName, @Nonnegative final long duration )
	{
		super(store, source, storeName, duration);
	}

	@Override
	public Iterable<String> explainQuery() throws Exception, UnsupportedOperationException
	{
		if(explainResult == null)
		{
			explainResult = runExplain( source);
		}
		return explainResult;
	}
	
	/**
	 *
	 * @param sqlStatment
	 * @return
	 * @throws Exception
	 */
	protected abstract List<String> runExplain(@Nonnull final String sqlStatment) throws Exception;

	@Override
	public Iterable<QueryRemark<String>> getRemarks() throws Exception, UnsupportedOperationException
	{
		if(remarks == null)
		{
			remarks = createRemarks( (List<String>)explainQuery());
		}
		return remarks;
	}

	//TODO create remarks for ResultSet/original statment, not output

	/**
	 *
	 * @param explainResult
	 * @return
	 * @throws UnsupportedOperationException
	 */
	protected abstract List<QueryRemark<String>> createRemarks(@Nonnull final List<String> explainResult) throws UnsupportedOperationException;
}
