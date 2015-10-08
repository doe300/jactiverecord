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

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.migration.constraints.Index;
import de.doe300.activerecord.migration.constraints.Indices;
import de.doe300.activerecord.pojo.POJOBase;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.RecordType;
import de.doe300.activerecord.record.Searchable;
import de.doe300.activerecord.record.SingleTableInheritance;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import de.doe300.activerecord.record.validation.Validate;
import de.doe300.activerecord.record.validation.ValidatedRecord;
import de.doe300.activerecord.record.validation.Validates;
import de.doe300.activerecord.record.validation.ValidationType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

/**
 * Annotation-processor for all annotations applicable to classes defining record-types
 * 
 * @author doe300
 * @see "http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html"
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
	"de.doe300.activerecord.record.RecordType", "de.doe300.activerecord.record.Searchable",
	"de.doe300.activerecord.migration.constraints.Index","de.doe300.activerecord.record.SingleTableInheritance",
	"de.doe300.activerecord.record.validation.Validate", "de.doe300.activerecord.record.validation.Validates",
})
public class RecordTypeProcessor extends AbstractProcessor
{
	private static final Set<String> globalIndices = new HashSet<>(20);

	/**
	 *
	 */
	public RecordTypeProcessor()
	{
		//public no-arg constructor required
	}

	@Override
	public Iterable<? extends Completion> getCompletions( Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText )
	{
		//default order - suggest every used key ASC/DESC
		if(member.getSimpleName().contentEquals( "defaultOrder"))
		{
			Stream<Completion> indexCompletions = Stream.empty();
			if(element.getAnnotation( Index.class) != null || element.getAnnotation( Indices.class) != null)
			{
				indexCompletions = Arrays.stream( element.getAnnotationsByType( Index.class)).
						flatMap( ( Index t ) -> 
						{
							return Stream.of( Arrays.stream( t.columns()).collect( Collectors.joining( " ASC, ", "\"", " ASC\"")),
									Arrays.stream( t.columns()).collect( Collectors.joining( " DESC, ", "\"", " DESC\"")));
						}
				).map((String s) -> Completions.of( s, "Sort by Index"));
			}
			//XXX remove duplicates
			return Stream.concat( indexCompletions, ProcessorUtils.getAllAttributeNames((TypeElement)element).stream().
					flatMap( (String name) -> Stream.of( 
							Completions.of( '"' + name + " ASC\""),
							Completions.of( '"' + name + " DESC\"")
							))).collect( Collectors.toList());
		}
		//single inheritance type-column / record-type primary-key / validate attribute
		if(member.getSimpleName().contentEquals( "typeColumnName") || member.getSimpleName().contentEquals( "primaryKey")
				|| member.getSimpleName().contentEquals( "attribute"))
		{
			return ProcessorUtils.getAllAttributeStringConstants( processingEnv, (TypeElement)element).
					map( (VariableElement ve) -> Completions.of( element.getSimpleName()+"."+ve.getSimpleName().toString(), (String)ve.getConstantValue())).
					collect( Collectors.toList());
					
		}
		//single inheritance factory-method / validate custom-method
		if(member.getSimpleName().contentEquals( "factoryMethod") || member.getSimpleName().contentEquals( "customMethod"))
		{
			//XXX needs testing
			final AnnotationValue factoryClassValue = ProcessorUtils.getAnnotationMemberValue( annotation, "factoryClass");
			if(factoryClassValue == null || factoryClassValue.getValue() == null)
			{
				return Collections.emptySet();
			}
			//TODO problem: class-values are not present at compile-time!
			Class<?> factoryClass = ( Class<?> ) factoryClassValue.getValue();
			return Arrays.stream( factoryClass.getMethods()).map( (Method m) -> Completions.of( '"'+m.getName()+'"')).collect( Collectors.toList());
		}
		//searchable searchable columns / index columns / record-type default-columns
		if(member.getSimpleName().contentEquals( "searchableColumns") || member.getSimpleName().contentEquals( "columns" ) || member.getSimpleName().contentEquals( "defaultColumns"))
		{
			return Arrays.asList(
					//all columns
					Completions.of( 
					ProcessorUtils.getAllAttributeStringConstants( processingEnv, (TypeElement)element).
							map( (VariableElement ve) -> element.getSimpleName()+"."+ve.getSimpleName().toString()).
							collect( Collectors.joining( ", ", "{", "}")), "all attribute-constants"),
					//only primary key
					Completions.of( '"'+element.getAnnotation( RecordType.class).primaryKey()+'"', "primary key" ),
					//all attribute-names
					Completions.of( ProcessorUtils.getAllAttributeNames( (TypeElement)element).stream().
							map( (String name) -> '"'+name+'"').collect( Collectors.joining( ",", "{","}")), "all attributes")
					);
		}
		//index-name: suggest combination of index-columns
		if(member.getSimpleName().contentEquals( "name"))
		{
			//XXX needs testing
			final AnnotationValue indexColumnValue = ProcessorUtils.getAnnotationMemberValue( annotation, "columns");
			if(indexColumnValue == null || indexColumnValue.getValue() == null)
			{
				return Collections.emptySet();
			}
			String[] indexColumns = (String[])indexColumnValue.getValue();
			return Collections.singleton( Completions.of( Stream.of( indexColumns ).collect( Collectors.joining("_"))));
		}
		return Collections.emptyList();
	}
	
	

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv )
	{	
		//type-annotations
		roundEnv.getElementsAnnotatedWith( RecordType.class).forEach( (final Element e) -> {
			processRecordType( (TypeElement)e, e.getAnnotation( RecordType.class));
		});
		roundEnv.getElementsAnnotatedWith( SingleTableInheritance.class).forEach( (final Element e) -> {
			processSingleTableInheritance((TypeElement)e, e.getAnnotation( SingleTableInheritance.class));
		});
		roundEnv.getElementsAnnotatedWith( Index.class).forEach( (final Element e) -> {
			processIndex((TypeElement)e, e.getAnnotation( Index.class));
		});
		roundEnv.getElementsAnnotatedWith( Indices.class).forEach( (final Element e) -> {
			for(Index index : e.getAnnotation( Indices.class).value())
			{
				processIndex( (TypeElement)e, index);
			}
		});
		roundEnv.getElementsAnnotatedWith( Searchable.class).forEach( (final Element e) -> {
			processSearchables( (TypeElement)e, e.getAnnotation( Searchable.class));
		});
		roundEnv.getElementsAnnotatedWith( Validate.class).forEach( (final Element e) -> {
			processValidate( (TypeElement)e, e.getAnnotation( Validate.class));
		});
		roundEnv.getElementsAnnotatedWith( Validates.class).forEach( (final Element e) -> {
			for(Validate validate : e.getAnnotation( Validates.class).value())
			{
				processValidate( (TypeElement)e, validate);
			}
		});
		//we do not claim the annotations, another processor may use them
		return false;
	}
	
	private void processRecordType(final TypeElement recordTypeElement, final RecordType recordType)
	{
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
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A record-type must extend ActiveRecord",recordTypeElement);
		}
		//type-name must not be empty
		if(recordType.typeName().isEmpty())
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "record-type name must not be empty", recordTypeElement);
		}
		//type-name must not contain spaces
		if(recordType.typeName().chars().anyMatch( (final int i)-> i == ' '))
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "record-type name must not contain any spaces", recordTypeElement);
		}
		//primary-key must not be empty
		if(recordType.primaryKey().isEmpty())
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "primary-key column must not be empty", recordTypeElement);
		}
		//primary-key must not contain spaces
		if(recordType.primaryKey().chars().anyMatch( (final int i)-> i == ' '))
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "primary-key column must not contain any spaces", recordTypeElement);
		}
		//default-columns should not be empty
		if(recordType.defaultColumns().length == 0)
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Default columns should not be empty.", recordTypeElement);
		}
		//default-columns should contain primary-key
		if(!Arrays.stream( recordType.defaultColumns()).anyMatch( (String column) -> recordType.primaryKey().equals( column)))
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Default columns should contain the primary-key column", recordTypeElement);
		}
		//if type is concrete class and not single-inheritance, the constructor must accept (int, POJOBase)
		if(ElementKind.CLASS == recordTypeElement.getKind())
		{
			//TODO doesn't accept constructor (int, POJOBase<X>)
			final TypeMirror integerType = processingEnv.getElementUtils().getTypeElement( Integer.class.getCanonicalName()).asType();
			final TypeMirror recordBaseType = processingEnv.getElementUtils().getTypeElement( RecordBase.class.getCanonicalName()).asType();
			if(!ElementFilter.constructorsIn( recordTypeElement.getEnclosedElements()).stream().
					anyMatch( (ExecutableElement constructor) -> {
						return constructor.getParameters().size() == 2 && 
								processingEnv.getTypeUtils().isSubtype( constructor.getParameters().get(0).asType(), integerType) &&
								processingEnv.getTypeUtils().isSubtype( constructor.getParameters().get(1).asType(), recordBaseType );
					}))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "No suitable constructor found", recordTypeElement);
			}
		}
	}
	
	private void processSingleTableInheritance(final TypeElement type, final SingleTableInheritance inheritanceAnnotation)
	{
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
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A single-inheritance type must extend ActiveRecord", type);
		}
		//column-name must not be empty
		if(inheritanceAnnotation.typeColumnName().isEmpty())
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "inheritance-column must not be empty", type);
		}
		//column-name must not contain spaces
		if(inheritanceAnnotation.typeColumnName().chars().anyMatch( (final int i)-> i == ' '))
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "inheritance-column must not contain any spaces", type);
		}
		try
		{
			//factory-method must exist in factory-class and have the correct signature
			Method factoryMethod = inheritanceAnnotation.factoryClass().getMethod( inheritanceAnnotation.factoryMethod(), POJOBase.class, Integer.TYPE, Object.class );
			if(!ActiveRecord.class.isAssignableFrom( factoryMethod.getReturnType()))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Factory-method does not return a subtype of ActiveRecord", type);
			}
		}
		catch ( NoSuchMethodException | SecurityException ex )
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "No factory-method found with the correct signature", type);
			processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "The correct signature for a single-inheritance factory-method is:");
			processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "\"public static <T extends ActiveRecord> T methodName(POJOBase<T> base, int primaryKey, Object type);\"");
		}
	}
	
	private void processIndex(final TypeElement type, final Index indexAnnotation)
	{
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
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "An indexed type must extend ActiveRecord", type);
		}
		//index-name must not be empty
		if(indexAnnotation.name().isEmpty())
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Index-name must not be empty", type);
		}
		//index-name must not have any spaces
		if(indexAnnotation.name().chars().anyMatch( (final int i)-> i == ' '))
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Index-name must not contain any spaces", type);
		}
		//attributes need to exist for index-columns
		for(String column : indexAnnotation.columns())
		{
			if(!AttributeProcessor.testAttributeExists(processingEnv, type, column ))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Column '"+column+"' is indexed but was not found (could be a false positive)", type);
			}
		}
		//index-names should be globally unique (e.g. for SQLite)
		if(globalIndices.contains( indexAnnotation.name()))
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Index-name should be globally unique. This is required on some DBMS");
		}
		globalIndices.add( indexAnnotation.name());
	}
	
	private void processValidate(final TypeElement type, final Validate validateAnnotation)
	{
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
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A validated type must extend ValidatedRecord",type);
		}
		//attribute-name of the validation must not be empty
		if(validateAnnotation.attribute().isEmpty())
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "validated attribute-name must not be empty", type);
		}
		//attribute-name of the validation must not contain spaces
		if(validateAnnotation.attribute().chars().anyMatch( (final int i)-> i == ' '))
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "validated attribute-name must not contain any spaces", type);
		}
		//if type is set to custom, custom-class and custom-method must exist
		if(validateAnnotation.type() == ValidationType.CUSTOM)
		{
			if(Void.class.equals( validateAnnotation.customClass()))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Custom validation class must be set", type);
				return;
			}
			//TODO doesn't work with class
			String customMethodName = validateAnnotation.customMethod();
