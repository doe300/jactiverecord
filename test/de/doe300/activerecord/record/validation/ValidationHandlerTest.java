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

package de.doe300.activerecord.record.validation;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class ValidationHandlerTest extends TestBase
{
	private final RecordBase<TestInterface> base;
	private final RecordBase<ValidationTestRecord> testBase;
	
	public ValidationHandlerTest(final RecordCore core)
	{
		super(core);
		base = core.getBase(TestInterface.class).getShardBase( ValidationHandlerTest.class.getSimpleName());
		testBase = core.getBase( ValidationTestRecord.class);
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables(TestInterface.class, ValidationHandlerTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestInterface.class, ValidationHandlerTest.class.getSimpleName());
	}

	@Test
	public void testIsValid()
	{
		TestInterface i = base.createRecord();
		assertFalse( i.isValid());
		i.setAge( 23);
		i.setName( "Adfan");
		assertTrue( i.isValid());
		
		ValidationTestRecord r = testBase.createRecord();
		assertFalse( r.isValid());
		r.setName( "");
		assertFalse( r.isValid());
		r.setName( "Eve");
		assertFalse( r.isValid());
		r.setName( "Adam");
		assertTrue( r.isValid());
	}
	
	@Test
	public void testValidate()
	{
		TestInterface i = base.createRecord();
		i.setAge( 23);
		i.setName( "Adam");
		i.validate();
		i.setAge(-100 );
		i.validate();
		
		ValidationTestRecord r = testBase.createRecord();
		assertThrows( ValidationException.class, () -> r.validate());
		r.setName( "");
		assertThrows( ValidationException.class, () -> r.validate());
		r.setName( "Eve");
		assertThrows( ValidationException.class, () -> r.validate());
		r.setName( "Adam");
		r.validate();
	}
	
	@Validates({
			@Validate(attribute = "name", type = ValidationType.NOT_NULL),
			@Validate(attribute = "name", type = ValidationType.NOT_EMPTY),
			@Validate(attribute = "name", type = ValidationType.CUSTOM, customClass = ValidationTestRecord.class, customMethod = "testName")
	})
	@RecordType(autoCreate = true, defaultColumns = ActiveRecord.DEFAULT_PRIMARY_COLUMN, typeName = "validationTest")
	public interface ValidationTestRecord extends ValidatedRecord
	{
		public void setName(String name);
		public String getName();
		
		public static boolean testName(final Object object)
		{
			return object != null && object instanceof String && object.equals( "Adam");
		}
	}
}
