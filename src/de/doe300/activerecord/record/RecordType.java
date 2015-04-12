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
package de.doe300.activerecord.record;

import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.migration.AutomaticMigration;
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
	public String primaryKey() default ActiveRecord.DEFAULT_PRIMARY_COLUMN;
	
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
	
	/**
	 * Any record-type with <code>autoCreate</code> set to <code>true</code> will automatically create its corresponding table in case it doesn't exists.
	 * The creation is executed on the first creation of a new record.
	 * NOTE: the creation is only guaranteed, if the data-store is accessed via its RecordBase.
	 * NOTE: if no {@link #autoCreateSQL() } is given, the table will be created via {@link AutomaticMigration} so all of its limitations apply
	 * @return whether to automatically create the corresponding table
	 * @see AutomaticMigration
	 * @see #autoCreateSQL() 
	 */
	public boolean autoCreate() default false;
	
	/**
	 * A full SQL statement to automatically create the table, if {@link #autoCreate() } is set to <code>true</code>.
	 * If this statement is not set, the table will be created via {@link AutomaticMigration}.
	 * @return the SQL statement to create the table
	 * @see #autoCreate() 
	 */
	public String autoCreateSQL() default "";
}
