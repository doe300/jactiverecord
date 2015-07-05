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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author doe300
 */
public class FileAttachmentHandler implements AttachmentHandler
{
	private final Path rootDirectory;

	/**
	 * @param rootDirectory The root directory to find all attachments inside
	 */
	public FileAttachmentHandler( Path rootDirectory )
	{
		this.rootDirectory = rootDirectory;
	}

	@Override
	public boolean attachmentExists( HasAttachment record )
	{
		return Files.exists( getAttachmentPath( record));
	}

	@Override
	public InputStream readAttachment( HasAttachment record ) throws IOException
	{
		return Files.newInputStream( getAttachmentPath( record));
	}

	@Override
	public OutputStream writeAttachment( HasAttachment record ) throws IOException
	{
		return Files.newOutputStream( getAttachmentPath( record));
	}

	/**
	 * By default, this method will look for attachments according to the following file-hierarchy:
	 * <p>
	 * <code>&lt;root-path&gt;/&lt;record type-name&gt;/&lt;attachment-column&gt;&lt;primary-key&gt;</code>
	 * </p>
	 * @param record
	 * @return the Path for requested attachment
	 */
	protected Path getAttachmentPath(HasAttachment record)
	{
		return rootDirectory.resolve( record.getClass().getSimpleName()).resolve( record.getAttachmentColumn()+record.getPrimaryKey());
	}
}
