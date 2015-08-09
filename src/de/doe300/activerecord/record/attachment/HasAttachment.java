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

import de.doe300.activerecord.record.ActiveRecord;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nonnull;

/**
 * ActiveRecord which provides has some kind of attached resource
 * @author doe300
 */
public interface HasAttachment extends ActiveRecord
{
	/**
	 * @return the associated attachment-handler
	 */
	@Nonnull
	public default AttachmentHandler getAttachmentHandler()
	{
		return Attachments.getHander( getClass());
	}
	
	/**
	 * @return the name of the column storing the attachment-key
	 */
	public String getAttachmentColumn();
	
	/**
	 * @return whether a attachment exists for this record
	 * @see AttachmentHandler#attachmentExists(de.doe300.activerecord.record.attachment.HasAttachment) 
	 */
	public default boolean attachmentExists()
	{
		return getAttachmentHandler().attachmentExists( this );
	}
	
	/**
	 * @return an InputStream to the attachment
	 * @throws IOException if no such attachment exists or an IO error occurs
	 * @see AttachmentHandler#readAttachment(de.doe300.activerecord.record.attachment.HasAttachment) 
	 */
	@Nonnull
	public default InputStream readAttachment() throws IOException
	{
		return getAttachmentHandler().readAttachment( this );
	}
	
	/**
	 * @return an OutputStream to the attachment
	 * @throws IOException if no such attachment exists and is not created or any other IO error occurs
	 * @see AttachmentHandler#writeAttachment(de.doe300.activerecord.record.attachment.HasAttachment) 
	 */
	@Nonnull
	public default OutputStream writeAttachment() throws IOException
	{
		return getAttachmentHandler().writeAttachment( this );
	}
}
