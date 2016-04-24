/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 doe300
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
package de.doe300.activerecord.jdbc.driver;

import de.doe300.activerecord.migration.constraints.IndexType;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author doe300
 * @since 0.7
 */
@RunWith(Parameterized.class)
public class JDBCDriverTest extends Assert
{
	private final JDBCDriver driver;
	
	public JDBCDriverTest(@Nonnull final JDBCDriver driver)
	{
		this.driver = driver;
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters()
	{
		return Arrays.asList(
			new Object[]{JDBCDriver.DEFAULT},
			new Object[]{HSQLDBDriver.INSTANCE},
			new Object[]{SQLiteDriver.INSTANCE},
			new Object[]{PostgreSQLDriver.INSTANCE},
			new Object[]{MySQLDriver.INSTANCE}
		);
	}

	@Test
	public void testIsTypeSupported()
	{
		//some standard classes
		assertTrue( driver.isTypeSupported( String.class));
		assertTrue( driver.isTypeSupported( Character.class));
		assertTrue( driver.isTypeSupported( Byte.class));
		assertTrue( driver.isTypeSupported( Short.class));
		assertTrue( driver.isTypeSupported( Integer.class));
		assertTrue( driver.isTypeSupported( Long.class));
		assertTrue( driver.isTypeSupported( Float.class));
		assertTrue( driver.isTypeSupported( Double.class));
		assertTrue( driver.isTypeSupported( Boolean.class));
		
		//SQL-types
		assertTrue( driver.isTypeSupported( java.sql.Date.class));
		assertTrue( driver.isTypeSupported( java.sql.Time.class));
		assertTrue( driver.isTypeSupported( java.sql.Timestamp.class));
		
		//some more types
		assertTrue( driver.isTypeSupported( ActiveRecord.class));
		assertTrue( driver.isTypeSupported( Enum.class));
		assertTrue( driver.isTypeSupported( java.util.UUID.class));
		assertTrue( driver.isTypeSupported( java.net.URL.class));
		assertTrue( driver.isTypeSupported( java.io.Serializable.class));
		
		//some dummy type
		assertFalse( driver.isTypeSupported( Void.class) );
	}

	@Test
	public void testGetIndexKeyword()
	{
		assertNotNull( driver.getIndexKeyword( IndexType.DEFAULT));
		assertNotNull( driver.getIndexKeyword( IndexType.CLUSTERED));
		assertNotNull( driver.getIndexKeyword( IndexType.UNIQUE));
	}

	@Test
	public void testGetLimitClause()
	{
		assertNotNull( driver.getLimitClause( 10, 10));
	}

	@Test
	public void testGetSQLFunction()
	{
		assertNotNull( driver.getSQLFunction( JDBCDriver.AGGREGATE_AVERAGE, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.AGGREGATE_COUNT_ALL, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.AGGREGATE_COUNT_DISTINCT, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.AGGREGATE_COUNT_NOT_NULL, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.AGGREGATE_MAXIMUM, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.AGGREGATE_MINIMUM, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.AGGREGATE_SUM, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.AGGREGATE_SUM_DOUBLE, "test_column"));
		
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_ABS, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_ABS_DOUBLE, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_CEILING, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_FLOOR, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_LOWER, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_ROUND, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_SIGN, "test_column"));
		if(driver instanceof SQLiteDriver)
		{
			try
			{
				driver.getSQLFunction( JDBCDriver.SCALAR_SQRT, "test_column");
				fail( "No exception thrown!");
			}
			catch(final RuntimeException e)
			{
				//exception is expected here!
			}
		}
		else
		{
			assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_SQRT, "test_column"));
		}
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_STRING_LENGTH, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_TRIM, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_UPPER, "test_column"));
		assertNotNull( driver.getSQLFunction( JDBCDriver.SCALAR_VALUE, "test_column"));
	}

	@Test
	public void testGetPrimaryKeyKeywords()
	{
		assertNotNull( driver.getPrimaryKeyKeywords( "INTEGER"));
		assertNotEquals( "INTEGER", driver.getPrimaryKeyKeywords( "INTEGER"));
	}

	@Test
	public void testGetStringDataType()
	{
		assertNotNull( driver.getStringDataType());
	}

	@Test
	public void testGetInsertDataForEmptyRow()
	{
		assertNotNull( driver.getInsertDataForEmptyRow( "id"));
		assertTrue( driver.getInsertDataForEmptyRow( "id").toUpperCase().contains( "VALUES"));
	}

	@Test
	public void testConvertBooleanToDB()
	{
		assertTrue( driver.convertDBToBoolean( driver.convertBooleanToDB( true)));
		assertFalse( driver.convertDBToBoolean( driver.convertBooleanToDB( false)));
	}

	@Test
	public void testGetSQLType()
	{
		assertEquals( driver.getStringDataType(), driver.getSQLType( String.class));
	}

	@Test
	public void testGetJavaType()
	{
		assertEquals( String.class, driver.getJavaType( driver.getStringDataType()));
		assertTrue( Number.class.isAssignableFrom( driver.getJavaType( driver.getSQLType( ActiveRecord.class))));
		assertTrue(java.sql.Date.class.isAssignableFrom( driver.getJavaType( driver.getSQLType( java.sql.Date.class))));
	}

	@Test
	public void testIsReservedKeyword() throws Exception
	{
		assertTrue( driver.isReservedKeyword( "when", null));
	}

	@Test
	public void testGetNextTableIdentifier()
	{
		//make sure, no identifiers are created twice
		final Set<String> identifiers = new HashSet<>(20);
		String lastIdentifier = null;
		for(int i = 0; i < 10; ++i)
		{
			assertTrue( identifiers.add( lastIdentifier = JDBCDriver.getNextTableIdentifier( lastIdentifier) ));
		}
		assertEquals( "otherAssociatedTable", JDBCDriver.getNextTableIdentifier( "associatedTableWrong"));
		lastIdentifier = "joinedTable"; 
		for(int i = 0; i < 10; ++i)
		{
			assertTrue( identifiers.add( lastIdentifier = JDBCDriver.getNextTableIdentifier( lastIdentifier)));
		}
		lastIdentifier = "dummy"; 
		assertTrue( identifiers.add( lastIdentifier = JDBCDriver.getNextTableIdentifier( lastIdentifier)));
		//falls back to associated-table, already in set
		assertFalse(identifiers.add( JDBCDriver.getNextTableIdentifier( lastIdentifier)));
	}
	
}
