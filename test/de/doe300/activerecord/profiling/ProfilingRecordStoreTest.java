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

import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.record.association.AssociationHelper;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ProfilingRecordStoreTest extends Assert
{
	private final ProfilingRecordStore recordStore;
	private final ProfilingRecordBase<TestInterface> base;
	private final String name;
	
	//TODO output is not correct, doesn't print anything for cached/memory
	
	public ProfilingRecordStoreTest(String name, RecordStore store) throws Exception
	{
		this.name = name;
		this.recordStore = new ProfilingRecordStore(store );
		base = new ProfilingRecordBase<>(RecordCore.fromStore( name, recordStore ).getBase( TestInterface.class));
		if(store instanceof MemoryRecordStore)
		{
			base.getCore().createTable( TestInterface.class);
			Map<String, Class<?>> columns = new HashMap<>(5);
			columns.put("id", Integer.class);
			columns.put("fk_test1", Integer.class);
			columns.put("fk_test2", Integer.class);
			base.getStore().getDriver().createMigration( "MappingTable", columns, store).apply();
		}
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() throws SQLException
	{
		return Arrays.asList(
			new Object[]{"TestSimple", new SimpleJDBCRecordStore(new ProfilingConnection(TestServer.getTestConnection()))},
			new Object[]{"TestCache", new CachedJDBCRecordStore(new ProfilingConnection(TestServer.getTestConnection()))},
			new Object[]{"TestMemory", new MemoryRecordStore()}
		);
	}
	
	@Before
	public void setUp() throws Exception
	{
		TestServer.buildTestTables();
	}
	
	@After
	public void tearDown() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testPerformance()
	{
		for(int i = 0; i< 200; i++)
		{
			TestInterface t = base.createRecord(Collections.singletonMap( "name", "ThisIsAName"));
			t.setAge( 213);
			assertEquals("ThisIsAName", t.getName());
			t.save();
			t.setDirectionOne( t);
			assertSame(t, t.getDirectionOther());
			t.setName( "AnotherName");
			t.save();
			t.getTestEnum();
			assertTrue(t.isSynchronized());
			AssociationHelper.addHasManyThrough( t, t, "mappingTable", "fk_test1", "fk_test2");
			AssociationHelper.addHasManyThrough( t, t, "mappingTable", "fk_test2", "fk_test1");
			t.getPrimaryKey();
			t.reload();
			t.setAge( 212);
			t.setName( "NextName");
			t.save();
			assertTrue(t.inRecordStore());
			assertTrue(t.isSynchronized());
			t.touch();
			AssociationHelper.getHasManyThroughSet( t, TestInterface.class, "mappingTable", "fk_test1", "fk_test2");
			AssociationHelper.getHasManySet(t, TestInterface.class, "fk_test_id");
			assertEquals(t, AssociationHelper.getBelongsTo( t, TestInterface.class, "fk_test_id"));
			assertEquals(t, AssociationHelper.getHasOne( t, TestInterface.class, "fk_test_id"));
			t.validate();
			assertEquals(1, t.getBase().count( new SimpleCondition(base.getPrimaryColumn(), t.getPrimaryKey(), Comparison.IS)));
			
			assertNotNull(t.getBase().findFirstFor( "name", "NextName"));
			base.find( new SimpleCondition("age", 212, Comparison.IS)).forEach( (TestInterface ti) -> ti.touch());
			
			assertSame( t, base.getRecord( t.getPrimaryKey()));
			assertTrue( t.getBase().hasRecord( t.getPrimaryKey()));
			
			t.destroy();
			assertFalse( t.inRecordStore());
		}

		System.out.println( name.toUpperCase() );
		System.out.println( "RecordBase:" );
		base.getProfiler().printStatistics(true);
		System.out.println( "" );
		System.out.println( "RecordStore:" );
		recordStore.getProfiler().printStatistics(true);
		System.out.println(  );
		System.out.println( "Connection:" );
		System.out.println(  );
		System.out.println(  );
	}
}
