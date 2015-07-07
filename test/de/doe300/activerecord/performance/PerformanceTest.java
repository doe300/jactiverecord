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
package de.doe300.activerecord.performance;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.migration.AutomaticMigration;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author doe300
 * @param <T>
 */
@RunWith(Parameterized.class)
public class PerformanceTest<T extends ProxyPerformance> extends Assert
{
	private static RecordCore core;
	private final RecordBase<T> base;

	public PerformanceTest(RecordBase<T> base)
	{
		this.base = base;
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParams() throws SQLException
	{
		core = RecordCore.fromDatabase( TestServer.getTestConnection(), true);
		return Arrays.asList(
				new Object[][]
				{
					{core.buildBase( ProxyPerformance.class)},
					{core.buildBase(POJOPerformance.class)},
					{core.buildBase( CachingPOJOPerformance.class)}
				}
		);
	}
	
	@Test
	public void testPerformance() throws SQLException
	{
		new AutomaticMigration(base.getRecordType(), false).apply( core.getStore().getConnection());
		long[] times = new long[10];
		
		for(int i = 0; i < 1000; i++)
		{
			long t0 = System.nanoTime();
			T p = base.createRecord();
			long t1 = System.nanoTime();
			assertNull( p.getName());
			assertNull( p.getOther());
			long t2 = System.nanoTime();
			T prev = i == 0 ? null : base.getRecord( i-1);
			long t3 = System.nanoTime();
			p.setName( "Adam"+i);
			if(i > 0)
			{
				p.setOther( prev );
			}
			long t4 = System.nanoTime();
			for(int k = 0; k < 100; k++)
			{
				assertNotNull( p.getName());
				assertEquals( prev, p.getOther());
			}
			long t5 = System.nanoTime();
			p.setName( "Hans"+i);
			for(int k = 0; k < 200; k++)
			{
				assertEquals( "Hans"+i, p.getName());
				assertEquals( prev, p.getOther());
			}
			long t6 = System.nanoTime();
			assertSame( p, base.findFirstFor( "name", "Hans"+i));
			long t7 = System.nanoTime();
			times[0] += t1 - t0;
			times[1] += t2 - t1;
			times[2] += t3 - t2;
			times[3] += t4 - t3;
			times[4] += t5 - t4;
			times[5] += t6 - t5;
			times[6] += t7 - t6;
		}
		System.out.printf( "%25s: %10s|%10s|%10s|%10s|%10s|%10s|%10s\n", "Name", "Create", "Access 1", "Find prev", "Set other", "Access 2", "Acces 3", "Find" );
		System.out.printf( "%25s: %10d|%10d|%10d|%10d|%10d|%10d|%10d\n", base.getRecordType().getSimpleName(), times[0], times[1], times[2], times[3], times[4], times[5], times[6] );
	}
}
