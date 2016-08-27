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
package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestBase;
import de.doe300.activerecord.TestServer;
import de.doe300.activerecord.migration.ExcludeAttribute;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.association.RecordSet;
import de.doe300.activerecord.record.association.generation.BelongsTo;
import de.doe300.activerecord.record.association.generation.Has;
import de.doe300.activerecord.record.association.generation.HasManyThrough;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 * @since 0.9
 */
public class AssociationHandlerTest extends TestBase
{
	private final static String ASSOCIATION_HANDLER_MAPPING_TABLE="associationHelperMappingTable";
	private final RecordBase<TestAssociationRecord> base;
	private final TestAssociationRecord parentRecord;
	private final TestAssociationRecord child1;
	private final TestAssociationRecord child2;
	
	public AssociationHandlerTest(final RecordCore core)
	{
		super(core);
		base = core.getBase( TestAssociationRecord.class, new AssociationHandler() );
		
		parentRecord = base.createRecord();
		child1 = base.createRecord();
		child1.setParentKey( parentRecord.getPrimaryKey());
		child2 = base.createRecord();
		child2.setParentKey( parentRecord.getPrimaryKey());

		base.getStore().addRow( ASSOCIATION_HANDLER_MAPPING_TABLE, new String[]{"fk_test1", "fk_test2"}, new Object[]{parentRecord.getPrimaryKey(), child1.getPrimaryKey()});
		base.getStore().addRow( ASSOCIATION_HANDLER_MAPPING_TABLE, new String[]{"fk_test1", "fk_test2"}, new Object[]{parentRecord.getPrimaryKey(), child2.getPrimaryKey()});
	}
	
	@BeforeClass
	public static void setUp() throws Exception
	{
		TestServer.buildTestMappingTables( ASSOCIATION_HANDLER_MAPPING_TABLE );
	}
	
	@AfterClass
	public static void tearDown() throws Exception
	{
		TestServer.destroyTestMappingTables( ASSOCIATION_HANDLER_MAPPING_TABLE );
	}

	@Test
	public void testBelongsTo()
	{
		assertNotNull( child1.getParent());
		assertEquals( parentRecord, child2.getParent());
		
		assertNotNull( child1.getCustomParent());
		assertEquals( parentRecord, child2.getCustomParent());
		
		assertNull( parentRecord.getParent());
		assertNull( parentRecord.getCustomParent());
	}
	
	@Test
	public void testHasOne()
	{
		assertNotNull( parentRecord.getFirstChild());
		assertEquals( child1, parentRecord.getFirstChild());
		
		assertNotNull( parentRecord.getCustomFirstChild());
		assertEquals( child1, parentRecord.getCustomFirstChild());
		
		assertNull( child1.getFirstChild());
		assertNull( child1.getCustomFirstChild());
	}
	
	@Test
	public void testHasMany()
	{
		assertNotNull( parentRecord.getChildren());
		assertEquals( 2, parentRecord.getChildren().size());
		
		assertNotNull( parentRecord.getCustomChildren());
		assertEquals( 2, parentRecord.getCustomChildren().size());
		
		assertNotNull( child1.getChildren());
		assertNotNull( child1.getCustomChildren());
		assertTrue( child1.getChildren().isEmpty());
		assertTrue( child1.getCustomChildren().isEmpty());
	}
	
	@Test
	public void testHasManyThrough()
	{
		assertNotNull( parentRecord.getChildrenThrough());
		assertEquals( 2, parentRecord.getChildrenThrough().size());
		assertTrue( parentRecord.getChildrenThrough().contains( child1));
	}
	
	@RecordType(typeName = "testAssociations", autoCreate = true, defaultColumns = {ActiveRecord.DEFAULT_PRIMARY_COLUMN, "parent_key"})
	public interface TestAssociationRecord extends ActiveRecord
	{
		@ExcludeAttribute
		@BelongsTo(name = "parent_key", associatedType = TestAssociationRecord.class)
		public TestAssociationRecord getParent();
		
		@ExcludeAttribute
		@BelongsTo(name = "parent", associatedType = TestAssociationRecord.class, associationKey = "parent_key", associationForeignKey = ActiveRecord.DEFAULT_PRIMARY_COLUMN)
		public TestAssociationRecord getCustomParent();
		
		@ExcludeAttribute
		@Has(name = "child", associatedType = TestAssociationRecord.class, associationKey = "parent_key", isHasOne = true)
		public TestAssociationRecord getFirstChild();
		
		@ExcludeAttribute
		@Has(name = "child", associatedType = TestAssociationRecord.class, associationKey = "parent_key", isHasOne = true, associationForeignKey = ActiveRecord.DEFAULT_PRIMARY_COLUMN)
		public TestAssociationRecord getCustomFirstChild();
		
		@Has(name = "children", associatedType = TestAssociationRecord.class, associationKey = "parent_key", isHasOne = false)
		public RecordSet<TestAssociationRecord> getChildren();
		
		@Has(name = "children", associatedType = TestAssociationRecord.class, associationKey = "parent_key", isHasOne = false, associationForeignKey = ActiveRecord.DEFAULT_PRIMARY_COLUMN)
		public RecordSet<TestAssociationRecord> getCustomChildren();
		
		@HasManyThrough(name = "children", mappingTable = ASSOCIATION_HANDLER_MAPPING_TABLE, associatedType = TestAssociationRecord.class, mappingTableAssociatedKey = "fk_test2", mappingTableThisKey = "fk_test1")
		public RecordSet<TestAssociationRecord> getChildrenThrough();
		
		public int getParentKey();
		public void setParentKey(int key);
	}
}
