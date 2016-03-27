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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nonnull;

/**
 * Handles a set of attachments, e.g. stores them in files or the DB
 * 
 * NOTE: This handler does not manage the attribute containing the attachment-key!
 * 
 * @author doe300
 */
public interface AttachmentHandler
{
	/**
	 * @param record
	 * @return whether an attachment for this record exists
	 */
	public boolean attachmentExists(@Nonnull final HasAttachment record);
	
	/**
	 * 
	 * @param record
	 * @return an InputStream to read from this attachment
	 * @throws IOException if the attachment doesn't exists or any other IO error occurs
	 */
	 @Nonnull
	public InputStream readAttachment(@Nonnull final HasAttachment record) throws IOException;
	
	/**
	 * @param record
	 * @return an OutputStream to write to the attachment
	 * @throws IOException if the attachment can't be written or any other IO error occurs
	 */
	@Nonnull
	public OutputStream writeAttachment(@Nonnull final HasAttachment record) throws IOException;
	
	/**
	 * Removes the attachment for the given record by deleting the resource, etc.
	 * 
	 * @param record
	 * @return whether there was an attachment which was removed
	 * @throws IOException if any IO error occurs while deleting the attachment
	 * @since 0.7
	 */
	public boolean removeAttachment(@Nonnull final HasAttachment record) throws IOException;
}
