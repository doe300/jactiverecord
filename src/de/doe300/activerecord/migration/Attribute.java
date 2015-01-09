package de.doe300.activerecord.migration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies attributes (columns) in a record-type for generation via  {@link AutomaticMigration}.
 * It is recommended to specify both {@link #type() } and {@link #typeName() }
 * @author doe300
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Attribute
{
	/**
	 * @return the name of the column in the data-source
	 */
	public String name();
	
	/**
	 * This type is the second priority to generate the mapped column.
	 * The specified {@link java.sql.Types type} is mapped to the default SQL-type.
	 * If some more precise constraints on the type are required, {@link #typeName() } should be used
	 * 
	 * @return the SQL-Type
	 * @see java.sql.Types
	 * @see #typeName() 
	 */
	public int type();
	
	/**
	 * This type is used in first priority to generate the mapped column.
	 * Use this value to specify additional constraints on the type, i.e. "varchar(200)" or "NUMBER(3,0)".
	 * If this value is not given, the {@link #type() } will be mapped to its standard type
	 * 
	 * Note: some types may not be supported by all DBMS
	 * @return the name of the SQL type
	 */
	public String typeName() default "";
	
	/**
	 * A column which is the primary key may never be null
	 * @return whether this column may be NULL
	 */
	public boolean mayBeNull() default true;
}
