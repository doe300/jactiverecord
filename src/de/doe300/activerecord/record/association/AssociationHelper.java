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
package de.doe300.activerecord.record.association;

import de.doe300.activerecord.ReadOnlyRecordBase;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.AndCondition;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.SimpleCondition;
import de.doe300.activerecord.record.ActiveRecord;
import javax.annotation.Nonnegative;

/**
 * Helper methods to be used by {@link ActiveRecord} implementations for mapping associations
 * @author doe300
 */
public final class AssociationHelper
{
	/**
	 * In a belongs-to association, the foreign key is stored in the table represented by <code>record</code>
	 * @param <T>
	 * @param record
	 * @param type
	 * @param foreignKeyColumn the column (of this model), the foreign-key for the other model is stored
	 * @return the associated record or <code>null</code>
	 */
	@Nullable
	public static <T extends ActiveRecord> T getBelongsTo(@Nonnull final ActiveRecord record,
		@Nonnull final Class<T> type, @Nonnull final String foreignKeyColumn)
	{
		final Integer foreignKey = ( Integer ) record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), foreignKeyColumn);
		if(foreignKey == null)
		{
			return null;
		}
		RecordBase<T> otherBase = record.getBase().getCore().getBase( type);
		return getBelongsTo( foreignKey, otherBase, otherBase.getPrimaryColumn() );
	}
	
	/**
	 * In a belongs-to association, the foreign key is stored in the table represented by <code>record</code>
	 * @param <T>
	 * @param thisAssociationValue the association-value of this record
	 * @param otherBase the record-base of the associated record-type
	 * @param otherKeyColumn the column (of the other model) to be matched with <code>thisAssociationValue</code>
	 * @return the associated record or <code>null</code>
	 * @since 0.3
	 */
	@Nullable
	public static <T extends ActiveRecord> T getBelongsTo(@Nonnull final Object thisAssociationValue,
		@Nonnull final ReadOnlyRecordBase<T> otherBase, @Nonnull final String otherKeyColumn)
	{
		return otherBase.findFirstFor( otherKeyColumn, thisAssociationValue );
	}

	/**
	 * In a belongs-to association, the foreign key is stored in the table represented by <code>record</code>
	 * @param record
	 * @param otherRecord
	 * @param foreignKeyColumn the column (of this model), the foreign key of <code>otherRecord</code> is stored
	 */
	public static void setBelongsTo(@Nonnull final ActiveRecord record, @Nonnull final ActiveRecord otherRecord,
		@Nonnull final String foreignKeyColumn)
	{
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), foreignKeyColumn, otherRecord.getPrimaryKey());
	}

	/**
	 * In a has-one association, the other model represented by <code>type</code> stores the foreign-key to this model represented by <code>record</code>
	 * @param <T>
	 * @param record
	 * @param type
	 * @param foreignKeyColumn the column in the other model storing the foreign-key for the given <code>record</code> of this model
	 * @return the associated record or <code>null</code>
	 */
	@Nullable
	public static <T extends ActiveRecord> T getHasOne(@Nonnull final ActiveRecord record, @Nonnull final Class<T> type,
		@Nonnull final String foreignKeyColumn)
	{
		final RecordBase<T> base = record.getBase().getCore().getBase( type);
		return getHasOne( record.getPrimaryKey(), base, foreignKeyColumn);
	}
	
	/**
	 * In a has-one association, the other model represented by <code>type</code> stores the foreign-key to this model represented by <code>record</code>
	 * @param <T>
	 * @param thisAssociationValue the value of this record to be matched by the associated record
	 * @param otherBase the record-base of the result record-type
	 * @param foreignKeyColumn the column in the other model storing the foreign-key for the given <code>record</code> of this model
	 * @return the associated record or <code>null</code>
	 * @since 0.3
	 */
	@Nullable
	public static <T extends ActiveRecord> T getHasOne(@Nonnull final Object thisAssociationValue, @Nonnull final ReadOnlyRecordBase<T> otherBase,
		@Nonnull final String foreignKeyColumn)
	{
		return otherBase.findFirst( new SimpleCondition(foreignKeyColumn, thisAssociationValue, Comparison.IS));
	}

	/**
	 * In a has-one association, the other model represented by <code>otherRecord</code> stores the foreign-key to this model represented by <code>record</code>
	 * @param record
	 * @param otherRecord
	 * @param foreignKeyColumn the column in the other model storing the foreign-key for the given <code>record</code> of this model
	 */
	public static void setHasOne(@Nonnull final ActiveRecord record, @Nonnull final ActiveRecord otherRecord,
		@Nonnull final String foreignKeyColumn)
	{
		otherRecord.getBase().getStore().setValue( otherRecord.getBase(), otherRecord.getPrimaryKey(), foreignKeyColumn, record.getPrimaryKey());
	}

	/**
	 * The given record has an associated record matching the conditions applied in the other model
	 * @param <T>
	 * @param record
	 * @param type
	 * @param cond
	 * @return the associated record or <code>null</code>
	 */
	@Nullable
	public static <T extends ActiveRecord> T getHasOne(@Nonnull final ActiveRecord record, @Nonnull final Class<T> type,
		@Nullable final Condition cond)
	{
		return record.getBase().getCore().getBase( type ).findFirst( cond );
	}

	/**
	 * This given <code>record</code> has many associated records having this record's primary-key in the column specified in <code>foreignKeyColumn</code>
	 * @param <T>
	 * @param record
	 * @param type
	 * @param foreignKeyColumn the name of the column of the other model holding the primary key
	 * @return the associated records
	 */
	@Nonnull
	public static <T extends ActiveRecord> Stream<T> getHasMany(@Nonnull final ActiveRecord record,
		@Nonnull final Class<T> type, @Nonnull final String foreignKeyColumn)
	{
		final RecordBase<T> base = record.getBase().getCore().getBase( type);
		return base.find( new SimpleCondition(foreignKeyColumn, record.getPrimaryKey(), Comparison.IS));
	}

	/**
	 * This given <code>record</code> has many associated records having this record's primary-key in the column specified in <code>foreignKeyColumn</code>
	 * @param <T>
	 * @param record
	 * @param type
	 * @param foreignKeyColumn the name of the column of the other model holding the primary key
	 * @return the associated records as modifiable set
	 */
	@Nonnull
	public static <T extends ActiveRecord> RecordSet<T> getHasManySet(@Nonnull final ActiveRecord record,
		@Nonnull final Class<T> type, @Nonnull final String foreignKeyColumn)
	{
		final RecordBase<T> base = record.getBase().getCore().getBase( type);
		final Condition cond = new SimpleCondition(foreignKeyColumn, record.getPrimaryKey(), Comparison.IS);
		final Consumer<T> setAssoc = (final T t) -> base.getStore().setValue( base, t.getPrimaryKey(), foreignKeyColumn, record.getPrimaryKey() );
		final Consumer<T> unsetAssoc = (final T t) -> base.getStore().setValue( base, t.getPrimaryKey(), foreignKeyColumn, null );
		return new HasManyAssociationSet<T>(base, cond, setAssoc, unsetAssoc );
	}

	/**
	 * This method sets the column <code>foreignKeyColumn</code> of the <code>otherRecord</code> to the primary-key of <code>record</code>
	 * @param record
	 * @param otherRecord
	 * @param foreignKey the name of the column of the other model holding the primary key
	 */
	public static void addHasMany(@Nonnull final ActiveRecord record, @Nonnull final ActiveRecord otherRecord,
		@Nonnull final String foreignKey)
	{
		otherRecord.getBase().getStore().setValue( otherRecord.getBase(), otherRecord.getPrimaryKey(), foreignKey, record.getPrimaryKey());
	}

	/**
	 * This given <code>record</code> has many associated records applying to the given conditions
	 * @param <T>
	 * @param record
	 * @param type
	 * @param cond
	 * @return the associated records
	 */
	@Nonnull
	public static <T extends ActiveRecord> Stream<T> getHasMany(@Nonnull final ActiveRecord record,
		@Nonnull final Class<T> type, @Nullable final Condition cond)
	{
		return record.getBase().getCore().getBase( type ).find( cond );
	}

	/**
	 * This helper is used to retrieve all associated records of the other model defined by <code>type</code> in a has-many through (the <code>associationTable</code>) association.
	 * @param <T>
	 * @param record
	 * @param type the type of the other model
	 * @param associationTable the table storing pairs of foreign keys to both models
	 * @param thisForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to <code>record</code>
	 * @param otherForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to the other model defined by <code>type</code>
	 * @return all matching associations or an empty stream
	 */
	@Nonnull
	public static <T extends ActiveRecord> Stream<T> getHasManyThrough(@Nonnull final ActiveRecord record,
		@Nonnull final Class<T> type, @Nonnull final String associationTable,
		@Nonnull final String thisForeignKeyColumn, @Nonnull final String otherForeignKeyColumn)
	{
		return getHasManyThrough( record.getPrimaryKey(), record.getBase().getCore().getBase( type), associationTable, thisForeignKeyColumn, otherForeignKeyColumn);
	}
	
	/**
	 * This helper is used to retrieve all associated records of the other model defined by <code>type</code> in a has-many through (the <code>associationTable</code>) association.
	 * @param <T>
	 * @param thisPrimaryKey the primary key of this record
	 * @param otherBase the record-base of the other record-type
	 * @param associationTable the table storing pairs of foreign keys to both models
	 * @param thisForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to <code>record</code>
	 * @param otherForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to the other model defined by <code>type</code>
	 * @return all matching associations or an empty stream
	 * @since 0.3
	 */
	@Nonnull
	public static <T extends ActiveRecord> Stream<T> getHasManyThrough(@Nonnegative final int thisPrimaryKey, 
			@Nonnull final RecordBase<T> otherBase, @Nonnull final String associationTable,
		@Nonnull final String thisForeignKeyColumn, @Nonnull final String otherForeignKeyColumn)
	{
		final Stream<Object> foreignKeys = otherBase.getStore().getValues( associationTable, otherForeignKeyColumn,
				thisForeignKeyColumn, thisPrimaryKey);
		return foreignKeys.filter((final Object obj) -> obj != null).map((final Object obj) ->
		{
			try
			{
				return otherBase.getRecord((Integer) obj);
			}
			catch (final Exception ex)
			{
				return null;
			}
		}).filter((final T t) -> t != null);
	}


	/**
	 * This helper is used to retrieve all associated records of the other model defined by <code>type</code> in a has-many through (the <code>associationTable</code>) association.
	 * @param <T>
	 * @param record
	 * @param type the type of the other model
	 * @param associationTable the table storing pairs of foreign keys to both models
	 * @param thisForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to <code>record</code>
	 * @param otherForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to the other model defined by <code>type</code>
	 * @return all matching associations as a modifiable Set
	 */
	@Nonnull
	public static <T extends ActiveRecord> RecordSet<T> getHasManyThroughSet(@Nonnull final ActiveRecord record,
		@Nonnull final Class<T> type, @Nonnull final String associationTable,
		@Nonnull final String thisForeignKeyColumn, @Nonnull final String otherForeignKeyColumn)
	{
		final RecordBase<T> otherBase = record.getBase().getCore().getBase( type );
		return new HasManyThroughAssociationSet<T>(otherBase, record.getPrimaryKey(), associationTable, thisForeignKeyColumn,otherForeignKeyColumn);
	}

	/**
	 * This helper is used to set the record <code>otherRecord</code> associated with <code>record</code> in a has-many through (the <code>associationTable</code>) association.
	 * @param record
	 * @param otherRecord
	 * @param associationTable the table storing pairs of foreign keys to both models
	 * @param thisForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to <code>record</code>
	 * @param otherForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to the other model defined by <code>type</code>
	 * @return whether the association was added
	 */
	public static boolean addHasManyThrough(@Nonnull final ActiveRecord record, @Nonnull final ActiveRecord otherRecord,
		@Nonnull final String associationTable, @Nonnull final String thisForeignKeyColumn,
		@Nonnull final String otherForeignKeyColumn)
	{
		return record.getBase().getStore().addRow( associationTable, new String[]{thisForeignKeyColumn,otherForeignKeyColumn}, new Object[]{record.getPrimaryKey(),otherRecord.getPrimaryKey()});
	}

	/**
	 * This helper is used to remove the association between the two records in a has-many through (the <code>associationTable</code>) association.
	 * @param record
	 * @param otherRecord
	 * @param associationTable the table storing pairs of foreign keys to both models
	 * @param thisForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to <code>record</code>
	 * @param otherForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to the other model defined by <code>type</code>
	 * @return whether the association was removed
	 */
	public static boolean removeHasManyThrough(@Nonnull final ActiveRecord record,
		@Nonnull final ActiveRecord otherRecord, @Nonnull final String associationTable,
		@Nonnull final String thisForeignKeyColumn, @Nonnull final String otherForeignKeyColumn)
	{
		final Condition cond = AndCondition.andConditions(
				new SimpleCondition(thisForeignKeyColumn, record.getPrimaryKey(), Comparison.IS),
				new SimpleCondition(otherForeignKeyColumn, otherRecord.getPrimaryKey(), Comparison.IS)
		);
		return record.getBase().getStore().removeRow( associationTable, cond );
	}

	/**
	 * Creates a set containing all matching records of the given type
	 * @param <T> the record-type
	 * @param base the record-base
	 * @param conditionColumn the column to check
	 * @param conditionValue the value to match (exact)
	 * @param resetValue the value to set for any record, to be removed from this Set
	 * @return the set containing all matching records
	 */
	@Nonnull
	public static <T extends ActiveRecord> RecordSet<T> getConditionSet(@Nonnull final RecordBase<T> base,
		@Nonnull final String conditionColumn, @Nullable final Object conditionValue, @Nullable final Object resetValue)
	{
		final Consumer<T> setCondFunc = (final T t) -> {
			base.getStore().setValue( base, t.getPrimaryKey(), conditionColumn, conditionValue);
		};
		final Consumer<T> unsetCondFunc = (final T t) -> {
			base.getStore().setValue( base, t.getPrimaryKey(), conditionColumn, resetValue);
		};
		final Condition cond = new SimpleCondition(conditionColumn, conditionValue, Comparison.IS);
		return new ConditionSet<T>(base, cond, setCondFunc, unsetCondFunc);
	}

	private AssociationHelper()
	{
	}
}
