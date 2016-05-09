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
package de.doe300.activerecord.record.association.generation;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.annotations.ProcessorUtils;
import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.association.AssociationHelper;
import de.doe300.activerecord.record.association.HasManyAssociationSet;
import de.doe300.activerecord.record.association.HasManyThroughAssociationSet;
import de.doe300.activerecord.record.association.RecordSet;
import de.doe300.activerecord.record.attributes.Attributes;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Annotation-processor for generating association-methods for records
 *
 * @see BelongsTo
 * @see Has
 * @see Associations
 * @author doe300
 * @since 0.4
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
	"de.doe300.activerecord.record.association.generation.BelongsTo",
	"de.doe300.activerecord.record.association.generation.Has",
	"de.doe300.activerecord.record.association.generation.HasManyThrough",
	"de.doe300.activerecord.record.association.generation.Associations"
})
@SupportedOptions(ProcessorUtils.OPTION_ADD_NULLABLE_ANNOTATIONS)
public class AssociationGenerator extends AbstractProcessor
{
	private final Map<String, Set<String>> processedElements = new HashMap<>(10);
	private final DateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		roundEnv.getElementsAnnotatedWith( BelongsTo.class).forEach((final Element e)->{
			processAssociations( (TypeElement)e);
		});
		
		roundEnv.getElementsAnnotatedWith( Has.class).forEach((final Element e)->{
			processAssociations( (TypeElement)e);
		});
		
