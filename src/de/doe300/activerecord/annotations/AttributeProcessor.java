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
package de.doe300.activerecord.annotations;

import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.migration.ExcludeAttribute;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.attributes.Attributes;
import de.doe300.activerecord.record.security.EncryptedAttribute;
import de.doe300.activerecord.record.validation.ValidationFailed;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Annotation-processor for all annotations on attributes (member-methods)
 *
 * @author doe300
 * @since 0.3
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
	"de.doe300.activerecord.migration.Attribute", "de.doe300.activerecord.migration.ExcludeAttribute",
	"de.doe300.activerecord.attributes.record.AttributeGetter", "de.doe300.activerecord.record.attributes.AttributeSetter",
})
@SupportedOptions({ProcessorUtils.OPTION_CHECK_ATTRIBUTES})
public class AttributeProcessor extends AbstractProcessor
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		processAttribute( roundEnv );
		processExcludeAttribute( roundEnv );
		processAttributeGetter( roundEnv );
		processAttributeSetter( roundEnv );
		processEncryptedAttributes( roundEnv );
		//we do not claim the annotations, another processor may use them
		return false;
	}
	
	@Override
	public Iterable<? extends Completion> getCompletions( Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText )
	{
		//name of the attribute (attribute for encrypted attribute) - suggest any static class-fields of type string
		if(member.getSimpleName().contentEquals( "name") || member.getSimpleName().contentEquals( "attribute"))
		{
			return ProcessorUtils.getAllNonAttributeStringConstants( processingEnv, (TypeElement)element.getEnclosingElement()).
					map( (VariableElement ve) -> Completions.of( ve.getSimpleName().toString(), (String)ve.getConstantValue())).
					collect( Collectors.toList());
		}
		//type of the attribute - suggest any member of java.sql.Types
		if(member.getSimpleName().contentEquals( "type"))
		{
			return Arrays.stream( java.sql.Types.class.getFields()).
					//retain all public static int fields
					filter( (Field f) -> java.lang.reflect.Modifier.isStatic( f.getModifiers()) && java.lang.reflect.Modifier.isPublic( f.getModifiers()) && Integer.TYPE.equals( f.getType())).
					map( (Field f) -> Completions.of( java.sql.Types.class.getCanonicalName() +"."+ f.getName())).
					collect( Collectors.toList());
		}
		//typename of the attribute - by default suggest name of type, if set
		if(member.getSimpleName().contentEquals( "typeName"))
		{
			final AnnotationValue typeValue = ProcessorUtils.getAnnotationMemberValue( annotation, "type");
			if(typeValue == null || typeValue.getValue() == null)
			{
				return Collections.emptySet();
			}
			int type = ( int ) typeValue.getValue();
			for(VariableElement ve : ElementFilter.fieldsIn( processingEnv.getElementUtils().getTypeElement( java.sql.Types.class.getCanonicalName()).getEnclosedElements()))
			{
				if(type ==(int) ve.getConstantValue())					
				{
					return Collections.singleton( Completions.of( '"'+ve.getSimpleName().toString()+'"', "The correct syntax may vary from SQL-implementation to SQL-implementation!"));
				}
			}
		}
		//converter-method - suggest all methods in given converter-class, if any
		if(member.getSimpleName().contentEquals( "converterMethod"))
		{
			//XXX needs testing
			final AnnotationValue converterClassValue = ProcessorUtils.getAnnotationMemberValue( annotation, "converterClass");
			if(converterClassValue == null || converterClassValue.getValue() == null)
			{
				return Collections.emptySet();
			}
			TypeElement converterClass = (TypeElement)ProcessorUtils.getTypeMirror(processingEnv, () -> (Class<?>)converterClassValue.getValue()).asElement();
			return ProcessorUtils.getClassMethods( processingEnv, converterClass, null, (ExecutableElement ee) -> {
				return (userText == null || ee.getSimpleName().toString().toLowerCase().startsWith( userText.toLowerCase())) &&
						//retain all methods with matching signature
						ee.getParameters().size() == 1;
			}, Modifier.PUBLIC, Modifier.STATIC ).
					map( (ExecutableElement method) ->Completions.of( method.getSimpleName().toString() )).
					collect( Collectors.toList());
		}
		//validator-method - suggest all methods in given validator-class, if any
		if(member.getSimpleName().contentEquals( "validatorMethod"))
		{
			//XXX needs testing
			final AnnotationValue validatorClassValue = ProcessorUtils.getAnnotationMemberValue( annotation, "validatorClass");
			if(validatorClassValue == null || validatorClassValue.getValue() == null)
			{
				return Collections.emptySet();
			}
			TypeElement validatorClass = (TypeElement)ProcessorUtils.getTypeMirror(processingEnv, () -> (Class<?>)validatorClassValue.getValue()).asElement();
			return ProcessorUtils.getClassMethods( processingEnv, validatorClass, null, (ExecutableElement ee) -> {
				return (userText == null || ee.getSimpleName().toString().toLowerCase().startsWith( userText.toLowerCase())) &&
						//retain all methods with matching signature
						ee.getParameters().size() == 1;
			}, Modifier.PUBLIC, Modifier.STATIC ).
					map( (ExecutableElement method) ->Completions.of( method.getSimpleName().toString())).
					collect( Collectors.toSet());
		}
		return Collections.emptySet();
	}

	private void processAttribute(final RoundEnvironment roundEnv)
	{
		final DeclaredType recordTypeType = ProcessorUtils.getTypeMirror( processingEnv, () -> RecordType.class);
		final DeclaredType activeRecordType = ProcessorUtils.getTypeMirror( processingEnv, () -> ActiveRecord.class);
		final DeclaredType stringType = ProcessorUtils.getTypeMirror(processingEnv, () -> String.class);
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
			TypeMirror attributeType = ProcessorUtils.getTypeMirrorOrDefault(processingEnv, attributeAnnotation::type, null);
			if(ProcessorUtils.isClassSet( processingEnv, attributeType))
			{
				if(processingEnv.getTypeUtils().isSameType(stringType, attributeType) && attributeAnnotation.typeName().isEmpty())
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "A custom VARCHAR type-name should be set", e);
				}
			}
			else //#type is not set explicitely
			{
				final ExecutableElement methodElement = ( ExecutableElement ) e;
				if(methodElement.getReturnType().getKind() != TypeKind.VOID) //check return-type
				{
					if(processingEnv.getTypeUtils().isSameType(stringType, methodElement.getReturnType()) && attributeAnnotation.typeName().isEmpty())
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "A custom VARCHAR type-name should be set", e);
					}
					attributeType = methodElement.getReturnType();
				}
				else if(methodElement.getParameters().size() == 1) //check parameter-type
				{
					if(processingEnv.getTypeUtils().isSameType(stringType, methodElement.getParameters().get( 0).asType()) && attributeAnnotation.typeName().isEmpty())
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "A custom VARCHAR type-name should be set", e);
					}
					attributeType = methodElement.getParameters().get( 0).asType();
				}
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
			//if foreign-table is set, referenced type must extend ActiveRecord
			if(!attributeAnnotation.foreignKeyTable().isEmpty() && attributeType != null)
			{
				if(!processingEnv.getTypeUtils().isSubtype( attributeType, activeRecordType))
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Attribute with foreign-table set must reference a subtype of ActiveRecord", e);
				}
			}
			//if associated with ActiveRecord and without foreignTable, associated type should have recordType-annotation
			if(attributeType != null && processingEnv.getTypeUtils().isSubtype( attributeType, activeRecordType ) && attributeAnnotation.foreignKeyTable().isEmpty())
			{
				boolean recordTypeFound = false;
				List<? extends AnnotationMirror> mirrors = processingEnv.getElementUtils().getAllAnnotationMirrors( processingEnv.getTypeUtils().asElement( attributeType));
				for(AnnotationMirror mirror : mirrors)
				{
					if(processingEnv.getTypeUtils().isSameType( mirror.getAnnotationType(), recordTypeType))
					{
						recordTypeFound = true;
						//there is only one RecordType-annotation
						break;
					}
				}
				if(!recordTypeFound)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Associated type '" + 
							processingEnv.getTypeUtils().asElement( attributeType ).getSimpleName() + 
							"' should have RecordType-annotation.", e);
				}
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
		final TypeMirror validationFailedType = ProcessorUtils.getTypeMirror(processingEnv, () -> ValidationFailed.class);
		
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
			DeclaredType validatorClass = ProcessorUtils.getTypeMirror(processingEnv, setterAnnotation::validatorClass);
			if(ProcessorUtils.isClassSet( processingEnv, validatorClass))
			{
				//TODO needs testing
				ExecutableElement validatorMethod = ProcessorUtils.getClassMethod( processingEnv, (TypeElement) validatorClass.asElement(), 
						setterAnnotation.validatorMethod(), (ExecutableElement ee) -> {
							return ee.getParameters().size() == 1 &&
								//check if parameter-type is supertype of method return-value
								processingEnv.getTypeUtils().isSubtype( setterMethod.getReturnType(), ee.getParameters().get( 0).asType());
						}, Modifier.PUBLIC, Modifier.STATIC );
				//if validator-class is set, validator-method must be set too
				if(validatorMethod == null)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Declared validator-method not found", e);
				}
				//check for validator-method throwing ValidationFailed
				else
				{
					boolean validationErrorThrown = validatorMethod.getThrownTypes().stream().anyMatch( (TypeMirror type) -> 
					{
						return processingEnv.getTypeUtils().isSubtype( type, validationFailedType);
					});
					//validator-method should throw ValidationFailed
					if(!validationErrorThrown)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Validation-method should be marked to throw a ValidationFailed", e);
					}
				}
			}

			//check existence of converter
			DeclaredType converterClass = ProcessorUtils.getTypeMirror(processingEnv, setterAnnotation::converterClass);
			if(ProcessorUtils.isClassSet( processingEnv, converterClass))
			{
				ExecutableElement converterMethod = ProcessorUtils.getClassMethod( processingEnv, (TypeElement) converterClass.asElement(), 
						setterAnnotation.converterMethod(), (ExecutableElement ee) -> {
							return ee.getParameters().size() == 1 &&
								//check if parameter-type of converter is supertype of setter parameter-type
								processingEnv.getTypeUtils().isSubtype( ee.getParameters().get( 0).asType(), setterMethod.getParameters().get( 0).asType());
						}, Modifier.PUBLIC, Modifier.STATIC );
				//if converter-class is set, converter-method must be set too
				if(converterMethod == null)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Declared converter-method not found", e);
				}
				//check for signature of converter-method
				else
				{
					//converter-method must not return void
					if(!ProcessorUtils.isClassSet( processingEnv, converterMethod.getReturnType()))
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
			DeclaredType converterClass = ProcessorUtils.getTypeMirror(processingEnv, getterAnnotation::converterClass);
			if(ProcessorUtils.isClassSet( processingEnv, converterClass ))
			{
				ExecutableElement converterMethod = ProcessorUtils.getClassMethod( processingEnv, (TypeElement)converterClass.asElement(), 
						getterAnnotation.converterMethod(), (ExecutableElement ee )-> {
							return ee.getParameters().size() == 1 &&
							//check if return-type is subtype of getter-method return-type
									processingEnv.getTypeUtils().isSubtype( ee.getReturnType(), getterMethod.getReturnType());
						}, Modifier.PUBLIC, Modifier.STATIC );
				//if converter-class is set, converter-method must be set too
				if(converterMethod == null)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Declared converter-method not found", e);
				}
				//check for signature of converter-method
				else
				{
					//converter-method must not return void
					if(!ProcessorUtils.isClassSet( processingEnv, converterMethod.getReturnType()))
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must have a non-void return type", e);
					}
				}
			}
		});
	}
	
	private void processEncryptedAttributes(final RoundEnvironment roundEnv)
	{
		roundEnv.getElementsAnnotatedWith( EncryptedAttribute.class).forEach( (final Element e)->{
			final EncryptedAttribute encrypted = e.getAnnotation( EncryptedAttribute.class);
			final ExecutableElement method = (ExecutableElement)e;
			//attribute-name must not contain spaces
			if(encrypted.attribute().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Attribute-name must not contain any spaces", e);
			}
			if(!testAttributeExists(processingEnv, ( TypeElement ) method.getEnclosingElement(), encrypted.attribute()))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "No such attribute for encrypted method", e);
			}
			//method must be a string getter or setter
			if(method.getReturnType().getKind() != TypeKind.VOID)
			{
				if(!processingEnv.getTypeUtils().isSubtype( processingEnv.getElementUtils().getTypeElement( String.class.getCanonicalName()).asType(), method.getReturnType() ))
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Encrypted getter must be of type string", e);
				}
			}
			else if(!method.getParameters().isEmpty())
			{
				if(method.getParameters().size() != 1)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Encrypted setter must have exactly one parameter", e);
				}
				else if(!processingEnv.getTypeUtils().isSubtype( processingEnv.getElementUtils().getTypeElement( String.class.getCanonicalName()).asType(), method.getParameters().get( 0).asType() ))
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Encrypted setter must accept a variable of type string", e);
				}
			}
			else
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "EncryptedAttribute found on a non-setter and non-getter method", e);
			}
		});
	}
	
	static boolean testAttributeExists(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final TypeElement element, @Nonnull final String attrName)
	{
		String optionValue = processingEnv.getOptions().get( ProcessorUtils.OPTION_CHECK_ATTRIBUTES);
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
		List<ExecutableElement> methods = ElementFilter.methodsIn( processingEnv.getElementUtils().getAllMembers( element));
		return methods.stream().anyMatch( (ExecutableElement e) -> 
		{
			if(e.getAnnotation( Attribute.class) != null)
			{
				if(e.getAnnotation( Attribute.class).name().equals( attrName))
				{
					return true;
				}
			}
			if(e.getAnnotation( AttributeGetter.class) != null)
			{
				if(e.getAnnotation( AttributeGetter.class).name().equals( attrName))
				{
					return true;
				}
			}
			if(e.getAnnotation( AttributeSetter.class) != null)
			{
				if(e.getAnnotation( AttributeSetter.class).name().equals( attrName))
				{
					return true;
				}
			}
			String propName = Attributes.getPropertyName( e.getSimpleName().toString() );
			if(propName == null)
			{
				return false;
			}
			return propName.equals( attrName);
		});
	}
}
