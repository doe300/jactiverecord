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
package de.doe300.activerecord.store.diagnostics;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.dsl.Orders;
import de.doe300.activerecord.dsl.functions.Absolute;
import de.doe300.activerecord.dsl.functions.Floor;
import de.doe300.activerecord.dsl.functions.Maximum;
import de.doe300.activerecord.scope.Scope;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DiagnosticsTest extends TestBase
{
	private final RecordBase<TestInterface> base;
	private final Diagnostics<?> diagnostics;
	
	public DiagnosticsTest(@Nonnull final RecordCore core)
	{
		super(core);
		base = core.getBase( TestInterface.class).getShardBase( "DiagnosticsTest");
		diagnostics = base.getStore().getDiagnostics();
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables( TestInterface.class, "DiagnosticsTest");
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, "DiagnosticsTest");
	}

	@Test
	public void testSlowQueryThreshold()
	{
		diagnostics.setSlowQueryThreshold( -50);
		assertFalse( diagnostics.isSlowQueryLogEnabled());
		diagnostics.setSlowQueryThreshold( 100);
		assertEquals( 100, diagnostics.getSlowQueryThreshold());
		assertTrue( diagnostics.isSlowQueryLogEnabled());
	}

	@Test
	public void testProfileQuery()
	{
		diagnostics.setSlowQueryThreshold( 1);
		diagnostics.setSlowQueryListener((LoggedQuery<?> query) ->
		{
			assertTrue( query.getDuration() >= 1);
			System.out.println( query.store );
			System.out.println( query.getSource() );
			System.out.println( query.getDuration() );
			try
			{
				for(String line : query.explainQuery())
				{
					System.out.println( "\t" + line );
				}
				System.out.println(  );
				for(QueryRemark<?> remark : query.getRemarks())
				{
					System.out.println( "\t" + remark.type + " - " + remark.remark);
				}
			}
			catch(UnsupportedOperationException uoe)
			{
				//allowed to happen
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		});
		assertNotNull( diagnostics.getSlowQueryListener());
		
		final Map<String, Object> someData = new HashMap<>(2);
		someData.put( "name", "Adam");
		someData.put( "age", 12);
		for(int i = 0; i < 1000; i++)
		{
			base.createRecord( someData );
		}
		base.getStore().saveAll( base );
		base.findWithScope( new Scope(Conditions.or(Conditions.isLarger( base.getPrimaryColumn(), 0)), 
				Orders.combine( Orders.sortAscending( "name"), Orders.sortDescending( "age")), Scope.NO_LIMIT))
				.parallel().forEach( (i) -> {});
		assertTrue( base.getStore().getValues(base.getTableName(), "age", "name", "Adam" ).parallel().allMatch(Integer.valueOf( 12)::equals));
		assertEquals( 12, base.aggregate(new Maximum<>("age", TestInterface::getAge), Conditions.is( new Absolute<>("age", TestInterface::getAge), new Floor<>("age", TestInterface::getAge) )).intValue());
	}


	@Test
	public void testGetSlowQueryLog()
	{
		if(!diagnostics.getSlowQueryLog().isEmpty())
		{
			assertNotNull( diagnostics.getSlowQueryLog().getFirst());
			assertNotNull( diagnostics.getSlowQueryLog().pop());
		}
	}
	
}
