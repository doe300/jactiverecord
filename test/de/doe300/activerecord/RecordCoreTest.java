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
package de.doe300.activerecord;

import de.doe300.activerecord.proxy.handlers.MapHandler;
import de.doe300.activerecord.store.impl.MapRecordStore;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class RecordCoreTest extends Assert
{
	private static RecordCore core;
	
	public RecordCoreTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables();
		core = RecordCore.fromDatabase( TestServer.getTestConnection(), true);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables();
		core.close();
	}
	
	@Test
	public void testFromDatabase() throws Exception
	{
		assertNotNull( RecordCore.fromDatabase( TestServer.getTestConnection(), false));
	}

	@Test
	public void testNewMemoryStore() throws Exception
	{
		assertNotNull( RecordCore.newMemoryStore( "test1"));
	}

	@Test
	public void testFromStore() throws Exception
	{
		assertNotNull( RecordCore.fromStore( "test2", new MapRecordStore()));
	}

	@Test
	public void testGetCore()
	{
		assertNotNull( RecordCore.getCore( "PUBLIC"));
	}

	@Test
	public void testBuildBase()
	{
		assertNotNull( core.buildBase( TestInterface.class, new MapHandler()) );
		assertEquals(core.buildBase( TestInterface.class), core.getBase( TestInterface.class));
	}

	@Test
	public void testGetBase()
	{
		assertNotNull( core.getBase( TestPOJO.class));
	}
}
