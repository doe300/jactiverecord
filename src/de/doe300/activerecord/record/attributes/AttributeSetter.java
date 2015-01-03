package de.doe300.activerecord.record.attributes;

import de.doe300.activerecord.validation.ValidationFailed;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Setter for a attribute. Use this annotation if the setter name does not conform with beans-standard or you need to convert the data to the correct type.
 * See JDBC documentation for supported data-types
 * @author doe300
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AttributeSetter
{
	/**
	 * @return the name of the attribute as found in the data store
	 */
	public String name();
	
	/**
	 * A validatorClass of {@link Void} disables validation.
	 * @return the class the validation-method is in, defaults to {@link Void}
	 * @see #validatorMethod() 
	 */
	public Class<?> validatorClass() default Void.class;
	
	/**
	 * The validator-method must conform accept a single argument of type {@link Object} and must be accessible publicly.
	 * If the validation is set and fails, the setter has no effect on the underlying record-base and the validation-method throws a {@link ValidationFailed}.
	 * 
	 * If both {@link #validatorMethod() } and {@link #converterMethod() } are set, the validation is performed on the unconverted parameter.
	 * Meaning, the validation is called before the convertion and therefore must accept the original argument type.
	 * @return the name of the validator-method, if specified
	 */
	public String validatorMethod() default "";	
	
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
	 * The converter-method must accept the parameter of the annotated setter-method as only parameter and return a value of the type
	 * specified in the underlying data-store with the attribute-name {@link #name()}
	 * @return the name of the converter-method, if specified
	 */
	public String converterMethod() default "";	
}
