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

import de.doe300.activerecord.migration.ExcludeAttribute;
import de.doe300.activerecord.record.ActiveRecord;
import java.security.GeneralSecurityException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An ActiveRecord which encrypt several of its fields
 * @author doe300
 * @since 0.3
 */
public interface EncryptedRecord extends ActiveRecord
{
	/**
	 * @return the encryption-algorithm in use
	 */
	@Nonnull
	@ExcludeAttribute
	public EncryptionAlgorithm getEncryptionAlgorithm();
	
	/**
	 * Encrypts the given value
	 * @param rawValue the value to encrypt
	 * @return the encrypted result
	 * @throws GeneralSecurityException if any error occurs while encrypting
	 */
	@Nullable
	public default String encryptValue(@Nullable final String rawValue) throws GeneralSecurityException
	{
		if(rawValue == null)
		{
			return null;
		}
		return new String(getEncryptionAlgorithm().encryptValue( rawValue.getBytes() ));
	}
	
	/**
	 * Decrypts the given value
	 * @param encryptedValue the encrypted value to decrypt
	 * @return the plain-text result
	 * @throws GeneralSecurityException if any error occurs while decrypting
	 */
	@Nullable
	public default String decryptValue(@Nullable final String encryptedValue) throws GeneralSecurityException
	{
		if(encryptedValue == null)
		{
			return null;
		}
		return new String(getEncryptionAlgorithm().decryptValue( encryptedValue.getBytes() ));
	}
}
