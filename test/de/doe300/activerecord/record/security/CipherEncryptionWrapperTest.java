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

import java.security.GeneralSecurityException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author doe300
 * @since 0.6
 */
public class CipherEncryptionWrapperTest
{
	private final CipherEncryptionWrapper wrapper;
	
	public CipherEncryptionWrapperTest() throws GeneralSecurityException
	{
		wrapper = new CipherEncryptionWrapper("DES/ECB/NoPadding", SecretKeyFactory.getInstance( "DES").generateSecret( new DESKeySpec("Hallo !!".getBytes())));
	}

	@Test
	public void testMultiBlocks() throws Exception
	{
		final String originalValue = "Some Text to be checked!";
		final byte[] temporaryValue = wrapper.encryptValue( originalValue.getBytes() );
		final String result = new String(wrapper.decryptValue( temporaryValue ));
		
		Assert.assertEquals( originalValue, result);
	}
	
	@Test
	public void testSingleBlock() throws Exception
	{
		final String originalValue = "Short!!!";
		final byte[] temporaryValue = wrapper.encryptValue( originalValue.getBytes() );
		final String result = new String(wrapper.decryptValue( temporaryValue ));
		
		Assert.assertEquals( originalValue, result);
	}
	
	@Test
	public void testNull() throws Exception
	{
		final byte[] temporaryValue = wrapper.encryptValue( null );
		final byte[] result = wrapper.decryptValue( temporaryValue );
		
		Assert.assertNull(result);
	}
}
