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
package de.doe300.activerecord.dsl;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.jdbc.VendorSpecific;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.association.AssociationHelper;
import java.util.Map;

/**
 * A Condition on a table referenced by a has-many-through association
 * @author doe300
 */
public class HasManyThroughCondition implements Condition
{
	private final String associationTable, associationTableThisForeignKey, associationTableOtherForeignKey;
	private final String associationKeyColumn;
	private final RecordBase<?> associatedBase;
	private final Condition associatedBaseCondition;

	/**
	 * 
	 * @param associationTable the name of the association-table
	 * @param associationTableThisForeignKey the column in the <code>associationTable</code> matching this record <code>associationKeyColumn</code>
	 * @param associationTableOtherForeignKey the column in the <code>associationTable</code> matching the other records primary key
	 * @param associationKeyColumn the column in this records table to match
	 * @param associatedBase the RecordBase for the associated record
	 * @param associatedBaseCondition the Condition to match in the associated table
	 */
	public HasManyThroughCondition( String associationTable, String associationTableThisForeignKey,
			String associationTableOtherForeignKey, String associationKeyColumn,
			RecordBase<?> associatedBase, Condition associatedBaseCondition )
	{
		this.associationTable = associationTable;
		this.associationTableThisForeignKey = associationTableThisForeignKey;
		this.associationTableOtherForeignKey = associationTableOtherForeignKey;
		this.associationKeyColumn = associationKeyColumn;
		this.associatedBase = associatedBase;
		this.associatedBaseCondition = associatedBaseCondition;
	}

	
	@Override
	public boolean hasWildcards()
	{
		return associatedBaseCondition.hasWildcards();
	}

	@Override
	public Object[] getValues()
	{
		return associatedBaseCondition.getValues();
	}

	@Override
	public boolean test( ActiveRecord record )
	{
		return AssociationHelper.getHasManyThrough( record, associatedBase.getRecordType(), associationTable, associationTableThisForeignKey, associationTableOtherForeignKey).
				anyMatch(associatedBaseCondition);
	}

	@Override
	public boolean test( Map<String, Object> map )
	{
		throw new UnsupportedOperationException( "Can't resolve an AssociationCondition from column-values!" );
	}

	@Override
	public Condition negate()
	{
		return new InvertedCondition(this);
	}

	@Override
	public String toSQL( VendorSpecific vendorSpecifics )
	{
		//EXISTS(SELECT primaryKey FROM associatedTable WHERE associatedTable.primaryKey IN 
			// (SELECT associationTableOtherForeignKey FROM associationTable WHERE associationTable.associatioNTableThisForeignKey = thisTable.primaryKey)
		// AND cond)
		return "EXISTS("
				+ "SELECT " + associatedBase.getPrimaryColumn() + " FROM " + associatedBase.getTableName()+" WHERE " + associatedBase.getPrimaryColumn()+" IN ("
					+ "SELECT " + associationTableOtherForeignKey + " FROM " + associationTable + " WHERE " + associationTable + "." + associationTableThisForeignKey + " = " + associationKeyColumn
				+ ") AND " + associatedBaseCondition.toSQL( vendorSpecifics ) + ")";
	}

}
