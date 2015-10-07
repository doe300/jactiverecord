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
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.attributes.Attributes;
import de.doe300.activerecord.record.security.EncryptedAttribute;
import de.doe300.activerecord.record.validation.ValidationFailed;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
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
	"de.doe300.activerecord.attributes.AttributeGetter", "de.doe300.activerecord.attributes.AttributeSetter",
})
@SupportedOptions({AttributeProcessor.OPTION_CHECK_ATTRIBUTES})
public class AttributeProcessor extends AbstractProcessor
{
	static final String OPTION_CHECK_ATTRIBUTES = "record.annotations.checkAttributes";

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
					filter( (Field f) -> Modifier.isStatic( f.getModifiers()) && Modifier.isPublic( f.getModifiers()) && Integer.TYPE.equals( f.getType())).
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
			Class<?> converterClass = ( Class<?> ) converterClassValue.getValue();
			return Arrays.stream( converterClass.getMethods()).
					//retain all methods with matching signature
					filter( (Method method) -> method.getParameterCount() == 1 && Modifier.isStatic( method.getModifiers()) && Modifier.isPublic( method.getModifiers())).
					//retain only methods starting with the user-input
					filter( (Method method) -> userText == null || method.getName().startsWith( userText)).
					map( (Method method) ->Completions.of( method.getName() )).
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
			Class<?> validatorClass = ( Class<?> ) validatorClassValue.getValue();
			return Arrays.stream( validatorClass.getMethods()).
					//retain all methods with matching signature
					filter( (Method method) -> method.getParameterCount() == 1 && Modifier.isStatic( method.getModifiers()) && Modifier.isPublic( method.getModifiers())).
					//retain only methods starting with the user-input
					filter( (Method method) -> userText == null || method.getName().startsWith( userText)).
					map( (Method method) ->Completions.of( method.getName() )).
					collect( Collectors.toSet());
		}
		return Collections.emptySet();
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
				//TODO doesn't work (access to class)
				Method validatorMethod = null;
				for(final Method m: setterAnnotation.validatorClass().getMethods())
				{
					if(m.getName().equals( setterAnnotation.validatorMethod()))
					{
						if(m.getParameterCount() == 1 && Modifier.isStatic( m.getModifiers()) && Modifier.isPublic( m.getModifiers()))
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
					//TODO doesn't work (access to class)
					if(m.getName().equals( setterAnnotation.converterMethod()))
					{
						if(m.getParameterCount() == 1 && Modifier.isStatic( m.getModifiers()) && Modifier.isPublic( m.getModifiers()))
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
			if(false && !getterAnnotation.converterClass().equals( Void.class))
			{
				//TODO doesn't work (access to class)
				Method converterMethod = null;
				for(final Method m: getterAnnotation.converterClass().getMethods())
				{
					if(m.getName().equals( getterAnnotation.converterMethod()))
					{
						if(m.getParameterCount() == 1 && Modifier.isStatic( m.getModifiers()) && Modifier.isPublic( m.getModifiers()))
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
