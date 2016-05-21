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
package de.doe300.activerecord.record.attributes.generation;

import de.doe300.activerecord.annotations.ProcessorUtils;
import de.doe300.activerecord.jdbc.TypeMappings;
import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.migration.constraints.ReferenceRule;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.attributes.Attributes;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Annotation-processor to generate attribute getter and setter from {@link AddAttribute} annotations
 * 
 * For code-generation, see: https://deors.wordpress.com/2011/10/31/annotation-generators/
 * 
 * @author doe300
 * @since 0.3
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
	"de.doe300.activerecord.record.attributes.generation.AddAttribute",
	"de.doe300.activerecord.record.attributes.generation.AddAttributes",
})
@SupportedOptions(ProcessorUtils.OPTION_ADD_NULLABLE_ANNOTATIONS)
public class AttributeGenerator extends AbstractProcessor
{
	private final Set<String> processedElements = new HashSet<>(10);
	private final DateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		roundEnv.getElementsAnnotatedWith( AddAttribute.class).forEach((final Element e)->{
			final AddAttribute[] addAttributes = e.getAnnotationsByType( AddAttribute.class);
			final TypeElement recordTypeElement = (TypeElement)e;
			processAddAttributes( recordTypeElement, addAttributes );
		});
		
		roundEnv.getElementsAnnotatedWith( AddAttributes.class).forEach((final Element e)->{
			final AddAttribute[] addAttributes = e.getAnnotation(AddAttributes.class).value();
			final TypeElement recordTypeElement = (TypeElement)e;
			processAddAttributes( recordTypeElement, addAttributes );
		});
		
