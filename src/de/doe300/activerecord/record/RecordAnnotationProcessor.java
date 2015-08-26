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

import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.migration.ExcludeAttribute;
import de.doe300.activerecord.migration.constraints.Index;
import de.doe300.activerecord.pojo.POJOBase;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.attributes.Attributes;
import de.doe300.activerecord.validation.Validate;
import de.doe300.activerecord.validation.ValidatedRecord;
import de.doe300.activerecord.validation.ValidationFailed;
import de.doe300.activerecord.validation.ValidationType;
import java.util.Arrays;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.util.ElementScanner8;

/**
 * @author doe300
 * @see "http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html"
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
	"de.doe300.activerecord.record.RecordType", "de.doe300.activerecord.record.Searchable",
	"de.doe300.activerecord.migration.constraints.Index","de.doe300.activerecord.record.SingleTableInheritance",
	"de.doe300.activerecord.validation.Validate",
	"de.doe300.activerecord.migration.Attribute", "de.doe300.activerecord.migration.ExcludeAttribute",
	"de.doe300.activerecord.attributes.AttributeGetter", "de.doe300.activerecord.attributes.AttributeSetter",
})
@SupportedOptions({RecordAnnotationProcessor.OPTION_CHECK_ATTRIBUTES})
public class RecordAnnotationProcessor extends AbstractProcessor
{
	//TODO hasAttachment
	static final String OPTION_CHECK_ATTRIBUTES = "record.annotations.checkAttributes";

	/**
	 *
	 */
	public RecordAnnotationProcessor()
	{
		//public no-arg processor required
	}

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv )
	{	
		//type-annotations
		processRecordType( roundEnv );
		processSingleTableInheritance( roundEnv );
		processIndex( roundEnv );
		processSearchables( roundEnv );
		processValidate( roundEnv );
		//attribute-annotations
		processAttribute( roundEnv );
		processExcludeAttribute( roundEnv );
		processAttributeGetter( roundEnv );
		processAttributeSetter( roundEnv );
		return true;
	}
	
	private void processRecordType(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( RecordType.class).forEach((final Element e)->{
			final RecordType recordType = e.getAnnotation( RecordType.class);
			final TypeElement recordTypeElement = (TypeElement)e;
			boolean activeRecordFound = false;
			for(final TypeMirror i :recordTypeElement.getInterfaces())
			{
				if(processingEnv.getTypeUtils().isSubtype( i, processingEnv.getElementUtils().getTypeElement( ActiveRecord.class.getCanonicalName()).asType() ))
				{
					activeRecordFound = true;
					break;
				}
			}
			//Must extend ActiveRecord
			if(!activeRecordFound)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A record-type must extend ActiveRecord",e);
			}
			//type-name must not be empty
			if(recordType.typeName().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "record-type name must not be empty", e);
			}
			//type-name must not contain spaces
			if(recordType.typeName().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "record-type name must not contain any spaces", e);
			}
			//primary-key must not be empty
			if(recordType.primaryKey().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "primary-key column must not be empty", e);
			}
			//primary-key must not contain spaces
			if(recordType.primaryKey().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "primary-key column must not contain any spaces", e);
			}
			//default-columns should not be empty
			if(recordType.defaultColumns().length == 0)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Default columns should not be empty.", e);
			}
			//default-columns should contain primary-key
			if(!Arrays.stream( recordType.defaultColumns()).anyMatch( (String column) -> recordType.primaryKey().equals( column)))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Default columns should contain the primary-key column", e);
			}
			//TODO if type is concrete class and not single-inheritance, the constructor must accept (int, POJOBase)
		});
	}
	
	private void processSingleTableInheritance(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( SingleTableInheritance.class).forEach((final Element e)->{
			final SingleTableInheritance inheritanceAnnotation= e.getAnnotation( SingleTableInheritance.class);
			final TypeElement type = (TypeElement)e;
			boolean activeRecordFound = false;
			for(final TypeMirror i :type.getInterfaces())
			{
				if(processingEnv.getTypeUtils().isSubtype( i, processingEnv.getElementUtils().getTypeElement( ActiveRecord.class.getCanonicalName()).asType() ))
				{
					activeRecordFound = true;
					break;
				}
			}
			//Must extend ActiveRecord
			if(!activeRecordFound)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A single-inheritance type must extend ActiveRecord",e);
			}
			//column-name must not be empty
			if(inheritanceAnnotation.typeColumnName().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "inheritance-column must not be empty", e);
			}
			//column-name must not contain spaces
			if(inheritanceAnnotation.typeColumnName().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "inheritance-column must not contain any spaces", e);
			}
			try
			{
				//factory-method must exist in factory-class and have the correct signature
				Method factoryMethod = inheritanceAnnotation.factoryClass().getMethod( inheritanceAnnotation.factoryMethod(), POJOBase.class, Integer.TYPE, Object.class );
				if(!ActiveRecord.class.isAssignableFrom( factoryMethod.getReturnType()))
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Factory-method does not return a subtype of ActiveRecord", e);
				}
			}
			catch ( NoSuchMethodException | SecurityException ex )
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "No factory-method found with the correct signature", e);
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "The correct signature for a single-inheritance factory-method is:");
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "\"public static <T extends ActiveRecord> T methodName(POJOBase<T> base, int primaryKey, Object type);\"");
			}
		});
	}
	
	private void processIndex(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( Index.class).forEach((final Element e)->{
			final Index indexAnnotation= e.getAnnotation( Index.class);
			final TypeElement type = (TypeElement)e;
			boolean activeRecordFound = false;
			for(final TypeMirror i :type.getInterfaces())
			{
				if(processingEnv.getTypeUtils().isSubtype( i, processingEnv.getElementUtils().getTypeElement( ActiveRecord.class.getCanonicalName()).asType() ))
				{
					activeRecordFound = true;
					break;
				}
			}
			//Must extend ActiveRecord
			if(!activeRecordFound)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "An indexed type must extend ActiveRecord",e);
			}
			//index-name must not be empty
			if(indexAnnotation.name().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Index-name must not be empty", e);
			}
			//index-name must not have any spaces
			if(indexAnnotation.name().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Index-name must not contain any spaces", e);
			}
			//attributes need to exist for index-columns
			for(String column : indexAnnotation.columns())
			{
				if(!testAttributeExists( type, column ))
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Column '"+column+"' is indexed but was not found (could be a false positive)", e);
				}
			}
			//TODO index-names should globally be unique
		});
	}
	
	private void processValidate(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( Validate.class).forEach((final Element e)->{
			final Validate validateAnnotation= e.getAnnotation( Validate.class);
			final TypeElement type = (TypeElement)e;
			boolean activeRecordFound = false;
			for(final TypeMirror i :type.getInterfaces())
			{
				if(processingEnv.getTypeUtils().isSubtype( i, processingEnv.getElementUtils().getTypeElement( ValidatedRecord.class.getCanonicalName()).asType() ))
				{
					activeRecordFound = true;
					break;
				}
			}
			//any type annotated with validate must be of ValidatedRecord
			if(!activeRecordFound)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A validated type must extend ValidatedRecord",e);
			}
			//attribute-name of the validation must not be empty
			
			if(validateAnnotation.attribute().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "validated attribute-name must not be empty", e);
			}
			//attribute-name of the validation must not contain spaces
			if(validateAnnotation.attribute().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "validated attribute-name must not contain any spaces", e);
			}
			//if type is set to custom, custom-class and custom-method must exist
			if(validateAnnotation.type() == ValidationType.CUSTOM)
			{
				if(Void.class.equals( validateAnnotation.customClass()))
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Custom validation class must be set", e);
					return;
				}
				try
				{
					//if custom-class is set, custom-method must be a Predicate<Object>
					Method validationMethod = validateAnnotation.customClass().getMethod( validateAnnotation.customMethod(), Object.class);
					if(!Boolean.TYPE.isAssignableFrom( validationMethod.getReturnType()) && !Boolean.class.isAssignableFrom( validationMethod.getReturnType()))
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Validation-method must return a boolean value", e);
					}
				}
				catch ( NoSuchMethodException | SecurityException ex )
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "No validation-method found with the correct signature", e);
					processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "A validation-method must be a Predicate<Object>");
				}
			}
			//an attribure for the validated column must exist
			if(!testAttributeExists( type, validateAnnotation.attribute()))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Column '"+validateAnnotation.attribute()+"' is validated but was not found (could be a false positive)", e);
			}
		});
	}
	
	private void processAttribute(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( Attribute.class).forEach((final Element e)->{
			final Attribute attributeAnnotation= e.getAnnotation( Attribute.class);
			
			//attribute-name must not be empty
			if(attributeAnnotation.name().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Attribute-name must not be empty", e);
			}
			//attribute-name must not contain spaces
			if(attributeAnnotation.name().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Attribute-name must not contain any spaces", e);
			}
			//if type is VARCHAR, typeName should be set
			if(attributeAnnotation.type() == java.sql.Types.VARCHAR && attributeAnnotation.typeName().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "A custom VARCHAR type-name should be set", e);
			}
			//an attribute should not be nullable and unique
			if(attributeAnnotation.mayBeNull() && attributeAnnotation.isUnique())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Unique attributes should not be nullable", e);
			}
			//if a foreign-table is set, a foreign-column must be set too
			if(attributeAnnotation.foreignKeyTable().isEmpty() != attributeAnnotation.foreignKeyColumn().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A foreign key must specify both foreign-table and foreign-key", e);
			}
		});
	}
	
	private void processExcludeAttribute(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( ExcludeAttribute.class).forEach((final Element e)->{
			//exclude-attribute method should not be annotated with any other attribute-annotation
			if(e.getAnnotation( Attribute.class) != null || e.getAnnotation( AttributeGetter.class) != null || e.getAnnotation( AttributeSetter.class) != null)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Method annotated with ExcludeAttribute should not have any other attribute-annotation", e);
			}
		});
	}
	
	private void processAttributeSetter(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( AttributeSetter.class).forEach((final Element e)->{
			final AttributeSetter setterAnnotation= e.getAnnotation( AttributeSetter.class);
			final ExecutableElement setterMethod = (ExecutableElement)e;

			//attribute-name must not contain spaces
			if(setterAnnotation.name().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Attribute-name must not contain any spaces", e);
			}
			//setter-method must have exactly one parameter
			if(setterMethod.getParameters().size()!=1)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Method annotated with AttributeSetter must have exactly one parameter", e );
			}
			//setter-method should not have a return-type
			if(setterMethod.getReturnType().getKind()!=TypeKind.VOID)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Any return-value from a method annotated with AttributeSetter is ignored", e);
			}

			//check existence of validator
			if(!setterAnnotation.validatorClass().equals( Void.class))
			{
				Method validatorMethod = null;
				for(final Method m: setterAnnotation.validatorClass().getMethods())
				{
					if(m.getName().equals( setterAnnotation.validatorMethod()))
					{
						if(m.getParameterCount() == 1)
						{
							//check if parameter-type is supertype of method return-value
							if(processingEnv.getTypeUtils().isSubtype( setterMethod.getReturnType(), processingEnv.getElementUtils().getTypeElement( m.getParameterTypes()[0].getCanonicalName()).asType()))
							{
								validatorMethod = m;
								break;
							}
						}
					}
				}
				//if validator-class is set, validator-method must be set too
				if(validatorMethod == null)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Declared validator-method not found", e);
				}
				//check for validator-method throwing ValidationFailed
				else
				{
					boolean validationErrorThrown = false;
					for(final Class<?> exType : validatorMethod.getExceptionTypes())
					{
						if(ValidationFailed.class.isAssignableFrom( exType))
						{
							validationErrorThrown = true;
							break;
						}
					}
					//validator-method should throw ValidationFailed
					if(!validationErrorThrown)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Validation-method should be marked to throw a ValidationFailed", e);
					}
				}
			}

			//check existence of converter
			if(!setterAnnotation.converterClass().equals( Void.class))
			{
				Method converterMethod = null;
				for(final Method m: setterAnnotation.converterClass().getMethods())
				{
					if(m.getName().equals( setterAnnotation.converterMethod()))
					{
						if(m.getParameterCount() == 1)
						{
							//check if parameter-type is supertype of method return-value
							if(processingEnv.getTypeUtils().isSubtype( setterMethod.getReturnType(), processingEnv.getElementUtils().getTypeElement( m.getParameterTypes()[0].getCanonicalName()).asType()))
							{
								converterMethod = m;
								break;
							}
						}
					}
				}
				//if converter-class is set, converter-method must be set too
				if(converterMethod == null)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Declared converter-method not found", e);
				}
				//check for signature of converter-method
				else
				{
					//converter-method must have exactly one parameter
					if(converterMethod.getParameterCount()!=1)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must always have exactly one parameter", e );
					}
					//converter-method must not return void
					if(converterMethod.getReturnType() == Void.class)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must have a non-void return type", e);
					}
				}
			}
		});
	}
	
	private void processAttributeGetter(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( AttributeGetter.class).forEach( (final Element e)->{
			final AttributeGetter getterAnnotation= e.getAnnotation( AttributeGetter.class);
			final ExecutableElement getterMethod = (ExecutableElement)e;

			//attribute-name must not contain spaces
			if(getterAnnotation.name().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Attribute-name must not contain any spaces", e);
			}
			//getter-method must not have any parameters
			if(!getterMethod.getParameters().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Method annotated with AttributeGetter must not have any parameters", e );
			}
			//getter-method must not return void
			if(getterMethod.getReturnType().getKind()==TypeKind.VOID)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Any method annotated with AttributeGetter must return an non void type", e);
			}

			//check existence of converter
			if(!getterAnnotation.converterClass().equals( Void.class))
			{
				Method converterMethod = null;
				for(final Method m: getterAnnotation.converterClass().getMethods())
				{
					if(m.getName().equals( getterAnnotation.converterMethod()))
					{
						if(m.getParameterCount() == 1)
						{
							//check if return-type is subtype of getter-method return-type
							if(processingEnv.getTypeUtils().isSubtype( processingEnv.getElementUtils().getTypeElement( m.getReturnType().getCanonicalName()).asType(), getterMethod.getReturnType() ))
							{
								converterMethod = m;
								break;
							}
						}
					}
				}
				//if converter-class is set, converter-method must be set too
				if(converterMethod == null)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Declared converter-method not found", e);
				}
				//check for signature of converter-method
				else
				{
					//converter-method must have exactly one parameter
					if(converterMethod.getParameterCount() != 1)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must always have exactly one parameter", e );
					}
					//converter-method must not return void
					if(converterMethod.getReturnType() == Void.class)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must have a non-void return type", e);
					}
				}
			}
		});
	}

	private void processSearchables(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( Searchable.class).forEach( (final Element e)->{
			final Searchable searchable = e.getAnnotation( Searchable.class);
			final TypeElement searchableType = ( TypeElement ) e;
			boolean activeRecordFound = false;
			for(final TypeMirror i :searchableType.getInterfaces())
			{
				if(processingEnv.getTypeUtils().isSubtype( i, processingEnv.getElementUtils().getTypeElement( ActiveRecord.class.getCanonicalName()).asType() ))
				{
					activeRecordFound = true;
					break;
				}
			}
			//Must extend ActiveRecord
			if(!activeRecordFound)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A Searchable type must extend ActiveRecord",e);
			}
			//Should have a non-empty searchable-columns list
			if(searchable.searchableColumns().length==0)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Empty searchableColumns!", e);
			}
			//Should have (or inherit) attributes for all columns listed in the searchable-columns
			for(String column : searchable.searchableColumns())
			{
				if(!testAttributeExists( searchableType, column ))
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Column '"+column+"' is searchable but does not exist (This could be wrong)", e);
				}
			}
		});
	}
	
	private boolean testAttributeExists(TypeElement element, String attrName)
	{
		String optionValue = processingEnv.getOptions().get( OPTION_CHECK_ATTRIBUTES);
		if(optionValue == null || Boolean.parseBoolean( optionValue) != Boolean.TRUE)
		{
			//disable check for attributes
			return true;
		}
		if(ActiveRecord.DEFAULT_PRIMARY_COLUMN.equals( attrName))
		{
			return true;
		}
		if(TimestampedRecord.COLUMN_CREATED_AT.equals( attrName) || TimestampedRecord.COLUMN_UPDATED_AT.equals( attrName))
		{
			if(processingEnv.getTypeUtils().isSubtype( element.asType(), processingEnv.getElementUtils().getTypeElement( TimestampedRecord.class.getCanonicalName()).asType() ))
			{
				return true;
			}
		}
		return element.accept( new AttributeScanner(), attrName);
	}
	
	private static class AttributeScanner extends ElementScanner8<Boolean, String>
	{
		@Override
		public Boolean visitExecutable( ExecutableElement e, String p )
		{
			if(e.getAnnotation( Attribute.class) != null)
			{
				if(e.getAnnotation( Attribute.class).name().equals( p))
				{
					return true;
				}
			}
			if(e.getAnnotation( AttributeGetter.class) != null)
			{
				if(e.getAnnotation( AttributeGetter.class).name().equals( p))
				{
					return true;
				}
			}
			if(e.getAnnotation( AttributeSetter.class) != null)
			{
				if(e.getAnnotation( AttributeSetter.class).name().equals( p))
				{
					return true;
				}
			}
			String propName = Attributes.getPropertyName( e.getSimpleName().toString() );
			if(propName == null)
			{
				return false;
			}
			return propName.equals( p);
		}
	}
}
