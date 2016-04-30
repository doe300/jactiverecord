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
import java.net.URL;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author doe300
 */
public class URLAttachmentHandler implements AttachmentHandler
{
	private final URLConverter pathFunc;

	/**
	 * @param pathFunc
	 */
	public URLAttachmentHandler( final URLConverter pathFunc )
	{
		this.pathFunc = pathFunc;
	}

	@Override
	public boolean attachmentExists( final HasAttachment record )
	{
		try
		{
			if(pathFunc.getAttachmentPath(record ) == null)
			{
				return false;
			}
			final URL attachmentPath = pathFunc.getAttachmentPath(record );
			if(attachmentPath != null && attachmentPath.openConnection().getContentType() != null)
			{
				//could connect and read content-type
				return true;
			}
			return false;
		}
		catch ( final IOException ex )
		{
			return false;
		}
	}

	@Override
	public InputStream readAttachment( final HasAttachment record ) throws IOException
	{
		return Objects.requireNonNull( pathFunc.getAttachmentPath(record ), "Can't read null attachment").openConnection().getInputStream();
	}

	@Override
	public OutputStream writeAttachment( final HasAttachment record ) throws IOException
	{
		return Objects.requireNonNull( pathFunc.getAttachmentPath( record ), "Can't write into null attachment").openConnection().getOutputStream();
	}

	@Override
	public boolean removeAttachment( HasAttachment record ) throws IOException
	{
		//can't delete URLs
		return false;
	}

	/**
	 * A function providing the path to an attachment while allowing exceptions to be thrown
	 */
	public static interface URLConverter
	{
		/**
		 * Maps the HasAttachment to the URL containing the attachment
		 * @param record
		 * @return the URL for the attachment
		 * @throws IOException if any IO error occurs
		 */
		@Nullable
		public URL getAttachmentPath(@Nonnull final HasAttachment record) throws IOException;
	}
}
