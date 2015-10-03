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
package de.doe300.activerecord.generation;

import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.migration.constraints.ReferenceRule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to generate getter- and setter-methods for the given attribute
 * 
 * @author doe300
 * @see Attribute
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Repeatable(AddAttributes.class)
public @interface AddAttribute
{
	/**
	 * This name will be the name of the column and also the methods
	 * @return the name of the attribute
	 * @see Attribute#name() 
	 */
	public String name();
	
	/**
	 * The <code>type</code> determines the java and SQL data-type for this attribute
	 * @return the type of the attribute
	 */
	public AttributeType type();
	
	/**
	 * @return whether to generate a setter for this attribute
	 */
	public boolean hasSetter() default true;

	/**
	 * @return whether to generate a getter for this attribute
	 */
	public boolean hasGetter() default true;
	
	/**
	 * @return whether this attribute may be null
	 * @see Attribute#mayBeNull() 
	 */
	public boolean mayBeNull() default true;

	/**
	 * @return optionally specifies the default-value
	 * @see Attribute#defaultValue() 
	 */
	public String defaultValue() default "";
	
	/**
	 * @return specifies whether the values of this attribute are unique
	 * @see Attribute#isUnique() 
	 */
	public boolean isUnique() default false;
	
	/**
	 * @return an optional custom SQL data-type
	 * @see Attribute#typeName() 
	 */
	public String customSQLType() default "";
	
	/**
	 * @return table to reference as a FOREIGN KEY
	 * @see Attribute#foreignKeyTable() 
	 */
	public String foreignKeyTable() default "";
	
	/**
	 * @return the column to reference as a FOREIGN KEY
	 * @see Attribute#foreignKeyColumn() 
	 */
	public String foreignKeyColumn() default "";
	
	/**
	 * @return the rule for UPDATE of the associated row for this FOREIGN KEY
	 * @see Attribute#onUpdate() 
	 */
	public ReferenceRule onUpdate() default ReferenceRule.NONE;

	/**
	 * @return the rule for DELETE called on the associated row of this FOREIGN KEY
	 * @see Attribute#onDelete() 
	 */
	public ReferenceRule onDelete() default ReferenceRule.NONE;

	/**
	 * If defined (any value other than an empty string), the given value will be added as CHECK-constraint to the column
	 * @return the SQL constraint to apply
	 * @see Attribute#checkConstraint() 
	 */
	public String checkConstraint() default "";
}
