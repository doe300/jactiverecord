package de.doe300.activerecord;

import de.doe300.activerecord.pojo.POJOBase;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.store.RecordStore;

/**
 *
 * @author doe300
 */
@Searchable(searchableColumns = {"name","age"})
@RecordType(typeName = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
public class TestPOJO implements ActiveRecord, TestInterface
{
	private final int primaryKey;
	private final POJOBase<TestPOJO> base;

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
	public long getCreatedAt()
	{
		return ( long ) base.getProperty( primaryKey, RecordStore.COLUMN_CREATED_AT);
	}

	@Override
	public long getUpdatedAt()
	{
		return ( long ) base.getProperty( primaryKey, RecordStore.COLUMN_UPDATED_AT);
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
		// TODO Auto-generated method stub
		return super.hashCode();
	}
}
