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
package de.doe300.activerecord.record.attributes;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.validation.ValidatedRecord;
import java.lang.reflect.Method;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class AttributesTest
{
	private static RecordBase<TestInterface> base;
	
	public AttributesTest()
	{
	}
	
	@BeforeClass
	public static void createTables() throws Exception
	{
		TestServer.buildTestTable(TestInterface.class, AttributesTest.class.getSimpleName());
		base = TestServer.getTestCore().getBase( TestInterface.class).getShardBase( AttributesTest.class.getSimpleName());
	}
	
	@AfterClass
	public static void destroyTables() throws Exception
	{
		TestServer.destroyTestTable(TestInterface.class, AttributesTest.class.getSimpleName());
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
	public void testIsSetter() throws NoSuchMethodException
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
		assertEquals("name", Attributes.getPropertyName( m1.getName()));
		assertEquals("age", Attributes.getPropertyName( m2.getName()));
		assertEquals("updated_at", Attributes.getPropertyName( m3.getName()));
		assertEquals("valid", Attributes.getPropertyName( m4.getName()));
		assertNull(Attributes.getPropertyName( "get" ));
		assertNull(Attributes.getPropertyName( "set" ));
	}

	@Test
	public void testCheckNotNull()
	{
		TestInterface i = base.createRecord();
		i.setName( "Adam");
		assertTrue( Attributes.checkNotNull( i, "name", null));
		i.setAge( 123);
		assertTrue( Attributes.checkNotNull(i, "age", null));
	}

	@Test
	public void testCheckAttribute()
	{
		TestInterface i = base.createRecord();
		i.setAge( 123);
		i.setName( "Eve");
		assertTrue( Attributes.checkAttribute( i, "name", (Object obj) -> "Eve".equals( obj), null));
		assertTrue( Attributes.checkAttribute(i, "age", (Object obj) -> "123".equals( obj ) , (Object t) -> Integer.toString( (int)t)));
		assertFalse( Attributes.checkAttribute( i, "age", (Object obj) -> "Eve".equals( obj), null));
	}

	@Test
	public void testGetLength()
	{
		TestInterface i = base.createRecord();
		String name = "Steve";
		i.setName( name);
		i.setDirectionOne(i);
		i.save();
		
		assertEquals(name.length(), Attributes.getLength( i, "name", null, null));
		assertEquals( -1, Attributes.getLength( i, "age", null, null));
		
		assertEquals( 42, Attributes.getLength( i, "fk_test_id", (Object obj) -> (obj instanceof Number) ? 42 : -1, null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToCamelCase()
	{
		assertEquals( "AttributeWithCamelCase", Attributes.toCamelCase( "attribute_with_camel_case"));
		Attributes.toCamelCase( " no such attribute");
	}	
}
