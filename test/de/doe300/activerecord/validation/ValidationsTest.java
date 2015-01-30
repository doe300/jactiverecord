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
package de.doe300.activerecord.validation;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class ValidationsTest extends Assert
{
	
	public ValidationsTest()
	{
	}

	@Test
	public void testNotEmpty()
	{
		assertTrue( Validations.notEmpty("aa"));
		assertFalse( Validations.notEmpty(""));
	}

	@Test(expected = ValidationFailed.class)
	public void testValidate()
	{
		Validations.validate("name", null, (Object obj) -> obj != null, "is null");
	}

	@Test
	public void testIsValid()
	{
	}

	@Test
	public void testIsEmpty()
	{
		assertTrue( Validations.isEmpty( ""));
		assertFalse( Validations.isEmpty( Collections.singleton( "")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPositiveNumber()
	{
		assertTrue( Validations.positiveNumber( 3));
		assertFalse( Validations.positiveNumber( -5));
		Validations.positiveNumber( "23");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeNumber()
	{
		assertTrue( Validations.negativeNumber( -4346.4));
		assertFalse( Validations.negativeNumber( 345));
		Validations.negativeNumber( 'c' );
	}

	@Test
	public void testGetValidationMethod()
	{
	}
	
}
