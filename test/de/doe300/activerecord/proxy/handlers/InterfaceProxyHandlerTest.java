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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.proxy.RecordHandler;

/**
 *
 * @author daniel
 */
public class InterfaceProxyHandlerTest extends Assert
{
	private static DummyInterfaceProxyHandler handler;
	
	public InterfaceProxyHandlerTest()
	{
	}
	
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		InterfaceProxyHandlerTest.handler = new DummyInterfaceProxyHandler();
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testInvoke()
	{
	}

	@Test
	public void testHandlesMethod()
	{
	}

	static class DummyInterfaceProxyHandler extends InterfaceProxyHandler<TestInterface>
	{

		DummyInterfaceProxyHandler()
		{
			super( TestInterface.class );
		}
		
		public TestInterface getDirectionOne(final TestInterface record, final RecordHandler<TestInterface> handler)
		{
			return record;
		}
		
		public void setDirectionOne(final TestInterface record, final RecordHandler<TestInterface> handler, final TestInterface otherRecord)
		{
		}
	}
}
