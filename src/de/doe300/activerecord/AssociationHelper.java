package de.doe300.activerecord;

import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.SimpleCondition;
import java.util.stream.Stream;

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
	public static <T extends ActiveRecord> T getBelongsTo(ActiveRecord record, Class<T> type, String foreignKeyColumn)
	{
		Integer foreignKey = ( Integer ) record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), foreignKeyColumn);
		if(foreignKey==null)
		{
			return null;
		}
		return record.getBase().getCore().buildBase( type).getRecord(foreignKey);
	}
	
	/**
	 * In a belongs-to association, the foreign key is stored in the table represented by <code>record</code>
	 * @param record
	 * @param otherRecord
	 * @param foreignKeyColumn the column (of this model), the foreign key of <code>otherRecord</code> is stored
	 */
	public static void setBelongsTo(ActiveRecord record, ActiveRecord otherRecord, String foreignKeyColumn)
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
	public static <T extends ActiveRecord> T getHasOne(ActiveRecord record, Class<T> type, String foreignKeyColumn)
	{
		RecordBase<T> base = record.getBase().getCore().buildBase( type);
		return base.findFirst( new SimpleCondition(foreignKeyColumn, record.getPrimaryKey(), Comparison.IS));
	}
	
	/**
	 * In a has-one association, the other model represented by <code>otherRecord</code> stores the foreign-key to this model represented by <code>record</code>
	 * @param record
	 * @param otherRecord
	 * @param foreignKeyColumn the column in the other model storing the foreign-key for the given <code>record</code> of this model
	 */
	public static void setHasOne(ActiveRecord record, ActiveRecord otherRecord, String foreignKeyColumn )
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
	public static <T extends ActiveRecord> T getHasOne(ActiveRecord record, Class<T> type, Condition cond)
	{
		return record.getBase().getCore().buildBase( type ).findFirst( cond );
	}

	/**
	 * This given <code>record</code> has many associated records having this record's primary-key in the column specified in <code>foreignKeyColumn</code>
	 * @param <T>
	 * @param record
	 * @param type
	 * @param foreignKeyColumn the name of the column of the other model holding the primary key
	 * @return the associated records
	 */
	public static <T extends ActiveRecord> Stream<T> getHasMany(ActiveRecord record, Class<T> type, String foreignKeyColumn)
	{
		RecordBase<T> base = record.getBase().getCore().buildBase( type);
		return base.find( new SimpleCondition(foreignKeyColumn, record.getPrimaryKey(), Comparison.IS));
	}
	
	/**
	 * This method sets the column <code>foreignKeyColumn</code> of the <code>otherRecord</code> to the primary-key of <code>record</code>
	 * @param record
	 * @param otherRecord
	 * @param foreignKey the name of the column of the other model holding the primary key
	 */
	public static void addHasMany(ActiveRecord record, ActiveRecord otherRecord, String foreignKey)
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
	public static <T extends ActiveRecord> Stream<T> getHasMany(ActiveRecord record, Class<T> type, Condition cond)
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
	public static <T extends ActiveRecord> Stream<T> getHasManyThrough(ActiveRecord record, Class<T> type, String associationTable, String thisForeignKeyColumn, String otherForeignKeyColumn)
	{
		RecordBase<T> otherBase = record.getBase().getCore().getBase( type );
		Stream<Object> foreignKeys = record.getBase().getStore().getValues( associationTable, otherForeignKeyColumn,
				thisForeignKeyColumn, record.getPrimaryKey());
		if(foreignKeys!=null)
		{
			return foreignKeys.filter( (Object obj) -> obj!=null ).map( (Object obj)->
			{
				try
				{
					return otherBase.getRecord( (Integer)obj);
				}
				catch ( Exception ex )
				{
					return null;
				}
			}).filter( (T t) -> t!=null);
		}
		return Stream.empty();
	}
	
	/**
	 * This helper is used to set the record <code>otherRecord</code> associated with <code>record</code> in a has-many through (the <code>associationTable</code>) association.
	 * @param record
	 * @param otherRecord
	 * @param associationTable the table storing pairs of foreign keys to both models
	 * @param thisForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to <code>record</code>
	 * @param otherForeignKeyColumn the name of the column in the <code>associationTable</code> storing the foreign key to the other model defined by <code>type</code>
	 */
	public static void addHasManyThrough(ActiveRecord record, ActiveRecord otherRecord, String associationTable, String thisForeignKeyColumn, String otherForeignKeyColumn)
	{
		//TODO need to add row
	}
	//TODO missing: remove row for mapping-table
	
	private AssociationHelper()
	{
	}
}
