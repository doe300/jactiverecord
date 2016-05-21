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
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/**
 *
 * @author doe300
 * @since 0.3
 */
public final class ProcessorUtils
{
	
	/**
	 * Option to check if the used attributes exists
	 * @since 0.4
	 */
	public static final String OPTION_CHECK_ATTRIBUTES = "record.annotations.checkAttributes";
	
	/**
	 * Option to enable <code>@Nullable</code> and <code>@Nonnull</code> annotations for generated code
	 * @since 0.4
	 */
	public static final String OPTION_ADD_NULLABLE_ANNOTATIONS = "record.annotations.nullableAnnotations";
	
	@Nullable
	static ExecutableElement getAnnotationMemberName(@Nonnull final AnnotationMirror mirror, @Nonnull final String name)
	{
		for(final ExecutableElement el : mirror.getElementValues().keySet())
		{
			if(el.getSimpleName().contentEquals( name))
			{
				return el;
			}
		}
		return null;
	}
	
	@Nullable
	static AnnotationValue getAnnotationMemberValue(@Nonnull final AnnotationMirror mirror, @Nonnull final String name)
	{
		for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet())
		{
			if(entry.getKey().getSimpleName().contentEquals( name))
			{
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * @param processingEnv
	 * @param classElement
	 * @return a list of all used attribute-names
	 * @since 0.3
	 */
	@Nonnull
	public static List<String> getAllAttributeNames(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final TypeElement classElement)
	{
		List<String> attributeNames = new ArrayList<>(20);
		for(ExecutableElement ee : ElementFilter.methodsIn( processingEnv.getElementUtils().getAllMembers( classElement)))
		{
			if(ee.getAnnotation( ExcludeAttribute.class) != null)
			{
				continue;
			}
			if(ee.getAnnotation( Attribute.class) != null)
			{
				attributeNames.add( ee.getAnnotation( Attribute.class).name());
			}
			else if(ee.getAnnotation( AttributeGetter.class) != null)
			{
				attributeNames.add( ee.getAnnotation( AttributeGetter.class).name());
			}
			else if(ee.getAnnotation( AttributeSetter.class) != null)
			{
				attributeNames.add( ee.getAnnotation( AttributeSetter.class).name());
			}
		}
		return attributeNames;
	}
	
	static Stream<VariableElement> getAllStringConstants(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final TypeElement classElement)
	{
		final TypeMirror stringType = processingEnv.getElementUtils().getTypeElement( String.class.getCanonicalName()).asType();
		return ElementFilter.fieldsIn( processingEnv.getElementUtils().getAllMembers( classElement)).stream().
				filter( (VariableElement ve) -> ve.getModifiers().contains( Modifier.FINAL) 
						&& ve.getModifiers().contains( Modifier.STATIC) && ve.getModifiers().contains( Modifier.STATIC)).
				filter( (VariableElement ve) -> processingEnv.getTypeUtils().isSameType( stringType, ve.asType()));
	}
	
	static Stream<VariableElement> getAllAttributeStringConstants(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final TypeElement classElement)
	{
		List<String> allAttributes = getAllAttributeNames( processingEnv, classElement );
		return getAllStringConstants( processingEnv, classElement ).
				filter( (VariableElement ve) -> allAttributes.contains( (String)ve.getConstantValue()) );
	}
	
	static Stream<VariableElement> getAllNonAttributeStringConstants(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final TypeElement classElement)
	{
		List<String> allAttributes = getAllAttributeNames( processingEnv, classElement );
		return getAllStringConstants( processingEnv, classElement ).
				filter( (VariableElement ve) -> !allAttributes.contains( (String)ve.getConstantValue()) );
	}
	
	/**
	 * Uses a neat trick to get a useful TypeMirror from an unusable Class
	 * 
	 * @param processingEnv
	 * @param type
	 * @return the type-mirror for this type
	 * @since 0.4
	 * @see "http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/"
	 */
	@Nullable
	public static DeclaredType getTypeMirror(@Nullable final ProcessingEnvironment processingEnv, @Nonnull final Supplier<Class<?>> type)
	{
		try{
			type.get().getCanonicalName();
		}
		catch(MirroredTypeException mte)
		{
			return ( DeclaredType ) mte.getTypeMirror();
		}
		//fall back to default behavior
		if(processingEnv == null)
		{
			return null;
		}
		return ( DeclaredType )processingEnv.getElementUtils().getTypeElement( type.get().getCanonicalName()).asType();
	}
	
	/**
	 * Returns the type-mirror for the given <code>type</code> or the given <code>default-type</code> if the <code>type</code> is {@link Void}
	 * or <code>null</code>
	 * 
	 * @param processingEnv
	 * @param type the type to check
	 * @param defaultType the default-type
	 * @return the type-mirror or the given default-type
	 * @see #getTypeMirror(javax.annotation.processing.ProcessingEnvironment, java.util.function.Supplier) 
	 * @see #isClassSet(javax.annotation.processing.ProcessingEnvironment, javax.lang.model.type.TypeMirror) 
	 * @since 0.5
	 */
	@Nullable
	public static DeclaredType getTypeMirrorOrDefault(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final Supplier<Class<?>> type, @Nullable final DeclaredType defaultType)
	{
		DeclaredType typeMirror = getTypeMirror( processingEnv, type );
		if(isClassSet( processingEnv, typeMirror ))
		{
			return typeMirror;
		}
		return defaultType;
	}
	
	/**
	 * @param processingEnv
	 * @param type
	 * @return whether the type in the type-mirror is not {@link Void}
	 * @since 0.4
	 */
	public static boolean isClassSet(@Nonnull final ProcessingEnvironment processingEnv, @Nullable final TypeMirror type)
	{
		if(type == null)
		{
			return false;
		}
		final TypeMirror voidClass = processingEnv.getElementUtils().getTypeElement( Void.class.getCanonicalName()).asType();
		return !processingEnv.getTypeUtils().isSameType( voidClass, type);
	}
	
	/**
	 * @param processingEnv
	 * @param classElement
	 * @param methodName
	 * @param predicate
	 * @param modifiers
	 * @return the first method matching the given criteria
	 * @since 0.4
	 */
	@Nullable
	public static ExecutableElement getClassMethod(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final TypeElement classElement,
			@Nonnull final String methodName, @Nonnull final Predicate<ExecutableElement> predicate, @Nullable final Modifier... modifiers)
	{
		return getClassMethods( processingEnv, classElement, methodName, predicate, modifiers ).findFirst().orElse( null);
	}
	
	/**
	 * @param processingEnv
	 * @param classElement
	 * @param methodName
	 * @param predicate
	 * @param modifiers
	 * @return all methods matching the given criteria
	 * @since 0.4
	 */
	public static Stream<ExecutableElement> getClassMethods(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final TypeElement classElement,
			@Nullable final String methodName, @Nonnull final Predicate<ExecutableElement> predicate, @Nullable final Modifier... modifiers)
	{
		return ElementFilter.methodsIn( processingEnv.getElementUtils().
				getAllMembers( classElement)).stream().
				filter( (ExecutableElement ee) -> methodName == null || ee.getSimpleName().contentEquals( methodName)).
				filter( (ExecutableElement ee) -> ee.getModifiers().containsAll( Arrays.asList( modifiers))).
				filter( predicate);
	}
	
	/**
	 * Checks whether the record-type element extends the generated type (in the same package) with the given type-name
	 * 
	 * @param processingEnv
	 * @param typeName
	 * @param recordTypeElement
	 * @return whether the given <code>recordTypeElement</code> extends the generated type with the given <code>typeName</code>
	 * @since 0.8
	 */
	public static boolean extendsType(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final String typeName, @Nonnull final TypeElement recordTypeElement)
	{
		final String qualifiedTypeName = processingEnv.getElementUtils().getPackageOf( recordTypeElement) + "." + typeName;
		if(recordTypeElement.getSuperclass().toString().equals( qualifiedTypeName))
		{
			return true;
		}
		return recordTypeElement.getInterfaces().stream().anyMatch( (TypeMirror interfaceMirror) -> 
		{
			return interfaceMirror.toString().equals( qualifiedTypeName);
		});
	}
	
	private ProcessorUtils()
	{
	}
}
