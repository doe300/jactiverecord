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
package de.doe300.activerecord.record.attributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Getter for a attribute. Use this annotation if the getter name does not conform with beans-standard or you need to convert the data to the correct type.
 * See JDBC documentation for supported data-types
 * @author doe300
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AttributeGetter
{
	/**
	 * @return the name of the attribute as found in the data store
	 */
	@Nonnull
	public String name();

	/**
	 * A converterClass of {@link Void} disables converting
	 * @return the class the converter-method is in, defaults to {@link Void}
	 * @see #converterMethod()
	 */
	public Class<?> converterClass() default Void.class;

	/**
	 * The converter-method must conform to {@link Function Function&lt;Object,Object&gt;} and must be publicly accessible.
	 * It may also be a method of the declaring type or any of its super-types, like a <code>default</code> or static method.
	 *
	 * This method must accept the data stored in the RecordStore as only parameter and return the type, the annotated getter-method returns.
	 *
	 * @return the name of the converter-method, if specified
	 */
	public String converterMethod() default "";
}
