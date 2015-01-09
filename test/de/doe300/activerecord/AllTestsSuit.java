package de.doe300.activerecord;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author daniel
 */
@RunWith( Suite.class )
@Suite.SuiteClasses( 
{ AssociationHelperTest.class, TestRecordStore.class, RecordCoreTest.class, TestActiveRecordSyntax.class, TestPOJO.class,
		TestInterface.class, RecordBaseTest.class })
public class AllTestsSuit
{

	@BeforeClass
	public static void setUpClass() throws Exception
	{
		//TODO create test DB
	}

	@AfterClass
	public static void tearDownClass() throws Exception
	{
		//TODO remove test DB
	}
	
}
