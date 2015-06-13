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
 * A Condition on another table referenced by a belongs-to association
 * @author doe300
 */
public class BelongsToCondition implements Condition
{
	private final String foreignKeyColumn, associatedTableKey;
	private final Condition associatedTableCond;
	private final RecordBase<?> associatedTableBase;

	/**
	 * 
	 * @param foreignKeyColumn the name of the local column containing the foreign key
	 * @param associatedTableBase the base of the associated table
	 * @param associatedTableKey the column of the associated table referenced by the foreign key
	 * @param associatedTableCond the condition to test in the associated table
	 */
	public BelongsToCondition( String foreignKeyColumn, RecordBase<?> associatedTableBase, String associatedTableKey,
			Condition associatedTableCond )
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
	public BelongsToCondition(String foreignKeyColumn, RecordBase<?> associatedTableBase, Condition associatedTableCond )
	{
		this(foreignKeyColumn, associatedTableBase, associatedTableBase.getPrimaryColumn(), associatedTableCond);
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
	public String toSQL(VendorSpecific vendorSpecifics)
	{
		//TODO which version is more performant??
		//EXISTS(SELECT associatedKey FROM associatedTable WHERE thisTable.foreignKey = associatedKey AND cond)
		//foreignKey IN(SELECT associatedKey FROM associatedTable WHERE cond)
		//for now choosing the seconds, because the subquery is independant and could be cached easier, I think
		return foreignKeyColumn+" IN (SELECT "+associatedTableKey+" FROM "+associatedTableBase.getTableName()+" WHERE "+associatedTableCond.toSQL(vendorSpecifics)+")";
	}
	
	@Override
	public boolean test( ActiveRecord record )
	{
		ActiveRecord associatedRecord = AssociationHelper.getBelongsTo( record, associatedTableBase.getRecordType(), foreignKeyColumn);
			return associatedTableCond.test( associatedRecord);
		}

	@Override
	public boolean test( Map<String, Object> map )
	{
		throw new UnsupportedOperationException( "Can't resolve an AssociationCondition from column-values!" );
	}

	@Override
	public Condition negate()
	{
		return new InvertedCondition(this );
	}
}
