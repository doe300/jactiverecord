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
package de.doe300.activerecord.migration;

import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.store.RecordStore;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class MigrationTest extends TestBase
{
	private final static String MAPPING_TABLE_NAME = "mappingTableMigrationTest";
	private final Migration automaticMigration;
	private final Migration manualMigration;
	
	public MigrationTest(final RecordCore core)
	{
		super(core);
		
		final RecordStore store = core.getStore();
		Map<String, Class<?>> columns = new HashMap<>(2);
		columns.put( "fk_test1", Integer.class);
		columns.put( "fk_test2", Integer.class);
		automaticMigration = store.getDriver().createMigration( TestInterface.class, MigrationTest.class.getSimpleName(), store);
		manualMigration = store.getDriver().createMigration( MAPPING_TABLE_NAME, columns, store);
	}
	
	@Test
	public void testApply() throws Exception
	{
		assertTrue( automaticMigration.apply());
		assertFalse( automaticMigration.apply());
		assertTrue(manualMigration.apply(  ));
	}
	
	@Test
	public void testUpdate() throws Exception
	{
		assertFalse( automaticMigration.update( true ));
		assertFalse(manualMigration.update(true ));
	}

	@Test
	public void testRevert() throws Exception
	{
		assertTrue(automaticMigration.revert());
		assertFalse( automaticMigration.revert());
		assertTrue(manualMigration.revert());
	}
}