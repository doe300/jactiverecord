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
import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.association.AssociationHelper;

/**
 * A Condition on another table referenced by a has-one or has-many association
 * @author doe300
 */
public class HasOneCondition implements Condition
{
	@Nonnull
	private final String associationKeyColumn;
	@Nonnull
	private final ReadOnlyRecordBase<?> associatedTableBase;
	@Nonnull
	private final String associatedTableForeignKeyColumn;
	@Nonnull
	private final Condition associatedTableCond;

	/**
	 *
	 * @param associationKeyColumn the column of this table to match by the <code>associatedTableforeignKeyColumn</code>
	 * @param associatedTableBase the base of the associated table
	 * @param associatedTableForeignKeyColumn the column of the associated table matching the <code>associationKeyColumn</code>
	 * @param associatedTableCond the condition to match in the associated table
	 */
	public HasOneCondition(@Nonnull final String associationKeyColumn,
		@Nonnull final ReadOnlyRecordBase<?> associatedTableBase, @Nonnull final String associatedTableForeignKeyColumn,
		@Nonnull final Condition associatedTableCond)
	{
		this.associationKeyColumn = associationKeyColumn;
		this.associatedTableBase = associatedTableBase;
		this.associatedTableForeignKeyColumn = associatedTableForeignKeyColumn;
		this.associatedTableCond = associatedTableCond;
	}


	@Override
	public boolean hasWildcards()
	{
		return associatedTableCond.hasWildcards();
	}

	@Override
	public Object[] getValues()
	{
		return associatedTableCond.getValues();
	}

	@Override
	public String toSQL(final JDBCDriver driver, final String tableName)
	{
		String tableID = tableName != null ? tableName + "." : "";
		String associatedTableName = JDBCDriver.getNextTableIdentifier( tableName );
		//EXISTS(SELECT associatedForeignKey FROM associatedTable WHERE thisTable.associationKey = associatedTable.associatedForeignKey AND cond)
		return "EXISTS (SELECT "+associatedTableName+"."+associatedTableForeignKeyColumn+" FROM "+associatedTableBase.getTableName()+" AS "+associatedTableName
		+ " WHERE "+associatedTableName+"."+associatedTableForeignKeyColumn+" = "+tableID+associationKeyColumn+" AND "+associatedTableCond.toSQL(driver, associatedTableName)+")";
	}

	@Override
	public boolean test( final ActiveRecord record )
	{
		if (record == null)
		{
			return false;
		}
		final ActiveRecord associatedRecord = AssociationHelper.getHasOne( record.getPrimaryKey(), associatedTableBase, associatedTableForeignKeyColumn);
		if (associatedRecord == null)
		{
			return false;
		}
		return associatedTableCond.test( associatedRecord);
	}

	@Override
	public boolean test( final Map<String, Object> map )
	{
		if (map.isEmpty())
		{
			return false;
		}
		final ActiveRecord associatedRecord = AssociationHelper.getHasOne( map.get( associationKeyColumn), associatedTableBase, associatedTableForeignKeyColumn);
		if (associatedRecord == null)
		{
			return false;
		}
		return associatedTableCond.test( associatedRecord);
	}

	@Override
	public boolean equals( Object obj )
	{
		if(obj == null || !(obj instanceof Condition))
		{
			return false;
		}
		return equals( (Condition)obj);
	}
	
	@Override
	public int hashCode()
	{
		return toSQL( JDBCDriver.DEFAULT, null ).hashCode();
	}
}