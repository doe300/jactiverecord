package de.doe300.activerecord.record;

import de.doe300.activerecord.dsl.Order;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The data-set or table-name
 * @author doe300
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordType
{
	/**
	 * If the value is empty, the name of the class will be used
	 * @return the name of the data-set
	 */
	public String typeName();
	
	/**
	 * The primaryKey-column MUST be an integer and auto-increment
	 * @return the name of the primary key
	 */
	public String primaryKey();
	
	/**
	 * Default columns are always loaded into cache and therefore faster to access
	 * @return the list of default columns
	 */
	public String[] defaultColumns();

	/**
	 * Default order of rows for retrieval operations, defaults to ordering by primary key ascending.
	 * @return the default order, as SQL ORDER BY clause
	 * @see Order
	 */
	public String defaultOrder() default "";
}
