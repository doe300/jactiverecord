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
package de.doe300.activerecord.proxy;

import de.doe300.activerecord.AssertException;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.validation.ValidatedRecord;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class RecordHandlerTest extends TestBase implements AssertException
{
	private final RecordHandler<TestInterface> handler;
	private final RecordBase<TestInterface> base;
	private final TestInterface testI;
	
	public RecordHandlerTest(final RecordCore core)
	{
		super(core);
		
		base = core.getBase( TestInterface.class).getShardBase( RecordHandlerTest.class.getSimpleName());
		testI = base.createRecord();
		handler = new RecordHandler<TestInterface>(testI.getPrimaryKey(), base);
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, RecordHandlerTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, RecordHandlerTest.class.getSimpleName());
	}
	
	@Test
	public void testInvokeStandardMethods() throws Exception, Throwable
	{
		Method getPrimaryKey, getRecordBase, hashCode, toString, equals;
		getPrimaryKey = ActiveRecord.class.getMethod( "getPrimaryKey");
		getRecordBase = ActiveRecord.class.getMethod( "getBase");
		hashCode = Object.class.getMethod( "hashCode");
		toString = Object.class.getMethod( "toString");
		equals = Object.class.getMethod( "equals", Object.class);
		
		assertEquals( testI.getPrimaryKey(), handler.invoke( testI, getPrimaryKey,null));
		assertEquals( base, handler.invoke( testI, getRecordBase, null));
		assertNotNull( handler.invoke( testI, hashCode, null));
		assertNotNull( handler.invoke( testI, toString, null));
		assertFalse(( boolean ) handler.invoke( testI, equals, null));
		assertTrue((boolean) handler.invoke( testI, equals, new Object[]{testI}));
	}
	
	@Test
	public void testInvokeDefaultMethods() throws NoSuchMethodException, Throwable
	{
		Method df1= TestInterface.class.getMethod( "checkName", Object.class), df2 = ValidatedRecord.class.getMethod( "isValid");
		assertFalse( (boolean)handler.invoke( testI, df2, null));
		handler.invoke( testI, df1, new Object[]{"Test"});
		handler.invoke( testI, df2, null);
	}
	
	@Test 
	public void testInvokeAttributeAccessor() throws NoSuchMethodException, Throwable
	{
		Method m1 = TestInterface.class.getMethod( "setName", String.class);
		Method m2 = TestInterface.class.getMethod( "getOther");
		assertThrows( InvocationTargetException.class, () -> handler.invoke( testI, m1, new Object[]{null}));
		assertThrows( InvocationTargetException.class, () -> handler.invoke( testI, m2, null));
	}
	
	@Test
	public void testInvokeBeanStyleAccessor() throws Throwable
	{
		Method m1 = TestInterface.class.getMethod( "setAge", Integer.TYPE), m2 = TestInterface.class.getMethod( "getAge");
		handler.invoke( testI, m1, new Object[]{12});
		assertEquals( 12, testI.getAge());
		assertEquals( 12, handler.invoke( testI, m2, null));
	}
	
	@Test
	public void testInvokeMethodNotHandled() throws NoSuchMethodException, Throwable
	{
		Method m = String.class.getMethod( "length");
		assertThrows( NoSuchMethodException.class, () -> handler.invoke( testI, m, null ));
	}
	
	@Test
	public void testInvokeErrors()
	{
		try
		{
			handler.invoke( "5", null, null );
		}
		catch ( Throwable ex )
		{
			assertTrue( ex instanceof IllegalArgumentException);
		}
	}

	public void testGetRecordType()
	{
		assertEquals( TestInterface.class, handler.getRecordType());
	}
}
