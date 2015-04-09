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
package de.doe300.activerecord.profiling;

import de.doe300.activerecord.profiling.ProfilingRecordStore;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ProfilingRecordStoreTest extends Assert
{
	private final ProfilingRecordStore recordStore;
	private final RecordBase<TestInterface> base;
	private final String name;
	
	public ProfilingRecordStoreTest(String name, RecordStore store)
	{
		this.name = name;
		this.recordStore = new ProfilingRecordStore(store );
		base = RecordCore.fromStore( name, recordStore ).buildBase( TestInterface.class);
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() throws SQLException
	{
		return Arrays.asList(
			new Object[]{"TestSimple", new SimpleJDBCRecordStore(new ProfilingConnection(TestServer.getTestConnection()))},
			new Object[]{"TestCache", new CachedJDBCRecordStore(new ProfilingConnection(TestServer.getTestConnection()))}
		);
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables();
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testPerformance()
	{
		//TODO run several methods
		for(int i = 0; i< 100; i++)
		{
			TestInterface t = base.createRecord(Collections.singletonMap( "name", "ThisIsAName"));
			t.setAge( 213);
			t.getName();
			t.save();
			t.setDirectionOne( t);
			t.getDirectionOther();
			t.setName( "AnotherName");
			t.save();
			t.getTestEnum();
			t.isSynchronized();
			t.getPrimaryKey();
			t.reload();
			t.setAge( 212);
			t.setName( "NextName");
			t.save();
			t.inRecordStore();
			t.isSynchronized();
			//FIXME touch throws 2 errors!!!
//			t.touch();
			t.validate();
			t.destroy();
		}

		System.out.println( name );
		recordStore.printStatistics();
		System.out.println(  );
		((ProfilingConnection)recordStore.getConnection()).printStatistics();
		System.out.println(  );
		System.out.println(  );
	}
}
