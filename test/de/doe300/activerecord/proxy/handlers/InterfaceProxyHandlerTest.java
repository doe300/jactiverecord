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
package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author doe300
 * @since 0.9
 */
public class InterfaceProxyHandlerTest extends TestBase
{
	private final RecordBase<TestSomethingInterface> base;
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestSomethingInterface.class, InterfaceProxyHandlerTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestSomethingInterface.class, InterfaceProxyHandlerTest.class.getSimpleName());
	}
	
	public InterfaceProxyHandlerTest(final RecordCore core)
	{
		super(core);
		base = core.getBase( TestSomethingInterface.class, new SomeInterfaceHandler()).getShardBase( InterfaceProxyHandlerTest.class.getSimpleName());
	}
	
	@Test
	public void testGetNameString()
	{
		final TestSomethingInterface t = base.createRecord();
		t.setName( "Eve");
		assertEquals( t.getName(), t.getNameString());
	}
	
	@Test
	public void testAdd2()
	{
		final TestSomethingInterface t = base.createRecord();
		t.setAge( 13);
		assertEquals( 2, t.add2( 0 ));
		assertEquals( t.getAge() + 2, t.add2( t.getAge()));
		
		assertEquals( 0, t.add2( 2, -2));
	}
	
	@Test
	public void testNotImplemented()
	{
		final TestSomethingInterface t = base.createRecord();
		assertThrows( RuntimeException.class, () -> t.notImplemented());
	}
	

	public class SomeInterfaceHandler extends InterfaceProxyHandler<SomeInterface>
	{

		SomeInterfaceHandler()
		{
			super( SomeInterface.class );
		}

		public String getNameString(final ActiveRecord record, final RecordHandler handler)
		{
			return ((TestSomethingInterface)record).getName();
		}
		
		public int add2(final ActiveRecord record, final RecordHandler handler, int value)
		{
			return value + 2;
		}
		
		public int add2(final ActiveRecord record, final RecordHandler handler, int v1, int v2)
		{
			return v1 + v2;
		}
	}
	
	public static interface SomeInterface
	{
		public String getNameString();
		
		public int add2(int value);
		
		public void notImplemented();
		
		public int add2(int v1, int v2);
	}
	
	@RecordType(typeName = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
	public static interface TestSomethingInterface extends TestInterface, SomeInterface
	{
		
	}
}
