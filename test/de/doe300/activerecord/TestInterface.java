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

import de.doe300.activerecord.record.association.AssociationHelper;
import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.migration.constraints.Index;
import de.doe300.activerecord.migration.constraints.IndexType;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.RecordCallbacks;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.validation.Validate;
import de.doe300.activerecord.record.validation.ValidatedRecord;
import de.doe300.activerecord.record.validation.Validates;
import de.doe300.activerecord.record.validation.ValidationFailed;
import de.doe300.activerecord.record.validation.ValidationType;
import java.sql.Types;
import java.util.stream.Stream;

/**
 *
 * @author doe300
 */
@Searchable(searchableColumns = {"name", "age"})
@RecordType(typeName = "TESTTABLE", primaryKey = "id", defaultColumns = {"id", "name", "age"})
@Validates({
	@Validate(attribute = "age", type = ValidationType.POSITIVE),
	@Validate(attribute = "name", type = ValidationType.NOT_NULL),
	@Validate(attribute = "name", type = ValidationType.NOT_EMPTY)
})
@Index(type = IndexType.NON_UNIQUE, name = "fk_other", columns = {"other"})
public interface TestInterface extends TimestampedRecord, ValidatedRecord, RecordCallbacks
{
	public String getName();
	
	@AttributeSetter(name = "name", validatorClass = TestInterface.class, validatorMethod = "checkName")
	public void setName(String name) throws ValidationFailed;
	
	public int getAge();
	
	public void setAge(int age);

	@Attribute(name = "fk_test_id", type = Types.INTEGER, foreignKeyTable = "testtable", foreignKeyColumn = "id")
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
	
	public ValidationType getTestEnum();
}