		roundEnv.getElementsAnnotatedWith( Associations.class).forEach((final Element e)->{
			processAssociations( (TypeElement)e);
		});
		return true;
		
	}

	private void processAssociations(final TypeElement recordTypeElement)
	{
		//check if type already processed
		if(processedElements.containsKey(recordTypeElement.getQualifiedName().toString()))
		{
			return;
		}
		//we store all used attribute-names, so we can check for collissions later
		final Set<String> attributeNames = new HashSet<>(ProcessorUtils.getAllAttributeNames(processingEnv, recordTypeElement ));
		processedElements.put(recordTypeElement.getQualifiedName().toString(), attributeNames);
		
		//group all association-annotations
		final List<BelongsTo> allBelongsTo = new ArrayList<>(Arrays.asList( recordTypeElement.getAnnotationsByType( BelongsTo.class)));
		final List<Has> allHas = new ArrayList<>(Arrays.asList( recordTypeElement.getAnnotationsByType( Has.class)));
		final List<HasManyThrough> allHasThrough = new ArrayList<>(Arrays.asList( recordTypeElement.getAnnotationsByType( HasManyThrough.class)));

		final Associations assocs;
		if((assocs = recordTypeElement.getAnnotation( Associations.class)) != null)
		{
			allBelongsTo.addAll( Arrays.asList( assocs.belongsTo()));
			allHas.addAll( Arrays.asList( assocs.has()));
			allHasThrough.addAll( Arrays.asList( assocs.hasThrough()));
		}
		
		final boolean withNullControl = addNullableAnnotation();
		
		//we can't write into an existing source-file so we must create a new one
		try
		{
			final String generatedFileName = recordTypeElement.getSimpleName()+ "Associations";
			final String qualifiedGeneratedFileName = recordTypeElement.getQualifiedName()+"Associations";
			JavaFileObject destFile = processingEnv.getFiler().createSourceFile( qualifiedGeneratedFileName, recordTypeElement);
			try(Writer writer = destFile.openWriter())
			{
				//we create an interface with given validation-methods as default-methods and extending ValidatedRecord
				writer.append( "package ");
				writer.append( processingEnv.getElementUtils().getPackageOf( recordTypeElement).getQualifiedName().toString());
				writer.append( ";\n");

				
				writer.append( "import ").append( Consumer.class.getCanonicalName()).append( ";\n");
				if(withNullControl)
				{
					writer.append( "import ").append( Nullable.class.getCanonicalName()).append( ";\n");
					writer.append( "import ").append( Nonnull.class.getCanonicalName()).append( ";\n");
				}
				writer.append( "import ").append( Generated.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( RecordBase.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( Condition.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( Conditions.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( Comparison.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( AssociationHelper.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( RecordSet.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( HasManyAssociationSet.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( HasManyThroughAssociationSet.class.getCanonicalName()).append( ";\n");
				writer.append( "import ").append( Attribute.class.getCanonicalName()).append( ";\n");
				writer.append( "\n");
				
				writer.append( "@Generated(value = {\"").append( getClass().getCanonicalName()).append( "\"}, date = \"")
						.append( ISO_8601_DATE_FORMAT.format( new Date())).append( "\")\n");
				writer.append( "interface ").append( generatedFileName ).append(" extends ").append( ActiveRecord.class.getCanonicalName());

				writer.append(" {\n\n");

				//check association-names and write association-methods
				for(BelongsTo belongsTo : allBelongsTo)
				{
					if(attributeNames.contains( belongsTo.name()))
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Association-name '"+belongsTo.name()+"' already in use for attribute!", recordTypeElement);
						continue;
					}
					attributeNames.add( belongsTo.name());
					writer.append( generateBelongsTo( belongsTo, withNullControl));
				}
				for(Has has : allHas)
				{
					if(attributeNames.contains( has.name()))
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Association-name '"+has.name()+"' already in use for attribute!", recordTypeElement);
						continue;
					}
					attributeNames.add( has.name());
					writer.append( has.isHasOne() ? generateHasOne( has, withNullControl) : generateHasMany( has, withNullControl));
				}
				for(HasManyThrough hasManyThrough : allHasThrough)
				{
					if(attributeNames.contains( hasManyThrough.name()))
					{
						processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Association-name '"+hasManyThrough.name()+"' already in use for attribute!", recordTypeElement);
						continue;
					}
					attributeNames.add( hasManyThrough.name());
					writer.append( generateHasManyThrough( hasManyThrough, withNullControl));
				}

				writer.append( "}");
			}
			processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Generated: " + 
					processingEnv.getElementUtils().getPackageOf( recordTypeElement).getQualifiedName().toString()+ '.' +generatedFileName,
					recordTypeElement);
			
			//warn if type does not extend generated type
			if(!recordTypeElement.getInterfaces().stream().anyMatch( (TypeMirror interfaceMirror) -> interfaceMirror.toString().equals( generatedFileName)))
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
	private static String generateAttributeAnnotation(@Nonnull final BelongsTo source)
	{
		if(source.associationForeignKey().isEmpty())
		{
			return "\t@Attribute( name = \"" + source.name() + "\", type = Integer.class)\n";
		}
		//can't generate attribute-annotation without knowing the type of the foreign key
		return "";
	}
	
	private static String generateBelongsTo(@Nonnull final BelongsTo annotation, boolean withNullControl)
	{
		StringBuilder code = new StringBuilder(1000);
		final String thisAssociationKey = annotation.associationKey().isEmpty() ? annotation.name() : annotation.associationKey();
		final String typeName = ProcessorUtils.getTypeMirror(null, annotation::associatedType).toString();
		
		//Getter-Method
		
		if(withNullControl)
		{
			code.append( "\t@Nullable\n");
		}
		code.append( generateAttributeAnnotation( annotation));
		//public default <record-type> get<Name>() {
		code.append( "\tpublic default ").append( typeName).append( " get").
				append( Attributes.toCamelCase( annotation.name())).append( "() {\n");
		if(annotation.associationForeignKey().isEmpty())
		{
			//return AssociationHelper.getBelongsTo(this, <record-type>.class, <association-key>);
			code.append( "\t\treturn AssociationHelper.getBelongsTo(this, ").append( typeName).
					append(".class, \"").append(thisAssociationKey).append("\");\n");
		}
		else
		{
			//RecordBase<<record-type>> otherBase = getBase().getCore().getBase(<record-type>.class);
			code.append( "\t\tRecordBase<").append(typeName).append("> otherBase = getBase().getCore().getBase(").
					append( typeName).append( ".class);\n");
			//Object foreignKey = getBase().getStore().getValue(getBase(), getPrimaryKey(), <association-key>);
			code.append( "\t\tObject foreignKey = getBase().getStore().getValue( getBase(), getPrimaryKey(), \"").
					append( thisAssociationKey).append( "\");\n");
			//return foreignKey == null ? null : otherBase.findFirstFor(<association-foreign-key>, foreignKey);
			code.append( "\t\treturn foreignKey == null ? null : otherBase.findFirstFor(\"").
					append( annotation.associationForeignKey()).append( "\", foreignKey);\n");
		}
		// }
		code.append( "\t}\n\n");
		
		//Setter-Method
		
		if(!annotation.isReadOnly())
		{
			//public default void set<Name>(<record-type> value) {
			code.append( "\tpublic default void set").append( Attributes.toCamelCase( annotation.name())).
					append( '(').append((withNullControl ? "@Nullable " : "")).append( typeName).
					append( " value) {\n");
			if(annotation.associationForeignKey().isEmpty())
			{
				//Integer foreignKey = value == null ? null : value.getPrimaryKey();
				code.append( "\t\tInteger foreignKey = value == null ? null : value.getPrimaryKey();\n");
			}
			else
			{
				//Object foreignKey = value == null ? null : value.getBase().getStore().getValue(value.getBase(), value.getPrimaryKey(), <association-foreign-key>);
				code.append( "\t\tObject foreignKey = value == null ? null : value.getBase().getStore().getValue(value.getBase(), value.getPrimaryKey(), \"").
						append( annotation.associationForeignKey() ).append( "\");\n");
			}
			//getBase().getStore().setValue(getBase(), getPrimaryKey(), <association-key>, foreignKey);
			code.append( "\t\tgetBase().getStore().setValue( getBase(), getPrimaryKey(), \"").append( thisAssociationKey).
					append( "\", foreignKey);\n");
			// }
			code.append( "\t}\n\n");
		}
		return code.toString();
	}
	
	private static String generateHasOne(@Nonnull final Has annotation, boolean withNullControl)
	{
		StringBuilder code = new StringBuilder(1000);
		final String typeName = ProcessorUtils.getTypeMirror(null, annotation::associatedType).toString();
		
		//Getter-Method
		
		if(withNullControl)
		{
			code.append( "\t@Nullable\n");
		}
		//public default <record-type> get<Name>() {
		code.append( "\tpublic default ").append( typeName).append( " get").
				append( Attributes.toCamelCase( annotation.name())).append( "() {\n");
		if(annotation.associationForeignKey().isEmpty())
		{
			//return AssociationHelper.getHasOne(this, <record-type>.class, <association-key>)
			code.append( "\t\treturn AssociationHelper.getHasOne(this, ").append( typeName).
					append( ".class, \"").append( annotation.associationKey()).append( "\");\n");
		}
		else
		{
			//RecordBase<<record-type>> otherBase = getBase().getCore().getBase(<record-type>.class);
			code.append( "\t\tRecordBase<").append(typeName).append("> otherBase = getBase().getCore().getBase(").
					append( typeName).append( ".class);\n");
			//Object foreignKey = getBase().getStore().getValue(getBase(), getPrimaryKey(), <association-foreign-key>);
			code.append( "\t\tObject foreignKey = getBase().getStore().getValue(getBase(), getPrimaryKey(), \"").
					append(annotation.associationForeignKey()).append( "\");\n");
			//return foreignKey == null ? null : AssociationHelper.getHasOne(foreignKey, otherBase, <association-key>)
			code.append( "\t\treturn foreignKey == null ? null : AssociationHelper.getHasOne(foreignKey, otherBase, \"").
					append(annotation.associationKey()).append("\");\n");
		}
		// }
		code.append( "\t}\n\n");
		
		//Setter-Method
		
		if(!annotation.isReadOnly())
		{
			//public default void set<Name>(<record-type> value) {
			code.append( "\tpublic default void set").append( Attributes.toCamelCase( annotation.name())).
					append( '(').append((withNullControl ? "@Nullable " : "")).append( typeName).append( " value) {\n");
			//if(value == null) return;
			code.append( "\t\tif(value == null) return;\n");
			if(annotation.associationForeignKey().isEmpty())
			{
				//AssociationHelper.setHasOne(this, value, <association-key>)
				code.append( "\t\tAssociationHelper.setHasOne(this, value, \"").append( annotation.associationKey()).
						append( "\");\n");
			}
			else
			{
				//Object foreignKey = getBase().getStore().getValue(getBase(), getPrimaryKey(), <association-foreign-key>);
				code.append( "\t\tObject foreignKey = getBase().getStore().getValue(getBase(), getPrimaryKey(), \"").
						append( annotation.associationForeignKey() ).append( "\");\n");
				//value.getBase().getStore().setValue(value.getBase(), value.getPrimaryKey(), <association-key>, foreignKey);
				code.append( "\t\tvalue.getBase().getStore().setValue( value.getBase(), value.getPrimaryKey(), \"").
						append( annotation.associationKey()).append( "\", foreignKey);\n");
			}
			// }
			code.append( "\t}\n\n");
		}
		
		return code.toString();
	}
	
	private static String generateHasMany(@Nonnull final Has annotation, boolean withNullControl)
	{
		StringBuilder code = new StringBuilder(1000);
		final String typeName = ProcessorUtils.getTypeMirror(null, annotation::associatedType).toString();
		
		//Getter-Method
		
		if(withNullControl)
		{
			code.append( "\t@Nonnull\n");
		}
		//public default RecordSet<<record-type>> get<Name>() {
		code.append( "\tpublic default RecordSet<").append( typeName).append( "> get").
				append( Attributes.toCamelCase( annotation.name()) ).append( "() {\n");
		
		if(annotation.associationForeignKey().isEmpty())
		{
			//return AssociationHelper.getHasManySet(this, <record-type>.class, <association-key>);
			code.append( "\t\treturn AssociationHelper.getHasManySet(this, ").append(typeName).
					append(".class, \"" ).append( annotation.associationKey()).append( "\");\n");
		}
		else
		{
			//final RecordBase<<record-type>> otherBase = getBase().getCore().getBase( <record-type>.class);
			code.append( "\t\tfinal RecordBase<").append(typeName).
					append("> otherBase = getBase().getCore().getBase(" ).append( typeName).
					append( ".class);\n");
			//final Object foreignKey = getBase().getStore().getValue( getBase(), getPrimaryKey(), <association-foreign-key>);
			code.append( "\t\tfinal Object foreignKey = getBase().getStore().getValue( getBase(), getPrimaryKey(), \"").
					append( annotation.associationForeignKey()).append( "\");\n");
			//final Condition cond = Conditions.is(<association-key>, foreignKey);
			code.append( "\t\tfinal Condition cond = Conditions.is(\"").append(annotation.associationKey()).append("\", foreignKey);\n");
			//final Consumer<<record-type>> setAssoc = (final <record-type> t) -> otherBase.getStore().setValue( otherBase, t.getPrimaryKey(), <association-key>, foreignKey );
			code.append( "\t\tfinal Consumer<").append( typeName).append( "> setAssoc = (final ").
					append( typeName).append( " t) -> otherBase.getStore().setValue( otherBase, t.getPrimaryKey(), \"").
					append( annotation.associationKey()).append( "\", foreignKey);\n");
			//final Consumer<<record-type>> unsetAssoc = (final <record-type> t) -> otherBase.getStore().setValue( otherBase, t.getPrimaryKey(), <association-key>, null );
			code.append( "\t\tfinal Consumer<").append( typeName).append( "> unsetAssoc = (final ").
					append( typeName).append( " t) -> otherBase.getStore().setValue( otherBase, t.getPrimaryKey(), \"").
					append( annotation.associationKey() ).append( "\", null);\n");
			//return new HasManyAssociationSet<<record-type>>(otherBase, cond, setAssoc, unsetAssoc );
			code.append( "\t\treturn new HasManyAssociationSet<").append(typeName).
					append(">(otherBase, cond, null, setAssoc, unsetAssoc );");
		}
		
		// }
		code.append( "\t}\n\n");

		return code.toString();
	}
	
	private static String generateHasManyThrough(@Nonnull final HasManyThrough annotation, boolean withNullControl)
	{
		StringBuilder code = new StringBuilder(500);
		final String typeName = ProcessorUtils.getTypeMirror(null, annotation::associatedType).toString();
		
		//Getter-Method
		if(withNullControl)
		{
			code.append( "\t@Nonnull\n");
		}
		//public default RecordSet<<record-type>> get<Name>() {
		code.append( "\tpublic default RecordSet<").append( typeName).append( "> get").
				append( Attributes.toCamelCase( annotation.name()) ).append( "() {\n");
		
		//return AssociationHelper.getHasManyThroughSet(this, <record-type>.class, <mapping-table>, <mapping-this-key>, <mapping-other-key>);
		code.append( "\t\treturn AssociationHelper.getHasManyThroughSet(this, " ).append( typeName).
				append( ".class, \"").append( annotation.mappingTable()).append( "\", \"").
				append( annotation.mappingTableThisKey()).append( "\", \"").append( annotation.mappingTableAssociatedKey()).
				append( "\");\n");
		
		// }
		code.append( "\t}\n\n");
		
		
		return code.toString();
	}
}
