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
package de.doe300.activerecord.record.validation;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.Collections;
import java.util.UUID;
import java.util.function.BiPredicate;
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

	@Test(expected = IllegalArgumentException.class)
	public void testNotEmpty()
	{
		assertTrue( Validations.notEmpty("aa"));
		assertFalse( Validations.notEmpty(""));
		assertFalse( Validations.notEmpty( null));
		assertTrue( Validations.notEmpty( Collections.singleton( "")));
		assertTrue( Validations.notEmpty( new String[]{""}));
		assertFalse( Validations.notEmpty( Collections.emptyList()));
		assertFalse( Validations.notEmpty( new Object[]{}));
		assertTrue( Validations.notEmpty( Collections.singletonMap( "a", null)));
		assertFalse( Validations.notEmpty( Collections.emptyMap()));
		assertFalse( Validations.notEmpty(0));
		assertTrue( Validations.notEmpty(5L));
		
		//throws error
		Validations.notEmpty( new UUID(2, 2));
	}

	@Test(expected = ValidationFailed.class)
	public void testValidate()
	{
		//succeeds
		Validations.validate( "name", "Adam", Validations::notEmpty, "is wrong1");
		//fails
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
		assertFalse( Validations.positiveNumber( null));
		Validations.positiveNumber( "23");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeNumber()
	{
		assertTrue( Validations.negativeNumber( -4346.4));
		assertFalse( Validations.negativeNumber( 345));
		assertFalse( Validations.negativeNumber( null));
		Validations.negativeNumber( 'c' );
	}

	@Test
	public void testGetValidationMethod()
	{
		final BiPredicate<ActiveRecord, Object> nameIsNull = Validations.getValidationMethod( ValidationHolder.class.getAnnotationsByType( Validate.class)[0]);
		assertTrue( nameIsNull.test( null, null) );
		assertFalse( nameIsNull.test( null, "Dummy"));
		
		final BiPredicate<ActiveRecord, Object> childrenIsEmpty = Validations.getValidationMethod( ValidationHolder.class.getAnnotationsByType( Validate.class)[1]);
		assertTrue( childrenIsEmpty.test( null, ""));
		assertTrue( childrenIsEmpty.test( null, new Object[0]));
		assertTrue( childrenIsEmpty.test( null, Collections.emptyList()));
		assertTrue( childrenIsEmpty.test( null, Collections.emptyMap()));
		assertTrue( childrenIsEmpty.test( null, 0));
		assertFalse( childrenIsEmpty.test( null, "Dummy"));
		assertFalse( childrenIsEmpty.test( null, new Object[]{"Dummmy"}));
		assertFalse( childrenIsEmpty.test( null, Collections.singleton( "Dummy")));
		assertFalse( childrenIsEmpty.test( null, Collections.singletonMap( "Dummy", "Dummy")));
		assertFalse( childrenIsEmpty.test( null, 10));
		
		final BiPredicate<ActiveRecord, Object> nameNotNull = Validations.getValidationMethod( ValidationHolder.class.getAnnotationsByType( Validate.class)[2]);
		assertFalse( nameNotNull.test( null, null) );
		assertTrue( nameNotNull.test( null, "Dummy"));
		
		final BiPredicate<ActiveRecord, Object> childrenNotEmpty = Validations.getValidationMethod( ValidationHolder.class.getAnnotationsByType( Validate.class)[3]);
		assertFalse( childrenNotEmpty.test( null, ""));
		assertFalse( childrenNotEmpty.test( null, new Object[0]));
		assertFalse( childrenNotEmpty.test( null, Collections.emptyList()));
		assertFalse( childrenNotEmpty.test( null, Collections.emptyMap()));
		assertFalse( childrenNotEmpty.test( null, 0));
		assertTrue( childrenNotEmpty.test( null, "Dummy"));
		assertTrue( childrenNotEmpty.test( null, new Object[]{"Dummmy"}));
		assertTrue( childrenNotEmpty.test( null, Collections.singleton( "Dummy")));
		assertTrue( childrenNotEmpty.test( null, Collections.singletonMap( "Dummy", "Dummy")));
		assertTrue( childrenNotEmpty.test( null, 10));
		
		final BiPredicate<ActiveRecord, Object> agePositive = Validations.getValidationMethod( ValidationHolder.class.getAnnotationsByType( Validate.class)[4]);
		assertTrue( agePositive.test( null, 12));
		assertFalse( agePositive.test( null, -12));
		
		final BiPredicate<ActiveRecord, Object> ageNegative = Validations.getValidationMethod( ValidationHolder.class.getAnnotationsByType( Validate.class)[5]);
		assertFalse( ageNegative.test( null, 12));
		assertTrue( ageNegative.test( null, -12));
		
		final BiPredicate<ActiveRecord, Object> customName = Validations.getValidationMethod( ValidationHolder.class.getAnnotationsByType( Validate.class)[6]);
		assertFalse( customName.test( null, null));
		assertFalse( customName.test( null, "Test"));
		assertTrue( customName.test( null, "Dummy"));
	}
	
	public static boolean customValidation(final Object name)
	{
		return name != null && "Dummy".equals( name );
	}
	
	@Validates({
		@Validate(attribute = "name", type = ValidationType.IS_NULL),
		@Validate(attribute = "children", type = ValidationType.IS_EMPTY),
		@Validate(attribute = "name", type = ValidationType.NOT_NULL),
		@Validate(attribute = "children", type = ValidationType.NOT_EMPTY),
		@Validate(attribute = "age", type = ValidationType.POSITIVE),
		@Validate(attribute = "age", type = ValidationType.NEGATIVE),
		@Validate(attribute = "name", type = ValidationType.CUSTOM, customClass = ValidationsTest.class, customMethod = "customValidation")
	})
	private interface ValidationHolder
	{
		
	}
}
