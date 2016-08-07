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
package de.doe300.activerecord.store;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestInterface;
import de.doe300.activerecord.migration.Migration;
import de.doe300.activerecord.migration.constraints.IndexType;
import java.io.Serializable;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.junit.Ignore;
import org.junit.Test;


public class DBDriverTest extends TestBase
{
	private final RecordBase<TestInterface> base;
	private final DBDriver driver;
	
	public DBDriverTest(@Nonnull final RecordCore core)
	{
		super(core);
		base = core.getBase( TestInterface.class).getShardBase( DBDriverTest.class.getSimpleName());
		driver = core.getStore().getDriver();
	}
	
	@Test
	public void testIsTypeSupported()
	{
		assertTrue( driver.isTypeSupported( String.class));
		assertTrue( driver.isTypeSupported( Boolean.class));
		assertTrue( driver.isTypeSupported( Byte.class));
		assertTrue( driver.isTypeSupported( Short.class));
		assertTrue( driver.isTypeSupported( Integer.class));
		assertTrue( driver.isTypeSupported( Long.class));
		assertTrue( driver.isTypeSupported( Float.class));
		assertTrue( driver.isTypeSupported( Double.class));
		assertTrue( driver.isTypeSupported( Serializable.class));
	}

	@Test
	public void testCreateMigration_3args_1() throws Exception
	{
		try
		{
			final Migration mig = driver.createMigration( TestInterface.class, DBDriverTest.class.getSimpleName(), base.getStore() );
			assertNotNull( mig);
			assertTrue( mig.apply());
			assertTrue( mig.revert());
		}
		catch(UnsupportedOperationException uoe)
		{
			//is allowed
		}
	}

	@Test
	public void testCreateMigration_3args_2() throws Exception
	{
		try
		{
			final Migration mig = driver.createMigration( DBDriverTest.class.getSimpleName(), Collections.singletonMap( "id", Integer.class), base.getStore() );
			assertNotNull( mig);
			assertTrue( mig.apply());
			assertTrue( mig.revert());
		}
		catch(UnsupportedOperationException uoe)
		{
			//is allowed
		}
	}

	@Ignore("Unknown error for JDBC drivers")
	@Test
	public void testCreateMigration_4args_1() throws Exception
	{
		try
		{
			final Migration mig = driver.createMigration( DBDriverTest.class.getSimpleName(), Collections.singletonMap( "id", Integer.class), 
					Collections.singletonMap( Collections.singleton( "id"), IndexType.UNIQUE), base.getStore() );
			assertNotNull( mig);
			assertTrue( mig.apply());
			assertTrue( mig.revert());
		}
		catch(UnsupportedOperationException uoe)
		{
			//is allowed
		}
	}

	@Test
	public void testCreateMigration_4args_2() throws Exception
	{
		try
		{
			final Migration mig = driver.createMigration( "CREATE TABLE dbdrivertest (id INTEGER)", "ALTER TABLE dbdrivertest ADD num INTEGER",
					"DROP TABLE dbdrivertest", base.getStore() );
			assertNotNull( mig);
			assertTrue( mig.apply());
			assertTrue( mig.update( true));
			assertTrue( mig.revert());
		}
		catch(UnsupportedOperationException uoe)
		{
			//is allowed
		}
	}
}
