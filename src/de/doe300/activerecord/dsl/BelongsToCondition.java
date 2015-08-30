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
import javax.annotation.Nullable;

import de.doe300.activerecord.ReadOnlyRecordBase;
import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.jdbc.VendorSpecific;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.association.AssociationHelper;

/**
 * A Condition on another table referenced by a belongs-to association
 * @author doe300
 */
public class BelongsToCondition implements Condition
{
	@Nonnull
	private final String foreignKeyColumn, associatedTableKey;
	@Nonnull
	private final Condition associatedTableCond;
	@Nonnull
	private final ReadOnlyRecordBase<?> associatedTableBase;

	/**
	 *
	 * @param foreignKeyColumn the name of the local column containing the foreign key
	 * @param associatedTableBase the base of the associated table
	 * @param associatedTableKey the column of the associated table referenced by the foreign key
	 * @param associatedTableCond the condition to test in the associated table
	 */
	public BelongsToCondition(@Nonnull final String foreignKeyColumn,
		@Nonnull final ReadOnlyRecordBase<?> associatedTableBase, @Nonnull final String associatedTableKey,
		@Nonnull final Condition associatedTableCond)
	{
		this.foreignKeyColumn = foreignKeyColumn;
		this.associatedTableBase = associatedTableBase;
		this.associatedTableKey = associatedTableKey;
		this.associatedTableCond = associatedTableCond;
	}

	/**
	 * This constructor uses the associated table's primary key as foreign Key
	 * @param foreignKeyColumn the name of the local column containing the foreign key
	 * @param associatedTableBase the base of the associated table
	 * @param associatedTableCond the condition to test in the associated table
	 * @see RecordBase#getPrimaryColumn()
	 */
	public BelongsToCondition(@Nonnull final String foreignKeyColumn, @Nonnull final RecordBase<?> associatedTableBase,
		@Nonnull final Condition associatedTableCond)
	{
		this(foreignKeyColumn, associatedTableBase, associatedTableBase.getPrimaryColumn(), associatedTableCond);
	}



	@Override
	public boolean hasWildcards()
	{
		return associatedTableCond.hasWildcards();
	}

	@Override
	@Nullable
	public Object[] getValues()
	{
		return associatedTableCond.getValues();
	}

	@Override
	public String toSQL(@Nullable final VendorSpecific vendorSpecifics, final String tableName)
	{
		String associatedTableName = SQLCommand.getNextTableIdentifier( tableName );
		//foreignKey IN(SELECT associatedKey FROM associatedTable WHERE cond)
		//could also be written as:
		//EXISTS(SELECT associatedKey FROM associatedTable WHERE thisTable.foreignKey = associatedKey AND cond)
		return tableName + "." + foreignKeyColumn+" IN (SELECT " + associatedTableName + "." + associatedTableKey 
				+ " FROM " + associatedTableBase.getTableName() + " AS " + associatedTableName 
				+ " WHERE " + associatedTableCond.toSQL(vendorSpecifics, associatedTableName) + ")";
	}

	@Override
	public boolean test(final ActiveRecord record )
	{
		if (record == null)
		{
			return false;
		}
		final ActiveRecord associatedRecord = AssociationHelper.getBelongsTo( record, associatedTableBase.getRecordType(), foreignKeyColumn);
		if (associatedRecord == null)
		{
			return false;
		}
		return associatedTableCond.test(associatedRecord);
	}

	@Override
	public boolean test(final Map<String, Object> map )
	{
		throw new UnsupportedOperationException( "Can't resolve an AssociationCondition from column-values!" );
	}

	@Override
	public Condition negate()
	{
		return InvertedCondition.invertCondition(this );
	}
}