		//we have multiple processors handling the same annotations, so don't consume
		return false;
	}
	
	private void processAddAttributes(final TypeElement recordTypeElement, final AddAttribute[] addAttributes)
	{
		final DeclaredType booleanType = ProcessorUtils.getTypeMirror( processingEnv, () -> Boolean.class);
		if(processedElements.contains( recordTypeElement.getQualifiedName().toString()))
		{
			return;
		}
		processedElements.add( recordTypeElement.getQualifiedName().toString());

		final List<String> usedAttributeNames = ProcessorUtils.getAllAttributeNames(processingEnv, recordTypeElement);

		//we can't write into an existing source-file so we must create a new one
		try
		{
			final String generatedFileName = recordTypeElement.getSimpleName()+ "Attributes";
			JavaFileObject destFile = processingEnv.getFiler().createSourceFile( recordTypeElement.getQualifiedName()+"Attributes", recordTypeElement);
			try(Writer writer = destFile.openWriter())
			{
				//we create an interface with given attribute-methods as default-methods and extending ActiveRecord
				writer.append( "package ");
				writer.append( processingEnv.getElementUtils().getPackageOf( recordTypeElement).getQualifiedName().toString());
				writer.append( ";\n");

				writer.append( "import ").append( Attribute.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( TypeMappings.class.getCanonicalName()).append( ";\n");
				if(addNullableAnnotation())
				{
					writer.append( "import ").append( Nullable.class.getCanonicalName()).append( ";\n");
				}

				writer.append( "import ").append( Generated.class.getCanonicalName()).append( ";\n");
				writer.append( "\n");
				
				writer.append( "@Generated(value = {\"").append( getClass().getCanonicalName()).append( "\"}, date = \"")
						.append( ISO_8601_DATE_FORMAT.format( new Date())).append( "\")\n");
				//must be public for the default methods to be called from proxy-record
				writer.append( "public interface ").append( generatedFileName ).append(" extends ").append( ActiveRecord.class.getCanonicalName());

				writer.append(" {\n\n");

				//write attribute-methods to file
				for(final AddAttribute addAttribute : addAttributes)
				{
					//check if methods for attribute already exist
					if(usedAttributeNames.contains( addAttribute.name()))
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Attribute-name '" + addAttribute.name()+ "' already in use, skipping", recordTypeElement);
						continue;
					}
					final TypeElement classElement = (TypeElement)ProcessorUtils.getTypeMirror( processingEnv, addAttribute::type).asElement();
					//generate @Attribute-annotations
					final String attributeAnnotation = generateAttributeAnnotation( addAttribute, classElement.getQualifiedName().toString() );

					//generate getter/setter (is for boolean)
					if(addAttribute.hasSetter())
					{
						writer.append( attributeAnnotation);
						writer.append( generateSetter( addAttribute, addNullableAnnotation(), classElement.getQualifiedName().toString()));
					}
					if(addAttribute.hasGetter())
					{
						if(addNullableAnnotation())
						{
							writer.append( "\t@Nullable\n");
						}
						writer.append( attributeAnnotation);
						final boolean isBoolean = processingEnv.getTypeUtils().isSameType( booleanType, classElement.asType());
						writer.append( generateGetter( addAttribute, classElement.getQualifiedName().toString(), isBoolean));
					}
				}

				writer.append( "}");
			}
			processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Generated: " + 
					processingEnv.getElementUtils().getPackageOf( recordTypeElement).getQualifiedName().toString()+ '.' +generatedFileName,
					recordTypeElement);
			
			//warn if type does not extend generated type
			if(!ProcessorUtils.extendsType( processingEnv, generatedFileName, recordTypeElement))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Type '" + recordTypeElement.getSimpleName()
						+ "' does not extend the generated type: " + generatedFileName, recordTypeElement);
			}
		}
		catch ( IOException ex )
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, ex.getMessage(), recordTypeElement);
		}
	}
	
	private boolean addNullableAnnotation()
	{
		String optionValue = processingEnv.getOptions().get( ProcessorUtils.OPTION_ADD_NULLABLE_ANNOTATIONS);
		return optionValue != null && Boolean.TRUE.equals( Boolean.valueOf( optionValue));
	}

	@Nonnull
	private static String generateAttributeAnnotation(@Nonnull final AddAttribute source, @Nonnull final String classType)
	{
		return "\t@Attribute( name = \"" + source.name() + "\", type = " + classType
				+ ".class, typeName = \"" + source.customSQLType() + "\", mayBeNull = " 
				+ source.mayBeNull() + ", defaultValue = \"" + source.defaultValue() + "\", isUnique = " 
				+ source.isUnique() + ", foreignKeyTable = \"" + source.foreignKeyTable() + "\", foreignKeyColumn = \"" 
				+ source.foreignKeyColumn() + "\", onUpdate = " + ReferenceRule.class.getCanonicalName()+ '.' + source.onUpdate().name()
				+ ", onDelete = " + ReferenceRule.class.getCanonicalName()+ '.' + source.onDelete().name() + ", checkConstraint = \"" 
				+ source.checkConstraint() + "\")\n";
	}
	
	@Nonnull
	private static String generateSetter(@Nonnull final AddAttribute source, boolean withNullable, @Nonnull final String classType)
	{
		//public default void setAttributeName(final Type value) {
		return "\tpublic default void set" + Attributes.toCamelCase( source.name()) + "("+(withNullable ? "@Nullable" : "")+" final "
				+ classType + " value) {\n"
				//TODO setter does not support type-mapping
				//getBase.getStore.setValue(getBase(), getPrimaryKey(), "attributeName", value);
				+ "\t\tgetBase().getStore().setValue(getBase(), getPrimaryKey(), \"" + source.name() + "\", value);\n"
				//}
				+ "\t}\n\n";
	}
	
	private static String generateGetter(@Nonnull final AddAttribute source, @Nonnull final String classType, final boolean isBoolean)
	{
		//public default Type get(is)AttributeName() {
		return "\tpublic default " + classType + (isBoolean ? " is" : " get") 
				+ Attributes.toCamelCase( source.name()) + "() {\n"
				//return TypeMappings.coerceToType(getBase.getStore.getValue(getBase(), getPrimaryKey(), "attributeName"), Type.class);
				+ "\t\treturn TypeMappings.coerceToType(getBase().getStore().getValue(getBase(), getPrimaryKey(), \"" + source.name() + "\"), " + classType + ".class);"
				//}
				+ "\t}\n\n";
	}
}
