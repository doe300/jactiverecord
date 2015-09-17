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

import de.doe300.activerecord.record.ActiveRecord;
import java.security.GeneralSecurityException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An ActiveRecord which encrypt several of its fields
 * @author doe300
 */
public interface EncryptedRecord extends ActiveRecord
{
	@Nonnull
	public EncryptionAlgorithm getEncryptionAlgorithm();
	
	@Nullable
	public default String encryptValue(@Nullable final String rawValue) throws GeneralSecurityException
	{
		return getEncryptionAlgorithm().encryptValue( rawValue );
	}
	
	@Nullable
	public default String decryptValue(@Nullable final String encryptedValue) throws GeneralSecurityException
	{
		return getEncryptionAlgorithm().decryptValue( encryptedValue );
	}
}
