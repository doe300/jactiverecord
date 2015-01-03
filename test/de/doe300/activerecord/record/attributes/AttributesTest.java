package de.doe300.activerecord.record.attributes;

import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.validation.ValidatedRecord;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class AttributesTest
{
	
	public AttributesTest()
	{
	}

	@Test
	public void testGetConverterMethod() throws Exception
	{
		Method m1 = TestInterface.class.getMethod( "getOther"), m2 = TestInterface.class.getMethod( "setName", String.class);
		Method c1 = TestInterface.class.getMethod( "getInterFace", Object.class);
		assertEquals(Attributes.getConverterMethod( m1), c1);
		assertNotSame(Attributes.getConverterMethod( m2), c1);
		assertNull(Attributes.getConverterMethod( m2));
	}

	@Test
	public void testGetValidatorMethod() throws Exception
	{
		Method m1 = TestInterface.class.getMethod( "getOther"), m2 = TestInterface.class.getMethod( "setName", String.class);
		Method c1 = TestInterface.class.getMethod( "checkName", Object.class );
		assertEquals(Attributes.getValidatorMethod(m2), c1);
		assertNotSame(Attributes.getValidatorMethod(m1), c1);
		assertNull(Attributes.getValidatorMethod(m1));
	}

	@Test
	public void testIsGetter() throws NoSuchMethodException
	{
		Method m1 = TestInterface.class.getMethod( "getName"), m2 = TestInterface.class.getMethod( "setName", String.class);
		assertTrue(Attributes.isGetter( m1, false));
		assertFalse(Attributes.isGetter( m2, false));
	}

	@Test
	public void testIsSetter() throws NoSuchMethodException, IntrospectionException
	{
		Method m1 = TestInterface.class.getMethod( "getName"), m2 = TestInterface.class.getMethod( "setAge", Integer.TYPE);
		assertTrue(Attributes.isSetter( m2, Integer.TYPE, false));
		assertTrue(Attributes.isSetter( m2, Integer.class, false));
		assertFalse(Attributes.isSetter(m1, String.class, false));
		assertFalse(Attributes.isSetter( m2, Object.class, false));
	}

	@Test
	public void testGetPropertyName() throws NoSuchMethodException
	{
		Method m1 = TestInterface.class.getMethod( "getName"), m2 = TestInterface.class.getMethod( "setAge", Integer.TYPE),
				m3 = TimestampedRecord.class.getMethod( "getUpdatedAt"), m4 = ValidatedRecord.class.getMethod( "isValid");
		assertEquals("name", Attributes.getPropertyName( m1));
		assertEquals("age", Attributes.getPropertyName( m2));
		assertEquals("updated_at", Attributes.getPropertyName( m3));
		assertEquals("valid", Attributes.getPropertyName( m4));
	}

	@Test
	public void testCheckNotNull()
	{
		
	}

	@Test
	public void testCheckAttribute()
	{
	}

	@Test
	public void testGetLength()
	{
	}
	
}
