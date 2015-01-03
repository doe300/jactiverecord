package de.doe300.activerecord;

import de.doe300.activerecord.pojo.POJOBase;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.DataSet;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.store.RecordStore;

/**
 *
 * @author doe300
 */
@Searchable(searchableColumns = {"name","age"})
@DataSet(dataSet = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
public class TestPOJO implements ActiveRecord, TestInterface
{
	private final int primaryKey;
	private final POJOBase<TestPOJO> base;

	public TestPOJO(int key, POJOBase<TestPOJO> base)
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
	public void setName(String name)
	{
		base.setProperty( primaryKey, "name", name);
	}
	
	@Override
	public String getName()
	{
		return ( String ) base.getProperty( primaryKey, "name");
	}
	
	@Override
	public void setAge(int age)
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
		catch ( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void setOther( TestInterface it )
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
}