//			TypeElement customClassElement = processingEnv.getElementUtils().getTypeElement( validateAnnotation.customClass());
//			if(customClassElement == null)
//			{
//				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Could not find custom valdation-class: " + validateAnnotation.customClass(), type);
//			}
//			//if custom-class is set, custom-method must be set too
//			else 
			if(customMethodName.isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Custom validation-method must be set", type);
			}
//			//custom-method must be a Predicate<Object>
//			else if(ElementFilter.methodsIn( processingEnv.getElementUtils().getAllMembers( customClassElement)).stream().
//					filter( (ExecutableElement ee) -> {
//						return ee.getReturnType().getKind() == TypeKind.BOOLEAN && ee.getSimpleName().contentEquals( customMethodName )
//								&& ee.getParameters().size() == 1;
//					}).count() == 0)
//			{
//				processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "Validation-method must return a boolean value", type);
//			}
		}
		//if type is set to non-custom, warn about custom-class/-method not being used
		else
		{
			//TODO doesn#t work, can't access class
			if(/*!Void.class.equals( validateAnnotation.customClass()) ||*/ !validateAnnotation.customMethod().isEmpty())
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Custom-Class and -method will not be used unless the ValidationType is set to CUSTOM", type);
			}
		}
		//an attribure for the validated column must exist
		if(!AttributeProcessor.testAttributeExists(processingEnv, type, validateAnnotation.attribute()))
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Column '"+validateAnnotation.attribute()+"' is validated but was not found (could be a false positive)", type);
		}
	}
	
	private void processSearchables(final TypeElement searchableType, final Searchable searchable)
	{
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
			processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, "A Searchable type must extend ActiveRecord",searchableType);
		}
		//Should have a non-empty searchable-columns list
		if(searchable.searchableColumns().length==0)
		{
			processingEnv.getMessager().printMessage( Diagnostic.Kind.WARNING, "Empty searchableColumns!", searchableType);
		}
		//Should have (or inherit) attributes for all columns listed in the searchable-columns
		for(String column : searchable.searchableColumns())
		{
			if(!AttributeProcessor.testAttributeExists(processingEnv, searchableType, column ))
			{
				processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Column '"+column+"' is searchable but does not exist (This could be wrong)", searchableType);
			}
		}
	}
}
