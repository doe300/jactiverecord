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

import java.util.Map;

import javax.annotation.Nonnull;

import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.jdbc.VendorSpecific;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.association.AssociationHelper;

/**
 * A Condition on a table referenced by a has-many-through association.
 *
 * <p>
 * This condition matches any record in table <code>thisTable</code> which has at least one record
 * of table <code>associatedTable</code> - associated via a third table <code>associationTable</code>
 * - that matches the <code>associatedBaseCondition</code>.
 * </p>
 *
 * <pre>
 * Example:
 * |    A    |				|           C          |				|            B           |
 * -----------				------------------------				--------------------------
 * | id = 20 |		->		| fk_a = 20 | fk_b = 1 |		->		| id = 1 | name = "Adam" |
 * | id = 21 |		->		| fk_a = 21 | fk_b = 2 |		->		| id = 2 | name = "Rolf" |
 * In this example, A would be the <code>thisTable</code>, C the <code>associationTable</code> and B the <code>associatedTable</code>.
 * Furthermore, the <code>associationTableThisForeignKey</code> is <code>fk_a</code> and the <code>associationTableOtherForeignKey</code> is <code>fk_b</code>.
 * <code>thisBaseAssociationKey</code> is the column <code>id</code> of table A.
 * If the <code>associatedBaseCondition</code> matches only the first entry in B, this condition will only match the first entry in A.
 * </pre>
 *
 * @author doe300
 */
public class HasManyThroughCondition implements Condition
{
	@Nonnull
	private final String associationTable, associationTableThisForeignKey, associationTableOtherForeignKey;
	@Nonnull
	private final String thisBaseAssociationKey;
	@Nonnull
	private final ReadOnlyRecordBase<?> associatedBase, thisBase;
	@Nonnull
	private final Condition associatedBaseCondition;

	/**
	 * @param thisBase the RecordBase for this table
	 * @param associationTable the name of the association-table
	 * @param associationTableThisForeignKey the column in the <code>associationTable</code> matching this record <code>associationKeyColumn</code>
	 * @param associationTableOtherForeignKey the column in the <code>associationTable</code> matching the other records primary key
	 * @param thisBaseAssociationKey the column in this records table to match
	 * @param associatedBase the RecordBase for the associated record
	 * @param associatedBaseCondition the Condition to match in the associated table
	 */
	public HasManyThroughCondition(@Nonnull final ReadOnlyRecordBase<?> thisBase,
		@Nonnull final String associationTable, @Nonnull final String associationTableThisForeignKey,
		@Nonnull final String associationTableOtherForeignKey, @Nonnull final String thisBaseAssociationKey,
		@Nonnull final ReadOnlyRecordBase<?> associatedBase, @Nonnull final Condition associatedBaseCondition)
	{
		this.thisBase = thisBase;
		this.associationTable = associationTable;
		this.associationTableThisForeignKey = associationTableThisForeignKey;
		this.associationTableOtherForeignKey = associationTableOtherForeignKey;
		this.thisBaseAssociationKey = thisBaseAssociationKey;
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
	public boolean test( final ActiveRecord record )
	{
		if (record == null)
		{
			return false;
		}
		return AssociationHelper.getHasManyThrough( record.getPrimaryKey(), (RecordBase<?>)associatedBase, associationTable, associationTableThisForeignKey, associationTableOtherForeignKey).
			anyMatch(associatedBaseCondition);
	}

	@Override
	public boolean test( final Map<String, Object> map )
	{
		if(map == null || map.isEmpty())
		{
			return false;
		}
		return AssociationHelper.getHasManyThrough( (Integer)map.get( thisBase.getPrimaryColumn()), (RecordBase<?>)associatedBase, associationTable, associationTableThisForeignKey, associationTableOtherForeignKey).
			anyMatch(associatedBaseCondition);
	}

	@Override
	public Condition negate()
	{
		return InvertedCondition.invertCondition(this);
	}

	@Override
	public String toSQL( final VendorSpecific vendorSpecifics, final String tableName )
	{
		String associatedTableName = SQLCommand.getNextTableIdentifier( tableName );
		String associationTableName = SQLCommand.getNextTableIdentifier( associatedTableName);
		//EXISTS(SELECT primaryKey FROM associatedTable WHERE associatedTable.primaryKey IN
		// (SELECT associationTableOtherForeignKey FROM associationTable WHERE associationTable.associatioNTableThisForeignKey = thisTable.primaryKey)
		// AND cond)
		return "EXISTS("
		+ "SELECT " + associatedTableName + "."+ associatedBase.getPrimaryColumn() + " FROM " + associatedBase.getTableName()+ " AS "+ associatedTableName
				+ " WHERE " + associatedTableName + "." + associatedBase.getPrimaryColumn()+" IN ("
		+ "SELECT " + associationTableName + "." + associationTableOtherForeignKey + " FROM " + associationTable + " AS " + associationTableName
				+ " WHERE " + associationTableName + "." + associationTableThisForeignKey + " = "+ tableName + "." + thisBaseAssociationKey
		+ ") AND " + associatedBaseCondition.toSQL( vendorSpecifics, associatedTableName ) + ")";
	}

}
