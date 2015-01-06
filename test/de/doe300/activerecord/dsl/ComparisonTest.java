package de.doe300.activerecord.dsl;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class ComparisonTest extends Assert
{
	public ComparisonTest()
	{
	}

	@Test
	public void testIS()
	{
		Object a = "a";
		assertTrue( Comparison.IS.test( a, a));
		assertFalse( Comparison.IS.test( a, "b"));
	}
	
	@Test
	public void testIS_NOT()
	{
		Object a = "a";
		assertTrue( Comparison.IS_NOT.test( a, "b"));
		assertFalse( Comparison.IS_NOT.test( a, a));
	}
	
	@Test
	public void testIS_NULL()
	{
		Object a = null;
		assertTrue( Comparison.IS_NULL.test( a, null));
		assertFalse( Comparison.IS_NULL.test( "b", null));
	}
	
	@Test
	public void testIS_NOT_NULL()
	{
		Object a = null;
		assertTrue( Comparison.IS_NOT_NULL.test( "b", null));
		assertFalse( Comparison.IS_NOT_NULL.test( a, null));
	}
	
	 @Test
	 public void testLIKE()
	 {
		 Object a = "Apfelsaft";
		 assertTrue( Comparison.LIKE.test( a, "%saft"));
		 assertTrue( Comparison.LIKE.test( a, "%el%af%"));
		 assertFalse( Comparison.LIKE.test( a, "%Saft"));
		 assertFalse( Comparison.LIKE.test( a, "Birne"));
		 assertFalse( Comparison.LIKE.test( a, "Saftapfel"));
	 }
	 
	 @Test(expected = ClassCastException.class)
	 public void testLARGER()
	 {
		 Object a = -5, b = 7;
		 assertTrue( Comparison.LARGER.test( b, a));
		 assertFalse( Comparison.LARGER.test( a, b));
		 assertFalse( Comparison.LARGER.test( a, a ));
		 Comparison.LARGER.test( "a", new String[0]);
	 }
	 
	 @Test(expected = ClassCastException.class)
	 public void testSMALLER()
	 {
		 Object a = -5, b = 7;
		 assertTrue( Comparison.SMALLER.test( a, b));
		 assertFalse( Comparison.SMALLER.test( b, a));
		 assertFalse( Comparison.SMALLER.test( a, a));
		 Comparison.SMALLER.test( a, new int[]{7});
	 }
	 
	 @Test(expected = ClassCastException.class)
	 public void testLARGER_EQUALS()
	 {
		 Object a = -5, b = 7;
		 assertTrue( Comparison.LARGER_EQUALS.test( b, a));
		 assertTrue( Comparison.LARGER_EQUALS.test( a, a ));
		 assertFalse( Comparison.LARGER_EQUALS.test( a, b));
		 Comparison.LARGER_EQUALS.test( "a", new String[0]);
	 }
	 
	 @Test(expected = ClassCastException.class)
	 public void testSMALLER_EQUALS()
	 {
		 Object a = -5, b = 7;
		 assertTrue( Comparison.SMALLER_EQUALS.test( a, b));
		 assertTrue( Comparison.SMALLER_EQUALS.test( a, a));
		 assertFalse( Comparison.SMALLER_EQUALS.test( b, a));
		 Comparison.SMALLER_EQUALS.test( a, new int[]{7});
	 }
	 
	 @Test
	 public void testTRUE()
	 {
		 assertTrue( Comparison.TRUE.test( this, this));
		 assertTrue( Comparison.TRUE.test( null, null));
	 }
	 
	 @Test(expected = IllegalArgumentException.class)
	 public void  testIN()
	 {
		 Object a = "a";
		 assertTrue( Comparison.IN.test( a, new Object[]{a, "b"}));
		 assertTrue( Comparison.IN.test( a, Collections.singleton( a)));
		 assertTrue( Comparison.IN.test( a, Arrays.asList( a )));
		 assertTrue( Comparison.IN.test( a, a ));
		 assertFalse( Comparison.IN.test( a, new String[0]));
		 assertFalse( Comparison.IN.test( a, Collections.singleton( "b")));
		 assertFalse( Comparison.IN.test( a, Arrays.asList()));
		 assertFalse( Comparison.IN.test( a, null));
		 Comparison.IN.test( a, "b");
	 }
}