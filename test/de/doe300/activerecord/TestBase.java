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

package de.doe300.activerecord;

import static de.doe300.activerecord.TestServer.getTestCore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Common base class for tests
 * @author doe300
 * @since 0.8
 */
@RunWith(Parameterized.class)
public abstract class TestBase extends Assert implements AssertException
{
	protected TestBase(final RecordCore core)
	{
		//this constructor just guarantees the child to accept a parameter of type RecordCore
	}
	
	@Parameterized.Parameters
	@Nonnull
	public static Collection<Object[]> getRecordCores() throws SQLException
	{
		return Arrays.asList(
				new Object[]{getTestCore( SimpleJDBCRecordStore.class)},
				new Object[]{getTestCore( CachedJDBCRecordStore.class)},
				new Object[]{getTestCore( MemoryRecordStore.class)}
		);
	}	
}
