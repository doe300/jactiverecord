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
package de.doe300.activerecord.record.attachment;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.pojo.AbstractActiveRecord;
import de.doe300.activerecord.pojo.POJOBase;
import java.io.InputStream;
import java.net.URI;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class URLAttachmentHandlerTest extends Assert
{
	private static RecordBase<TestURLAttachmentRecord> base;
	
	public URLAttachmentHandlerTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		Attachments.registerHandler( TestURLAttachmentRecord.class, new URLAttachmentHandler((HasAttachment record) ->
				((TestURLAttachmentRecord)record).getAttachment() == null ? null : URI.create( ((TestURLAttachmentRecord)record).getAttachment()).toURL()));
		
		base = TestServer.getTestCore().getBase( TestURLAttachmentRecord.class);
		base.getCore().createTable( TestURLAttachmentRecord.class);
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		base.getCore().dropTable( TestURLAttachmentRecord.class);
	}

	@Test
	public void testAttachmentIO() throws Exception
	{
		TestURLAttachmentRecord r = base.createRecord();
		assertFalse( r.attachmentExists());
		r.setAttachment( "http://docs.oracle.com/javase/tutorial/reallybigindex.html");
		assertTrue( r.attachmentExists());
		try(final InputStream is = r.readAttachment())
		{
			assertNotNull( is);
		}
		assertFalse(r.removeAttachment());
		r.setAttachment( null);
		assertFalse( r.attachmentExists());
	}
	
	public static class TestURLAttachmentRecord extends AbstractActiveRecord implements HasAttachment
	{
		public TestURLAttachmentRecord( int primaryKey,POJOBase<?> base )
		{
			super( primaryKey, base );
		}

		@Override
		@Attribute(name = "attachment")
		public String getAttachmentColumn()
		{
			return "attachment";
		}
		
		public String getAttachment()
		{
			return getProperty( "attachment", String.class);
		}
		
		public void setAttachment(final String att)
		{
			setProperty( "attachment", att);
		}
	}
}
