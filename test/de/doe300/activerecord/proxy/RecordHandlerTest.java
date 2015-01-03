package de.doe300.activerecord.proxy;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.validation.ValidatedRecord;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class RecordHandlerTest extends Assert
{
	private static RecordHandler<TestInterface> handler;
	private static RecordBase<TestInterface> base;
	private static TestInterface testI;
	
	public RecordHandlerTest()
	{
	}
	
	@BeforeClass
	public static void setUpClass() throws SQLException, Exception
	{
		base = RecordCore.fromDatabase( TestInterface.createTestConnection(), true).buildBase( TestInterface.class);
		testI = base.createRecord();
		handler = new RecordHandler<TestInterface>(testI.getPrimaryKey(), base);
	}
	
	@Test
	public void testInvokeStandardMethods() throws Exception, Throwable
	{
		Method getPrimaryKey, getRecordBase, hashCode, toString, equals;
		getPrimaryKey = ActiveRecord.class.getMethod( "getPrimaryKey");
		getRecordBase = ActiveRecord.class.getMethod( "getBase");
		hashCode = Object.class.getMethod( "hashCode");
		toString = Object.class.getMethod( "toString");
		equals = Object.class.getMethod( "equals", Object.class);
		
		assertEquals( testI.getPrimaryKey(), handler.invoke( testI, getPrimaryKey,null));
		assertEquals( base, handler.invoke( testI, getRecordBase, null));
		assertNotNull( handler.invoke( testI, hashCode, null));
		assertNotNull( handler.invoke( testI, toString, null));
		assertFalse(( boolean ) handler.invoke( testI, equals, null));
		assertTrue((boolean) handler.invoke( testI, equals, new Object[]{testI}));
	}
	
	@Test
	public void testInvokeDefaultMethods() throws NoSuchMethodException, Throwable
	{
		Method df1= TestInterface.class.getMethod( "checkName", Object.class), df2 = ValidatedRecord.class.getMethod( "isValid");
		assertFalse( (boolean)handler.invoke( testI, df2, null));
		handler.invoke( testI, df1, new Object[]{"Test"});
		handler.invoke( testI, df2, null);
	}
	
	@Test(expected = InvocationTargetException.class)
	public void testInvokeAttributeAccessor() throws NoSuchMethodException, Throwable
	{
		Method m1 = TestInterface.class.getMethod( "setName", String.class);
		Method m2 = TestInterface.class.getMethod( "getOther");
		handler.invoke( testI, m1, new Object[]{null});
		handler.invoke( testI, m2, null);
	}
	
	@Test
	public void testInvokeBeanStyleAccessor() throws Throwable
	{
		Method m1 = TestInterface.class.getMethod( "setAge", Integer.TYPE), m2 = TestInterface.class.getMethod( "getAge");
		handler.invoke( testI, m1, new Object[]{12});
		assertEquals( 12, testI.getAge());
		assertEquals( 12, handler.invoke( testI, m2, null));
	}
	
	@Test(expected = NoSuchMethodException.class)
	public void testInvokeMethodNotHandled() throws NoSuchMethodException, Throwable
	{
		Method m = String.class.getMethod( "length");
		handler.invoke( testI, m, null );
	}
	
	@Test
	public void testInvokeErrors()
	{
		try
		{
			handler.invoke( "5", null, null );
		}
		catch ( Throwable ex )
		{
			assertTrue( ex instanceof IllegalArgumentException);
		}
	}
}
