package de.doe300.activerecord.record.association;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.TestServer;
import java.util.Arrays;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daniel
 */
public class HasManyThroughAssociationSetTest extends Assert
{
	private static Set<TestInterface> set;
	private static RecordBase<TestInterface> base;
	private static TestInterface assocI;
	private static TestInterface a1, a2, a3;
	private static TestInterface n1, n2;
	
	public HasManyThroughAssociationSetTest()
	{
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables();
		base = RecordCore.fromDatabase( TestServer.getTestConnection(), true).buildBase( TestInterface.class );
		assocI = base.createRecord();
		set = AssociationHelper.getHasManyThroughSet(assocI, TestInterface.class, "mappingTable", "fk_test1", "fk_test2" );
		
		//fill set
		a1 = base.createRecord();
		AssociationHelper.addHasManyThrough( assocI, a1, "mappingTable", "fk_test1", "fk_test2");
		a2 = base.createRecord();
		AssociationHelper.addHasManyThrough( assocI, a2, "mappingTable", "fk_test1", "fk_test2");
		a3 = base.createRecord();
		AssociationHelper.addHasManyThrough( assocI, a3, "mappingTable", "fk_test1", "fk_test2");
		n1 = base.createRecord();
		n2 = base.createRecord();
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		TestServer.destroyTestTables();
	}

	@Test
	public void testSize()
	{
		assertEquals(3, set.size());
		assertEquals( 3, set.stream().count());
	}

	@Test
	public void testContains()
	{
		assertTrue( set.contains( a1));
		assertFalse( set.contains( n1));
	}

	@Test
	public void testIterator()
	{
		assertNotNull( set.iterator() );
	}

	@Test
	public void testAdd()
	{
		assertFalse( set.add( a2));
		assertTrue( set.add( n1));
		assertTrue( set.remove( n1));
	}

	@Test
	public void testRemove()
	{
		assertFalse( set.remove( n2));
		assertTrue( set.remove( a3));
		assertTrue( set.add( a3));
	}

	@Test
	public void testContainsAll()
	{
		assertTrue( set.containsAll( Arrays.asList( a1,a2,a3)) );
		assertFalse( set.containsAll( Arrays.asList( a1, a2, n1)));
	}

	@Test
	public void testAddAll()
	{
		assertTrue( set.addAll( Arrays.asList( n1,n2)));
		assertFalse( set.addAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.removeAll( Arrays.asList( n1,n2)));
	}

	@Test
	public void testRetainAll()
	{
		assertTrue( set.retainAll( Arrays.asList( n1,a2,n2)));
		assertFalse( set.retainAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.addAll( Arrays.asList( a1,a2,a3)));
	}

	@Test
	public void testRemoveAll()
	{
		assertTrue( set.removeAll( Arrays.asList( a1,a2,a3)));
		assertTrue( set.isEmpty());
		assertTrue( set.addAll( Arrays.asList( a1,a2,a3)));
	}

	@Test
	public void testClear()
	{
		assertFalse( set.isEmpty());
		set.clear();
		assertTrue( set.isEmpty());
	}
}