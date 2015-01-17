package de.doe300.activerecord.record;

import de.doe300.activerecord.RecordBase;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate your record-type with this annotation to enable support for searching for records.
 * @author doe300
 * @see RecordBase#isSearchable() 
 * @see RecordBase#search(java.lang.String) 
 * @see RecordBase#searchFirst(java.lang.String) 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Searchable
{
	/**
	 * @return a list of columns to be used for searching
	 */
	public String[] searchableColumns();
}
