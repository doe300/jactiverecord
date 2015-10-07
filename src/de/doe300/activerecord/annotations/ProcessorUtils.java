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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/**
 *
 * @author doe300
 * @since 0.3
 */
public final class ProcessorUtils
{
	
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
	 * @param classElement
	 * @return a list of all used attribute-names
	 * @since 0.3
	 */
	@Nonnull
	public static List<String> getAllAttributeNames(@Nonnull final TypeElement classElement)
	{
		//XXX include inherited attribute-names
		List<String> attributeNames = new ArrayList<>(20);
		for(ExecutableElement ee : ElementFilter.methodsIn( classElement.getEnclosedElements()))
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
		List<String> allAttributes = getAllAttributeNames( classElement );
		return getAllStringConstants( processingEnv, classElement ).
				filter( (VariableElement ve) -> allAttributes.contains( (String)ve.getConstantValue()) );
	}
	
	static Stream<VariableElement> getAllNonAttributeStringConstants(@Nonnull final ProcessingEnvironment processingEnv, @Nonnull final TypeElement classElement)
	{
		List<String> allAttributes = getAllAttributeNames( classElement );
		return getAllStringConstants( processingEnv, classElement ).
				filter( (VariableElement ve) -> !allAttributes.contains( (String)ve.getConstantValue()) );
	}
	
	@Nonnull
	public static Stream<TypeElement> getAllTypes(@Nonnull final ProcessingEnvironment processingEnv, @Nullable final TypeElement currentType, @Nullable final String pathPart)
	{
		if(pathPart == null || pathPart.indexOf( '.') < 0)
		{
			//we don't have any path yet, so suggest classes in the same package as the currentType
			if(currentType == null)
			{
				return Stream.empty();
			}
			final PackageElement packageEl = processingEnv.getElementUtils().getPackageOf( currentType);
			if(packageEl == null)
			{
				return Stream.empty();
			}
			return getAllTypesInPackage( packageEl );
		}
		String packagePath = pathPart.substring( 0, pathPart.lastIndexOf( '.'));
		PackageElement packageEl = processingEnv.getElementUtils().getPackageElement( packagePath);
		return getAllTypesInPackage( packageEl );
	}
	
	private static Stream<TypeElement> getAllTypesInPackage(@Nonnull final PackageElement packageEl)
	{
		return packageEl.getEnclosedElements().stream().filter( (Element e) ->
		{
			return e.getKind() == ElementKind.CLASS || e.getKind() == ElementKind.INTERFACE || e.getKind() == ElementKind.PACKAGE;
		}).flatMap( new Function<Element, Stream<? extends TypeElement>>()
		{
			@Override
			public Stream<? extends TypeElement> apply( Element t )
			{
				if(t.getKind() == ElementKind.CLASS || t.getKind() == ElementKind.INTERFACE)
				{
					return Stream.of( (TypeElement)t );
				}
				if(t.getKind() == ElementKind.PACKAGE)
				{
					return getAllTypesInPackage( (PackageElement)t );
				}
				return Stream.empty();
			}
		});
	}
	
	private ProcessorUtils()
	{
	}
}
