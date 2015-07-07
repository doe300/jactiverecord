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
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
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
	private final RecordBase<T> base;

	public PerformanceTest(Class<T> type, boolean cached) throws SQLException
	{
		this.base = RecordCore.fromDatabase( TestServer.getTestConnection(), cached).buildBase( type);
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParams() throws SQLException
	{
		return Arrays.asList(
				new Object[][]
				{
					{ProxyPerformance.class, false},
					{POJOPerformance.class, false},
					{CachingPOJOPerformance.class,false},
					{ProxyPerformance.class,true},
					{POJOPerformance.class,true},
					{CachingPOJOPerformance.class,true}
				}
		);
	}
	
	@Test
	public void testPerformance() throws SQLException, Exception
	{
		AutomaticMigration mig = new AutomaticMigration(base.getRecordType(), false);
		mig.apply( base.getStore().getConnection());
		long[] times = new long[10];
		
		for(int i = 0; i < 500; i++)
		{
			long t0 = System.nanoTime();
			T p = base.createRecord();
			long t1 = System.nanoTime();
			for(int k = 0; k < 50; k++)
			{
				assertNull( p.getName());
			}
			long t2 = System.nanoTime();
			for(int k = 0; k < 100; k++)
			{
				assertNull( p.getOther());
			}
			long t3 = System.nanoTime();
			T prev = i == 0 ? null : base.getRecord( i-1);
			long t4 = System.nanoTime();
			for(int k = 0; k < 100; k++)
			{
				p.setName( "Adam"+i);
				if(i > 0)
				{
					p.setOther( prev );
				}
			}
			long t5 = System.nanoTime();
			for(int k = 0; k < 100; k++)
			{
				assertNotNull( p.getName());
			}
			long t6 = System.nanoTime();
			for(int k = 0; k < 100; k++)
			{
				assertEquals( prev, p.getOther());
			}
			long t7 = System.nanoTime();
			p.setName( "Hans"+i);
			for(int k = 0; k < 200; k++)
			{
				assertEquals( "Hans"+i, p.getName());
			}
			long t8 = System.nanoTime();
			for(int k = 0; k < 200; k++)
			{
				assertEquals( prev, p.getOther());
			}
			long t9 = System.nanoTime();
			assertSame( p, base.findFirstFor( "name", "Hans"+i));
			long t10 = System.nanoTime();
			times[0] += t1 - t0;	//Create
			times[1] += (t2 - t1) + (t6 - t5) + (t8 - t7);	//Access "name"
			times[2] += (t3 - t2) + (t7 - t6) + (t9 - t8);	//Access "other"
			times[3] += t4 - t3;	//Find prev
			times[4] += t5 - t4;	//Set fields
			times[5] += t10 - t9;	//Find
		}
		System.out.printf( "%33s: %12s|%12s|%12s|%12s|%12s|%12s\n", "Name", "Create", "Get 'name'", "Get 'other'", "Find prev", "Set atts", "Find" );
		System.out.printf( "%33s: %12d|%12d|%12d|%12d|%12d|%12d\n", base.getRecordType().getSimpleName() + (base.getStore() instanceof CachedJDBCRecordStore ? " (cached)" : " (plain)"),
				times[0], times[1], times[2], times[3], times[4], times[5] );
		
		//clean up
		mig.revert( base.getStore().getConnection());
		base.getCore().close();
	}
}
