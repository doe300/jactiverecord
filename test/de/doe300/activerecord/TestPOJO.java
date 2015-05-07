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
package de.doe300.activerecord;

import de.doe300.activerecord.jdbc.TypeMappings;
import de.doe300.activerecord.pojo.POJOBase;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.validation.Validate;
import de.doe300.activerecord.validation.ValidationType;
import java.sql.Timestamp;

/**
 *
 * @author doe300
 */
@Searchable(searchableColumns = {"name","age"})
@RecordType(typeName = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
@Validate(attribute = "name", type = ValidationType.NOT_NULL)
public class TestPOJO implements ActiveRecord, TestInterface
{
	private final int primaryKey;
	private final POJOBase<TestPOJO> base;

	/**
	 * @param key
	 * @param base
	 */
	public TestPOJO(final int key, final POJOBase<TestPOJO> base)
	{
		this.primaryKey = key;
		this.base = base;
	}


	@Override
	public int getPrimaryKey()
	{
		return primaryKey;
	}

	@Override
	public RecordBase<?> getBase()
	{
		return base;
	}

	@Override
	public void setName(final String name)
	{
		base.setProperty( primaryKey, "name", name);
	}

	@Override
	public String getName()
	{
		return ( String ) base.getProperty( primaryKey, "name");
	}

	@Override
	public void setAge(final int age)
	{
		base.setProperty( primaryKey, "age", age);
	}

	@Override
	public int getAge()
	{
		return ( int ) base.getProperty( primaryKey, "age");
	}

	@Override
	public TestInterface getOther()
	{
		try
		{
			return base.getCore().buildBase( TestInterface.class).getRecord(( int ) base.getProperty( primaryKey, "fk_test_id"));
		}
		catch ( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void setOther( final TestInterface it )
	{
		base.setProperty( primaryKey, "fk_test_id", it.getPrimaryKey());
	}

	@Override
	public Timestamp getCreatedAt()
	{
		return ( Timestamp ) base.getProperty( primaryKey, TimestampedRecord.COLUMN_CREATED_AT);
	}

	@Override
	public Timestamp getUpdatedAt()
	{
		return ( Timestamp ) base.getProperty( primaryKey, TimestampedRecord.COLUMN_UPDATED_AT);
	}

	@Override
	public void touch()
	{
		base.getStore().touch( base, primaryKey );
	}

	@Override
	public boolean equals(final Object obj)
	{
		return obj instanceof TestPOJO && RecordBase.equals( this, (TestPOJO)obj);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"{"+primaryKey+"@"+base.getRecordType().getCanonicalName()+"}";
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public ValidationType getTestEnum()
	{
		return TypeMappings.readEnumValue( ValidationType.class, this, "test_enum");
	}
}
