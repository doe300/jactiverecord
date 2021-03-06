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
package de.doe300.activerecord.migration;

import de.doe300.activerecord.migration.constraints.ReferenceRule;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies attributes (columns) in a record-type for generation via  {@link AutomaticMigration}.
 * It is recommended to specify both {@link #type() } and {@link #typeName() }.
 * <p>
 * Currently is able to generate this column definition:
 * <br>
 * <pre>
 * name							- the column_name as of #name()
 *   type							- the data-type, either #typeName() or the SQL type for #type()
 *     (NULL|NOT NULL)					- the result of #mayBeNull()
 *     [UNIQUE]						- if #isUnique() is set to true
 *     [DEFAULT defaultValue]				- if a #defaultValue() is set
 *     [REFERENCES referenceTable				- the #foreignKeyTable(), if set
 *       [(referenceColumn)]				- the #foreignKeyColumn(), if set
 *       [ON UPDATE (CASCADE|SET DEFAULT|SET NULL)]	- the value of #onUpdate(), if any other than NONE
 *       [ON DELETE (CASCADE|SET DEFAULT|SET NULL)]	- the value of #onDelete(), if any other than NONE
 *     ]
 *     [CHECK(constraint)]				- the SQL clause in #checkConstraint() unless empty
 * </pre>
 * </p>
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
	 * The specified {@link Class type} is mapped to the default SQL-type.
	 * If some more precise constraints on the type are required, {@link #typeName() } should be used
	 * 
	 * By default, this type is set to {@link Void}. If neither {@link #typeName()} nor {@link #type()} are set,
	 * the attribute deduces its type from the annotated method.
	 * If the method is a getter (non-void return-type), the return-type is used.
	 * If the method is a setter (single argument, void return-type), the argument-type is used.
	 * 
	 * @return the java-Type
	 * @see #typeName() 
	 */
	public Class<?> type() default Void.class;
	
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
	
	/**
	 * @return the default value, as SQL text
	 */
	public String defaultValue() default "";
	
	/**
	 * All cells in an unique column must have different values
	 * @return whether this column is UNIQUE
	 */
	public boolean isUnique() default false;
	
	/**
	 * @return table to reference as a FOREIGN KEY
	 */
	public String foreignKeyTable() default "";
	
	/**
	 * @return the column to reference as a FOREIGN KEY
	 */
	public String foreignKeyColumn() default "";
	
	/**
	 * @return the rule for UPDATE of the associated row for this FOREIGN KEY
	 */
	public ReferenceRule onUpdate() default ReferenceRule.NONE;

	/**
	 * @return the rule for DELETE called on the associated row of this FOREIGN KEY
	 */
	public ReferenceRule onDelete() default ReferenceRule.NONE;

	/**
	 * If defined (any value other than an empty string), the given value will be added as CHECK-constraint to the column
	 * @return the SQL constraint to apply
	 */
	public String checkConstraint() default "";
}
