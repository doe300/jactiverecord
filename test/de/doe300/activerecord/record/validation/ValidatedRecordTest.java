/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 doe300
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
package de.doe300.activerecord.record.validation;

import de.doe300.activerecord.AssertException;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class ValidatedRecordTest extends Assert implements AssertException
{
	private static RecordBase<DefaultValidatedRecord> defaultBase;
	private static RecordBase<CustomValidatedRecord> customBase;
	
	public ValidatedRecordTest()
	{
	}
	
	@BeforeClass
	public static void setUpClass() throws SQLException, Exception
	{
		final RecordCore core = TestServer.getTestCore( MemoryRecordStore.class);
		defaultBase = core.getBase( DefaultValidatedRecord.class);
		customBase = core.getBase( CustomValidatedRecord.class);

		core.getStore().getDriver().createMigration( DefaultValidatedRecord.class, core.getStore()).apply();
		core.getStore().getDriver().createMigration( CustomValidatedRecord.class, core.getStore()).apply();
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		defaultBase.getStore().getDriver().createMigration( DefaultValidatedRecord.class, defaultBase.getStore()).revert();
		customBase.getStore().getDriver().createMigration( CustomValidatedRecord.class, customBase.getStore()).revert();
	}

	@Test
	public void testIsValid()
	{
		final DefaultValidatedRecord d = defaultBase.createRecord();
		assertFalse( d.isValid());
		
		final CustomValidatedRecord c = customBase.createRecord();
		assertFalse( c.isValid());
		c.setName( "Eve");
		assertTrue( c.isValid());
		c.setName( null);
		assertFalse( c.isValid());
	}

	@Test
	public void testValidate()
	{
		final DefaultValidatedRecord d = defaultBase.createRecord();
		assertThrows( ValidationException.class, () -> d.validate());
		
		final CustomValidatedRecord c = customBase.createRecord();
		assertThrows( ValidationException.class, () -> c.validate());
		c.setName( "Eve");
		c.validate();
		c.setName( null);
		assertThrows( ValidationException.class, () -> c.validate());
	}

	public interface DefaultValidatedRecord extends ValidatedRecord
	{
	}
	
	public interface CustomValidatedRecord extends ValidatedRecord
	{
		@Override
		public default void validate() throws ValidationException
		{
			if(getName() == null)
			{
				throw new ValidationException("name", null, "Name may not be null!");
			}
		}

		@Override
		public default boolean isValid()
		{
			return getName() != null;
		}
		
		public void setName(String name);
		
		public String getName();
	}
}
