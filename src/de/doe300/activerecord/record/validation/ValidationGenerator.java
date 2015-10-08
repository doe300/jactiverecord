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
package de.doe300.activerecord.record.validation;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Annotation-processor for generation the validation-methods for validated record from their {@link Validate} annotations.
 * 
 * @author doe300
 * @since 0.4
 * @see Validate
 * @see ValidatedRecord
 * @see ValidationHandler
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
	"de.doe300.activerecord.record.validation.Validate",
	"de.doe300.activerecord.record.validation.Validates"
})
public class ValidationGenerator extends AbstractProcessor
{
	private final Set<String> processedElements = new HashSet<>(10);

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		roundEnv.getElementsAnnotatedWith( Validate.class).forEach((final Element e)->{
			final Validate[] validations = e.getAnnotationsByType( Validate.class);
			final TypeElement recordTypeElement = (TypeElement)e;
			processValidations( recordTypeElement, validations );
		});
		
		roundEnv.getElementsAnnotatedWith( Validates.class).forEach((final Element e)->{
			final Validate[] validations = e.getAnnotation(Validates.class).value();
			final TypeElement recordTypeElement = (TypeElement)e;
			processValidations( recordTypeElement, validations );
		});
		
		return true;
	}
	
	private void processValidations(final TypeElement recordTypeElement, final Validate[] validations)
	{
		if(processedElements.contains( recordTypeElement.getQualifiedName().toString()))
		{
			return;
		}
		processedElements.add( recordTypeElement.getQualifiedName().toString());

		//we can't write into an existing source-file so we must create a new one
		try
		{
			final String generatedFileName = recordTypeElement.getSimpleName()+ "Validations";
			JavaFileObject destFile = processingEnv.getFiler().createSourceFile( recordTypeElement.getQualifiedName()+"Validations", recordTypeElement);
			try(Writer writer = destFile.openWriter())
			{
				//we create an interface with given validation-methods as default-methods and extending ValidatedRecord
				writer.append( "package ");
				writer.append( processingEnv.getElementUtils().getPackageOf( recordTypeElement).getQualifiedName().toString());
				writer.append( ";\n");

				writer.append( "import ").append( ValidatedRecord.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( ValidationFailed.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( Validations.class.getCanonicalName()).append( ";\n");
				
				//TODO write @Generated annotation (somehow netbeans can't find it)

				writer.append( "interface ").append( generatedFileName ).append(" extends ValidatedRecord");

				writer.append(" {\n\n");

				//check if #validate() or #isValid() are already overridden
				final TypeMirror validatedRecordType = processingEnv.getElementUtils().getTypeElement( ValidatedRecord.class.getCanonicalName()).asType();
				boolean validateOverridden = false, isValidOverridden = false;
				for(ExecutableElement ee : ElementFilter.methodsIn( processingEnv.getElementUtils().getAllMembers( recordTypeElement)))
				{
					if(ee.getSimpleName().contentEquals( "validate") && ee.getReturnType().getKind() == TypeKind.VOID 
							&& ee.getParameters().isEmpty())
					{
						if(!processingEnv.getTypeUtils().isSameType( validatedRecordType, ee.getEnclosingElement().asType() ))
						{
							validateOverridden = true;
						}
					}
					if(ee.getSimpleName().contentEquals( "isValid") && ee.getReturnType().getKind() == TypeKind.BOOLEAN
							&& ee.getParameters().isEmpty())
					{
						if(!processingEnv.getTypeUtils().isSameType( validatedRecordType, ee.getEnclosingElement().asType() ))
						{
							validateOverridden = true;
						}
					}
				}
				
				//write validation-methods to file
				if(validateOverridden)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "validate() already overridden, skipping", recordTypeElement);
				}
				else
				{
					writer.append( generateValidateMethod( validations));
				}
				if(isValidOverridden)
				{
					processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "isValid() already overridden, skipping", recordTypeElement);
					
				}
				else 
				{
					writer.append( generateIsValidMethod( validations));
				}

				writer.append( "}");
			}
			processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Generated: " + 
					processingEnv.getElementUtils().getPackageOf( recordTypeElement).getQualifiedName().toString()+ '.' +generatedFileName,
					recordTypeElement);
		}
		catch ( IOException ex )
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, ex.getMessage(), recordTypeElement);
		}
	}
	
	private static String generateValidateMethod(@Nonnull final Validate[] validations)
	{
		//public default void validate() throws ValidationFailed {
		return "\t@Override\n" +
				"\tpublic default void validate() throws ValidationFailed {\n"
				//print validation-methods
				+ Arrays.stream(validations).map( (Validate validation) -> toAssertionCall(validation)).
						collect( Collectors.joining( "\n", "", "\n") )
				//}
				+ "\t}\n\n";
	}
	
	private static String generateIsValidMethod(@Nonnull final Validate[] validations)
	{
		//public default boolean isValid() {
		return "\t@Override\n" +
				"\tpublic default boolean isValid() {\n"
				//print validation-methods
				+ Arrays.stream(validations).map( (Validate validation) -> toValidationCheck(validation)).
						collect( Collectors.joining( "\n", "", "\n") )
				//return true;
				+ "\t\treturn true;\n"
				//}
				+ "\t}\n\n";
	}

	private static String toValidationCheck(@Nonnull final Validate validation)
	{
		// {
		return "\t\t{\n"
				// Object value = getBase().getStore().getValue( getBase(), getPrimaryKey(), <attribute-name>);
				+ "\t\t\tObject value = getBase().getStore().getValue( getBase(), getPrimaryKey(), \"" + validation.attribute() + "\");\n"
				// if(!<validation-method>) {
				+ "\t\t\tif(!(" + toValidationMethodCall( validation )+ "))\n"
				//return false;
				+ "\t\t\t\treturn false;\n"
				//}
				+ "\t\t}";
	}
	
	private static String toAssertionCall(@Nonnull final Validate validation)
	{
		//{
		return "\t\t{\n"
				// Object value = getBase().getStore().getValue( getBase(), getPrimaryKey(), <attribute-name>);
				+ "\t\t\tObject value = getBase().getStore().getValue( getBase(), getPrimaryKey(), \"" + validation.attribute() + "\");\n"
				// if(!<validation-method>) {
				+ "\t\t\tif(!(" + toValidationMethodCall( validation )+ "))\n"
				//throw new ValidationFailed(<attribute-name>, value);
				+ "\t\t\t\tthrow new ValidationFailed(\"" + validation.attribute() + "\", value);\n"
				//}
				+ "\t\t}";
	}
	
	private static String toValidationMethodCall(@Nonnull final Validate validate)
	{
		switch(validate.type())
		{
			case IS_NULL:
				return "value == null";
			case IS_EMPTY:
				return "Validations.isEmpty(value)";
			case NOT_NULL:
				return "value != null";
			case NOT_EMPTY:
				return "Validations.notEmpty(value)";
			case POSITIVE:
				return "Validations.positiveNumber(value)";
			case NEGATIVE:
				return "Validations.negativeNumber(value)";
			case CUSTOM:
			default:
				return validate.customClass().getCanonicalName()+'.'+validate.customMethod()+"(value)";
		}
	}
}
