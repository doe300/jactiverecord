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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.cert.Certificate;
import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

/**
 *
 * @author doe300
 * @since 0.3
 */
public class CipherEncryptionWrapper implements EncryptionAlgorithm
{
	private final Cipher encryptionCipher;
	private final Cipher decryptionCipher;

	public CipherEncryptionWrapper(@Nonnull final Cipher encryptionCipher, @Nonnull final Cipher decryptionCipher)
	{
		this.encryptionCipher = encryptionCipher;
		this.decryptionCipher = decryptionCipher;
	}

	public CipherEncryptionWrapper(@Nonnull final String transformation, @Nonnull final Key key) throws GeneralSecurityException
	{
		this.encryptionCipher = Cipher.getInstance( transformation);
		this.decryptionCipher = Cipher.getInstance( transformation );
		encryptionCipher.init( Cipher.ENCRYPT_MODE, key);
		decryptionCipher.init( Cipher.DECRYPT_MODE, key);
	}
	
	public CipherEncryptionWrapper(@Nonnull final String transformation, @Nonnull final Certificate certificate) throws GeneralSecurityException
	{
		this.encryptionCipher = Cipher.getInstance( transformation);
		this.decryptionCipher = Cipher.getInstance( transformation );
		encryptionCipher.init( Cipher.ENCRYPT_MODE, certificate);
		decryptionCipher.init( Cipher.DECRYPT_MODE, certificate);
	}

	@Override
	public byte[] encryptValue( byte[] rawValue ) throws GeneralSecurityException
	{
		if(rawValue == null)
		{
			return null;
		}
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(rawValue.length); CipherOutputStream cos = new CipherOutputStream(bos, encryptionCipher ))
		{
			cos.write( rawValue);
			if(encryptionCipher.getBlockSize() != 0 && rawValue.length % encryptionCipher.getBlockSize() != 0)
			{
				//needs padding
				final int paddingCount = encryptionCipher.getBlockSize() - (rawValue.length % encryptionCipher.getBlockSize());
				final byte[] padding = new byte[paddingCount];
				cos.write( padding );
			}
			cos.flush();
			return bos.toByteArray();
		}
		catch(final IOException e)
		{
			throw new GeneralSecurityException(e);
		}
	}

	@Override
	public byte[] decryptValue( byte[] encryptedValue ) throws GeneralSecurityException
	{
		if(encryptedValue == null)
		{
			return null;
		}
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(encryptedValue.length); CipherOutputStream cos = new CipherOutputStream(bos, decryptionCipher))
		{
			cos.write( encryptedValue);
			cos.flush();
			return bos.toByteArray();
		}
		catch(final IOException e)
		{
			throw new GeneralSecurityException(e);
		}
	}

}
