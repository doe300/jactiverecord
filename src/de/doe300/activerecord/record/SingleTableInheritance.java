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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.pojo.POJOBase;

/**
 * Allows for storing inherited object types via single-table inheritance.
 * This annotation forces the {@link RecordBase} to use the specified factory-method for creating the record-object.
 * <br>
 * Record-types with inherited objects should use {@link RecordBase#createRecord(java.util.Map) } for record-creation
 * and a non-null column for the {@link #typeColumnName() type-column}.
 * Otherwise the instantiation of the correct subclass can not be guaranteed.
 * <br>
 * This annotation can only be used in combination with {@link RecordType}. It also only works on POJO-records.
 *
 * @author doe300
 * @see RecordType
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleTableInheritance
{
	/**
	 * @return the name of the column determining the type
	 */
	@Nonnull
	public String typeColumnName();

	/**
	 * @return class containing the factory-method
	 */
	@Nonnull
	public Class<?> factoryClass();

	/**
	 * The factory-method must be static, public and accepts three parameters:
	 * <ul>
	 * <li>the {@link RecordBase}, of type {@link POJOBase}
	 * <li>an integer with the primary key
	 * <li>an {@link Object} holding the value from the {@link #typeColumnName() type-column}</li>
	 * </ul>
	 * The factory-method returns an object extending the annotated class.
	 * <p>
	 * This method MUST conform to the signature:<br>
	 * {@code public static T methodName(POJOBase<T> base, int primaryKey, Object typeKey);}<br>
	 * Where T a subtype of the annotated type, extending ActiveRecord
	 * </p>
	 *
	 * @return the name of the factory-method.
	 */
	@Nonnull
	public String factoryMethod();
}
