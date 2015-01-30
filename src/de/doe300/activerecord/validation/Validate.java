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

package de.doe300.activerecord.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Specifies validations for {@link ValidatedRecord}, from which the <code>validate</code> and <code>isValid</code> methods are gnerated
 * @author doe300
 * @see ValidatedRecord#validate() 
 * @see ValidatedRecord#isValid() 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Validates.class)
@Inherited
public @interface Validate
{
	/**
	 * @return the name of the attribute to validate
	 */
	public String attribute();
	
	/**
	 * @return the type of validation
	 */
	public ValidationType type();
	
	/**
	 * For custom validation to be used, <code>type</code> must be set to {@link ValidationType#CUSTOM}
	 * @return the class of the custom validation-method
	 */
	public Class<?> customClass() default Void.class;
	
	/**
	 * For custom validation to be used, <code>type</code> must be set to {@link ValidationType#CUSTOM}.
	 * The method for custom validation must conform to {@link Predicate Predicate&lt;Object&gt;}, meaning it must accept exactly one
	 * parameter of type  {@link Object} and return a boolean-value.
	 * This method may be an instance-method of the record-type or any <code>static</code> method accessible to the validated record.
	 * @return the name of the custom validation-method
	 */
	public String customMethod() default "";
}
