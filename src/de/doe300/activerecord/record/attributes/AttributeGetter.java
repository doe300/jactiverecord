package de.doe300.activerecord.record.attributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
