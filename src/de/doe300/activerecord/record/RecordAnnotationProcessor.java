package de.doe300.activerecord.record;

import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.validation.ValidationFailed;
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

/**
 *
 * @author doe300
 * @see http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"de.doe300.activerecord.record.DataSet", "de.doe300.activerecord.attributes.AttributeGetter",
	"de.doe300.activerecord.attributes.AttributeSetter", "de.doe300.activerecord.Searchable"})
public class RecordAnnotationProcessor extends AbstractProcessor
{
	//TODO hasAttachment
	
	public RecordAnnotationProcessor()
	{
		//public no-arg processor required
	}

	//TODO also use this processor to generate accessor-methods or other kind of methods??
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		////
		//	AttributeAccessor
		////
		roundEnv.getElementsAnnotatedWith( AttributeGetter.class).forEach( (Element e)->{
			AttributeGetter accessor= e.getAnnotation( AttributeGetter.class);
			ExecutableElement executable = ((ExecutableElement)e);
			
			//check syntax of attribute-name
			if(executable.getSimpleName().chars().anyMatch( (int i)-> i == ' '))
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
				for(Method m: accessor.converterClass().getMethods())
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
		
		roundEnv.getElementsAnnotatedWith( AttributeSetter.class).forEach((Element e)->{
			AttributeSetter accessor= e.getAnnotation( AttributeSetter.class);
			ExecutableElement executable = ((ExecutableElement)e);
			
			//check syntax of attribute-name
			if(executable.getSimpleName().chars().anyMatch( (int i)-> i == ' '))
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
				for(Method m: accessor.validatorClass().getMethods())
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
					for(Class<?> exType : validatorMethod.getExceptionTypes())
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
				for(Method m: accessor.converterClass().getMethods())
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
		roundEnv.getElementsAnnotatedWith( Searchable.class).forEach( (Element e)->{
			if(!e.getKind().isInterface())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Searchables need to be interfaces", e);
			}
			TypeElement el = ( TypeElement ) e;
			boolean activeRecordFound = false;
			for(TypeMirror i :el.getInterfaces())
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
