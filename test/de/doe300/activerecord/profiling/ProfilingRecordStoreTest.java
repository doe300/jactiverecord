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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.record.association.AssociationHelper;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.store.impl.CachedJDBCRecordStore;
import de.doe300.activerecord.store.impl.SimpleJDBCRecordStore;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ProfilingRecordStoreTest extends Assert
{
	private static final String mappingTable = "mappingTable" + ProfilingRecordStoreTest.class.getSimpleName();
	private final RecordStore originalStore;
	private final ProfilingRecordStore recordStore;
	private final ProfilingRecordBase<TestInterface> base;
	private final String name;
	
	public ProfilingRecordStoreTest(String name, RecordStore store) throws Exception
	{
		this.name = name;
		originalStore = store;
		this.recordStore = new ProfilingRecordStore(store );
		final RecordCore core = RecordCore.fromStore( name, recordStore );
		final RecordBase<TestInterface> origBase = core.getBase( TestInterface.class).getShardBase( ProfilingRecordStoreTest.class.getSimpleName());
		base = new ProfilingRecordBase<>(origBase);
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
	
	@Test
	public void testPerformance() throws Exception
	{
		TestServer.buildTestMappingTable(originalStore, mappingTable );
		TestServer.buildTestTable(originalStore, TestInterface.class, ProfilingRecordStoreTest.class.getSimpleName());
		for(int i = 0; i< 200; i++)
		{
			TestInterface t = base.createRecord(Collections.singletonMap( "name", "ThisIsAName"));
			t.setAge( 213);
			assertEquals("ThisIsAName", t.getName());
			t.save();
			t.setDirectionOne( t);
			//assertSame(t, t.getDirectionOther());
			t.setName( "AnotherName");
			t.save();
			t.getTestEnum();
			assertTrue(t.isSynchronized());
			AssociationHelper.addHasManyThrough( t, t, mappingTable, "fk_test1", "fk_test2");
			AssociationHelper.addHasManyThrough( t, t, mappingTable, "fk_test2", "fk_test1");
			t.getPrimaryKey();
			t.setAge( 212);
			t.setName( "NextName");
			t.save();
			assertTrue(t.inRecordStore());
			assertTrue(t.isSynchronized());
			t.touch();
			AssociationHelper.getHasManyThroughSet( t, TestInterface.class, mappingTable, "fk_test1", "fk_test2");
			AssociationHelper.getHasManySet(t, TestInterface.class, "fk_test_id");
			assertEquals(t, AssociationHelper.getBelongsTo( t, base, "fk_test_id"));
			assertEquals(t, AssociationHelper.getHasOne( t.getPrimaryKey(), base, "fk_test_id"));
			t.validate();
			assertEquals(1, t.getBase().count( Conditions.is(base.getPrimaryColumn(), t.getPrimaryKey())));
			
			assertNotNull(t.getBase().findFirstFor( "name", "NextName"));
			base.find( Conditions.is("age", 212)).forEach( (TestInterface ti) -> ti.touch());
			
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
		
		TestServer.destroyTestTable(originalStore, TestInterface.class, ProfilingRecordStoreTest.class.getSimpleName());
		TestServer.destroyTestMappingTable(originalStore, mappingTable);
	}
}
