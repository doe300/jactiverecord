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
package de.doe300.activerecord.record.association.generation;

import de.doe300.activerecord.record.ActiveRecord;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation to declare an association to be used to generate association-methods via annotation-processing.
 * 
 * This annotation declares a many-to-many association, with an association-table storing the foreign-key pairs of the
 * associated tables
 * 
 * {@link SingleTableInheritance} is automatically supported.
 * 
 * @author doe300
 * @since 0.4
 */
@Target(ElementType.TYPE)
public @interface HasManyThrough
{
	/**
	 * @return the name of the association
	 */
	public String name();
	
	/**
	 * @return the record-type of the associated table
	 */
	public Class<? extends ActiveRecord> associatedType();
	
	/**
	 * @return the name of the table storing the associations
	 */
	public String mappingTable();
	
	/**
	 * @return the key (of the mapping-table) referencing the key of this record
	 */
	public String mappingTableThisKey();
	
	/**
	 * @return the key (of the mapping-table) referencing the key of the associated record
	 */
	public String mappingTableAssociatedKey();	
}
