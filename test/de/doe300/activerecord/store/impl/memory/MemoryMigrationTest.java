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
package de.doe300.activerecord.store.impl.memory;

import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author doe300
 */


public class MemoryMigrationTest extends Assert
{
	private final MemoryMigration testAutomaticMemoryMigration;
	private final MemoryMigration testManualMemoryMigration;
	
	public MemoryMigrationTest() throws SQLException
	{
		testAutomaticMemoryMigration = new MemoryMigration(( MemoryRecordStore ) TestServer.getTestCore(MemoryRecordStore.class).getStore(), TestInterface.class, false);
		testManualMemoryMigration = new MemoryMigration(( MemoryRecordStore ) TestServer.getTestCore(MemoryRecordStore.class).getStore(), "mappingTable", new MemoryColumn[]
		{
			new MemoryColumn("fk_test1", Integer.class),
			new MemoryColumn("fk_test2", Integer.class),
			new MemoryColumn("info", String.class)
		}, "id");
	}

	@Test
	public void testApply()
	{
		assertTrue( testAutomaticMemoryMigration.apply( null ));
		assertTrue( testManualMemoryMigration.apply( null));
	}

	@Test
	public void testRevert()
	{
		assertTrue( testAutomaticMemoryMigration.revert( null));
		assertTrue( testManualMemoryMigration.revert( null));
	}

}
