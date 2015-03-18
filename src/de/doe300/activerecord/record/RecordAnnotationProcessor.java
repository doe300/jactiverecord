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
import de.doe300.activerecord.validation.ValidationFailed;

/**
 * @author doe300
 * @see "http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html"
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"de.doe300.activerecord.record.DataSet", "de.doe300.activerecord.attributes.AttributeGetter",
	"de.doe300.activerecord.attributes.AttributeSetter", "de.doe300.activerecord.Searchable"})
public class RecordAnnotationProcessor extends AbstractProcessor
{
	//TODO hasAttachment
	//TODO adapt to POJORecord
	//TODO generate (if flag is set) static Query- and FinderMethods for ActiveRecord-interface
	//TODO generate Record-interface from DB-Table

	/**
	 *
	 */
	public RecordAnnotationProcessor()
	{
		//public no-arg processor required
	}

	//TODO also use this processor to generate accessor-methods or other kind of methods??

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv )
	{
		////
		//	AttributeAccessor
		////
		roundEnv.getElementsAnnotatedWith( AttributeGetter.class).forEach( (final Element e)->{
			final AttributeGetter accessor= e.getAnnotation( AttributeGetter.class);
			final ExecutableElement executable = (ExecutableElement)e;

			//check syntax of attribute-name
			if(executable.getSimpleName().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Name must not contain any spaces", e);
			}

			if(!executable.getParameters().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Method annotated with AttributeGetter must not have any parameters", e );
			}
			if(executable.getReturnType().getKind()==TypeKind.VOID)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Any method annotated with AttributeGetter must return an non void type", e);
			}

			//check existence of converter
			if(!accessor.converterClass().equals( Void.class))
			{
				Method converterMethod = null;
				for(final Method m: accessor.converterClass().getMethods())
				{
					if(m.getName().equals( accessor.converterMethod()))
					{
						if(m.getParameterCount() == 1)	//TODO check if parameter-type is supertype of method return-value
						{
							converterMethod = m;
							break;
						}
					}
				}
				if(converterMethod == null)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Declared converter-method not found", e);
				}
				//check for signature of converter-method
				else
				{
					if(converterMethod.getParameterCount()!=1)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must always have exactly one parameter", e );
					}
					if(converterMethod.getReturnType() == Void.class)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must have a non-void return type", e);
					}
				}
			}
		});

		roundEnv.getElementsAnnotatedWith( AttributeSetter.class).forEach((final Element e)->{
			final AttributeSetter accessor= e.getAnnotation( AttributeSetter.class);
			final ExecutableElement executable = (ExecutableElement)e;

			//check syntax of attribute-name
			if(executable.getSimpleName().chars().anyMatch( (final int i)-> i == ' '))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Name must not contain any spaces", e);
			}

			if(executable.getParameters().size()!=1)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Method annotated with AttributeSetter must have exactly one parameter", e );
			}
			if(executable.getReturnType().getKind()!=TypeKind.VOID)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Any return-value from a method annotated with AttributeSetter is ignored", e);
			}

			//check existence of validator
			if(!accessor.validatorClass().equals( Void.class))
			{
				Method validatorMethod = null;
				for(final Method m: accessor.validatorClass().getMethods())
				{
					if(m.getName().equals( accessor.validatorMethod()))
					{
						if(m.getParameterCount() == 1)	//TODO check if parameter-type is supertype of method return-value
						{
							validatorMethod = m;
							break;
						}
					}
				}
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
					if(!validationErrorThrown)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Validation-method should be marked to throw a ValidationFailed", e);
					}
				}
			}

			//check existence of converter
			if(!accessor.converterClass().equals( Void.class))
			{
				Method converterMethod = null;
				for(final Method m: accessor.converterClass().getMethods())
				{
					if(m.getName().equals( accessor.converterMethod()))
					{
						if(m.getParameterCount() == 1)	//TODO check if parameter-type is supertype of method return-value
						{
							converterMethod = m;
							break;
						}
					}
				}
				if(converterMethod == null)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Declared converter-method not found", e);
				}
				//check for signature of converter-method
				else
				{
					if(converterMethod.getParameterCount()!=1)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must always have exactly one parameter", e );
					}
					if(converterMethod.getReturnType() == Void.class)
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Converter-method must have a non-void return type", e);
					}
				}
			}
		});

		////
		//	Searchable
		////
		roundEnv.getElementsAnnotatedWith( Searchable.class).forEach( (final Element e)->{
			if(!e.getKind().isInterface())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Searchables need to be interfaces", e);
			}
			final TypeElement el = ( TypeElement ) e;
			boolean activeRecordFound = false;
			for(final TypeMirror i :el.getInterfaces())
			{
				if(processingEnv.getTypeUtils().isSubtype( i, processingEnv.getElementUtils().getTypeElement( ActiveRecord.class.getCanonicalName()).asType() ))
				{
					activeRecordFound = true;
					break;
				}
			}
			if(!activeRecordFound)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A Searchable interface must extend ActiveRecord",e);
			}
			if(e.getAnnotation( Searchable.class).searchableColumns()==null || e.getAnnotation( Searchable.class).searchableColumns().length==0)
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Empty searchableColumns!", e);
			}
		});
		return true;
	}

}
