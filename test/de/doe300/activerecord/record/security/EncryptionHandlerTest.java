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
package de.doe300.activerecord.record.security;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.migration.Attribute;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author doe300
 * @since 0.6
 */
public class EncryptionHandlerTest extends TestBase
{
	private final RecordBase<TestEncryptedRecord> base;
	private final TestEncryptedRecord record;
	
	public EncryptionHandlerTest(final RecordCore core) throws GeneralSecurityException
	{
		super(core);

		final EncryptionAlgorithm algrithm = new CipherEncryptionWrapper("DES/ECB/NoPadding", SecretKeyFactory.getInstance( "DES").generateSecret( new DESKeySpec("Hallo !!".getBytes())));		
		base = core.getBase( TestEncryptedRecord.class, new EncryptionHandler(algrithm ));
		record = base.createRecord();
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTables( TestEncryptedRecord.class, "TestEncryptedRecord" );
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTables(TestEncryptedRecord.class, "TestEncryptedRecord" );
	}
	
	public static interface TestEncryptedRecord extends EncryptedRecord
	{
		@Attribute(name = "name", type = Byte[].class)
		@EncryptedAttribute(attribute = "name")
		public String getName();
		
		@EncryptedAttribute(attribute = "name")
		public void setName(String name);
		
		@Attribute(name = "errorColumn", type = Integer.class)
		@EncryptedAttribute(attribute = "errorColumn")
		public String getWrongValue();
		
		@EncryptedAttribute(attribute = "errorColumn")
		public void setWrongValue(String val);
	}

	@Test
	public void testEncryptedAttribute()
	{
		assertTrue(record.getEncryptionAlgorithm() instanceof CipherEncryptionWrapper);
		assertNull( record.getName());
		
		record.setName( "AdamAdam");
		assertEquals( "AdamAdam", record.getName());
		
		record.setName( "Eve");
		assertEquals( "Eve", record.getName());
		
		record.setName( null);
		assertNull( record.getName());
	}
	
	@Test
	public void testErrors()
	{
		assertThrows( IllegalArgumentException.class, () -> record.setWrongValue("Eden"));
	}
}
