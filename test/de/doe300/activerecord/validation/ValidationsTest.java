package de.doe300.activerecord.validation;

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
	public void testNotNull()
	{
		assertTrue( Validations.notNull("a"));
		assertFalse( Validations.notNull(null));
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
		Validations.validate("name", null, Validations::notNull, "is null");
	}

	@Test
	public void testIsValid()
	{
	}
	
}
