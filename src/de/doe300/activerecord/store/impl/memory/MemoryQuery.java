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

package de.doe300.activerecord.store.impl.memory;

import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.diagnostics.Query;
import de.doe300.activerecord.store.diagnostics.QueryRemark;
import de.doe300.activerecord.util.Pair;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation for a {@link Query} for the {@link MemoryRecordStore}
 *
 * @author doe300
 * @since 0.8
 */
class MemoryQuery extends Query<Pair< String, Scope>>
{
	MemoryQuery(@Nonnull final MemoryRecordStore store, @Nonnull final Pair< String, Scope> source, @Nullable final String storeName, @Nonnegative final long duration )
	{
		super(store, source,storeName, duration );
	}

	@Override
	public Iterable<String> explainQuery() throws Exception, UnsupportedOperationException
	{
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<QueryRemark<Pair< String, Scope>>> getRemarks() throws Exception, UnsupportedOperationException
	{
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

}
