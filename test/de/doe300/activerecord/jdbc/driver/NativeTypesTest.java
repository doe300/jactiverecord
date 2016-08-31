/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.doe300.activerecord.jdbc.driver;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.store.impl.memory.MemoryRecordStore;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 * @since 0.8
 */
public class NativeTypesTest extends TestBase
{
	
	public interface TestNativeTypes extends ActiveRecord
	{
		//"col" inserted, so names are no reserved keyword
		public boolean getColBoolean();
		public byte getColByte();
		public short getColShort();
		public int getColInteger();
		public long getColLong();
		public float getColFloat();
		public double getColDouble();
		//public char getColChar();	//not supported by JDBC standard
		public String getColString();
		public Date getColDate();
		public Time getColTime();
		public Timestamp getColTimestamp();
	}
	
	private final RecordBase<TestNativeTypes> base;
	
	public NativeTypesTest(@Nonnull final RecordCore core)
	{
		super(core);
		base = core.getBase( TestNativeTypes.class).getShardBase( "testNatives");
		for(Map.Entry<String, Class<?>> type: base.getStore().getAllColumnTypes( base.getTableName() ).entrySet())
		{
			System.out.println( type.getKey()+": " + type.getValue() );
		}
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		TestServer.buildTestTables( TestNativeTypes.class, "testNatives");
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		TestServer.destroyTestTables(TestNativeTypes.class, "testNatives");
	}
	
	@Test
	public void testNativeOnly()
	{
		assertTrue( base.createRecord( Collections.singletonMap( "col_boolean", true)).getColBoolean());
		assertEquals( (byte)5, base.createRecord( Collections.singletonMap( "col_byte", (byte)5)).getColByte());
		assertEquals( (short)200, base.createRecord( Collections.singletonMap( "col_short", (short)200)).getColShort());
		assertEquals( 100000, base.createRecord( Collections.singletonMap( "col_integer", 100000)).getColInteger());
		assertEquals( 102410241024L, base.createRecord( Collections.singletonMap( "col_long", 102410241024L)).getColLong());
		assertEquals( 200.5f, base.createRecord( Collections.singletonMap( "col_float", 200.5f)).getColFloat(), 0.005f);
		assertEquals( 200.0005d, base.createRecord( Collections.singletonMap( "col_double", 200.0005d)).getColDouble(), 0.000005d);
		assertEquals( "Hello World!", base.createRecord( Collections.singletonMap( "col_string", "Hello World!")).getColString());
		final long someDays = 3600000L * 24 * 12; //12 days
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis( someDays );
		final long offset = cal.getTimeZone().getOffset( someDays );
		if(base.getStore() instanceof MemoryRecordStore || base.getStore().getDriver() instanceof SQLiteDriver)
		{
			//SQLite doesn't apply any offset!
			assertEquals( someDays, base.createRecord( Collections.singletonMap( "col_date", new Date(someDays))).getColDate().getTime());
			assertEquals( someDays, base.createRecord( Collections.singletonMap( "col_time", new Time(someDays))).getColTime().getTime());
			assertEquals( someDays, base.createRecord( Collections.singletonMap( "col_timestamp", new Timestamp(someDays))).getColTimestamp().getTime());
		}
		else	//HsqlDB, MySQL and PostgreSQL apply timezone offset
		{
			assertEquals( someDays - offset, base.createRecord( Collections.singletonMap( "col_date", new Date(someDays))).getColDate().getTime());
			assertEquals( 450000, base.createRecord( Collections.singletonMap( "col_time", new Time(450000))).getColTime().getTime());
			assertEquals( someDays, base.createRecord( Collections.singletonMap( "col_timestamp", new Timestamp(someDays))).getColTimestamp().getTime());
		}
	}
}
