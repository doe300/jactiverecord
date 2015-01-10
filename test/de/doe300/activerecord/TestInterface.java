package de.doe300.activerecord;

import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.RecordCallbacks;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.validation.ValidatedRecord;
import de.doe300.activerecord.validation.ValidationFailed;
import java.sql.Types;
import java.util.stream.Stream;

/**
 *
 * @author doe300
 */
@Searchable(searchableColumns = {"name", "age"})
@RecordType(typeName = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
public interface TestInterface extends TimestampedRecord, ValidatedRecord, RecordCallbacks
{
	public String getName();
	
	@AttributeSetter(name = "name", validatorClass = TestInterface.class, validatorMethod = "checkName")
	public void setName(String name) throws ValidationFailed;
	
	public int getAge();
	
	public void setAge(int age);

	@Attribute(name = "fk_test_id", type = Types.INTEGER)
	public default TestInterface getDirectionOne()
	{
		return AssociationHelper.getBelongsTo(this, TestInterface.class, "fk_test_id" );
	}
	
	public default void setDirectionOne(TestInterface i)
	{
		AssociationHelper.setBelongsTo( this, i, "fk_test_id");
	}
	
	public default TestInterface getDirectionOther()
	{
		return AssociationHelper.getHasOne(this, TestInterface.class, "fk_test_id" );
	}
	
	public default void setDirectionOther(TestInterface i)
	{
		AssociationHelper.setHasOne( this, i, "fk_test_id" );
	}
	
	public default Stream<TestInterface> getALotOne()
	{
		return AssociationHelper.getHasManyThrough( this, TestInterface.class, "mappingTable", "fk_test1", "fk_test2");
	}
	
	public default Stream<TestInterface> getALotOther()
	{
		return AssociationHelper.getHasManyThrough( this, TestInterface.class, "mappingTable", "fk_test2", "fk_test1");
	}
	
	//Obsolete method for associations
	@AttributeGetter(name = "other", converterClass = TestInterface.class, converterMethod = "getInterFace")
	public TestInterface getOther();
	
	@AttributeSetter(name = "other", converterClass = TestInterface.class, converterMethod = "setInterFace")
	public void setOther(TestInterface it);
			
	public default TestInterface getInterFace(Object primaryKey) throws Exception
	{
		return ( TestInterface ) getBase().getRecord((Integer)primaryKey);
	}
	
	public default void setInterFace(Object other)
	{
		getBase().getStore().setValue( getBase(), getPrimaryKey(), "other", ((TestInterface)other).getPrimaryKey());
	}
	//-- end of obsolete
	
	public default void checkName(Object name) throws ValidationFailed
	{
		if(name == null || !String.class.isAssignableFrom( name.getClass()))
		{
			throw new ValidationFailed("name", name);
		}
	}

	@Override
	public default void validate() throws ValidationFailed
	{
		checkName( getName());
	}
}
