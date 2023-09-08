/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

/**
 * Util class to handle {@link Element}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeUtils {

	private static final String FRAMEWORK_INTERNAL = "com.top_logic.basic.annotation.FrameworkInternal";

	private static final Collection<String> ENUM_SYNTHESIZED = Arrays.asList("values", "valueOf");

	/**
	 * Whether the given element is either {@link Modifier#PUBLIC public} or
	 * {@link Modifier#PROTECTED protected}.
	 */
	public static boolean publicOrProtected(Element element) {
		Set<Modifier> modifiers = element.getModifiers();
		return modifiers.contains(Modifier.PROTECTED) || modifiers.contains(Modifier.PUBLIC);
	}

	/**
	 * String representation of a {@link Name}.
	 */
	public static String asString(Name name) {
		return name.toString();
	}

	/**
	 * String representation of elements {@link Element#getSimpleName() simple name}.
	 */
	public static String asString(Element element) {
		return asString(element.getSimpleName());
	}

	/**
	 * String representation of elements {@link QualifiedNameable#getQualifiedName() qualified
	 * name}.
	 */
	public static String qName(QualifiedNameable element) {
		return asString(element.getQualifiedName());
	}

	/**
	 * Whether the given method is a synthesised {@link Enum} method.
	 */
	public static boolean isEnumSynthesized(Element element) {
		return element.getKind() == ElementKind.METHOD
			&& element.getEnclosingElement().getKind() == ElementKind.ENUM
			&& ENUM_SYNTHESIZED.contains(asString(element));
	}

	/**
	 * Cast to {@link TypeElement}
	 */
	public static TypeElement asTypeElement(Element element) {
		return (TypeElement) element;
	}

	private static boolean hidden(List<? extends AnnotationMirror> annotations) {
		for (AnnotationMirror annotation : annotations) {
			TypeElement annotationType = asTypeElement(annotation.getAnnotationType().asElement());
			if (annotationType.getQualifiedName().contentEquals(FRAMEWORK_INTERNAL)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given element is treated as "hidden", i.e. it is not included in the
	 * documentation.
	 */
	public static boolean hiddenElement(Element element) {
		if (!publicOrProtected(element)) {
			return true;
		}
		if (isEnumSynthesized(element)) {
			return true;
		}

		return hidden(element.getAnnotationMirrors());
	}

	/**
	 * Negation of {@link #hiddenElement(Element)}.
	 */
	public static boolean notHiddenElement(Element element) {
		return !hiddenElement(element);
	}

	/**
	 * Whether the given package (and its content) is not included in the documentation.
	 */
	public static boolean hiddenPackage(PackageElement element) {
		return hidden(element.getAnnotationMirrors());
	}

	private static boolean hasModifier(Element elem, Modifier modifier) {
		return elem.getModifiers().contains(modifier);
	}

	/**
	 * Whether the given element has the modifier {@link Modifier#STATIC}.
	 */
	public static boolean isStatic(Element element) {
		return hasModifier(element, Modifier.STATIC);
	}

	/**
	 * Whether the given element has the modifier {@link Modifier#FINAL}.
	 */
	public static boolean isFinal(Element element) {
		return hasModifier(element, Modifier.FINAL);
	}

	/**
	 * Whether the given element has the modifier {@link Modifier#ABSTRACT}.
	 */
	public static boolean isAbstract(Element element) {
		return hasModifier(element, Modifier.ABSTRACT);
	}

	/**
	 * Checks element kind for being a {@link ElementKind#isClass() class}.
	 */
	public static boolean isClass(Element element) {
		return element.getKind().isClass();
	}

	/**
	 * Checks element kind for being an {@link ElementKind#isInterface() interface}.
	 */
	public static boolean isInterface(Element element) {
		return element.getKind().isInterface();
	}

	/**
	 * Determines the enclosing class or interface of the given element, if there is one.
	 */
	public static TypeElement containingClass(Element element) {
		Element enclosingElement = element.getEnclosingElement();
		if (enclosingElement == null) {
			return null;
		}
		if (isClass(enclosingElement) || isInterface(enclosingElement)) {
			return asTypeElement(enclosingElement);
		}
		return null;
	}

	/**
	 * Determines the constructors in the given type.
	 */
	public static List<ExecutableElement> constructorsIn(TypeElement element) {
		return ElementFilter.constructorsIn(element.getEnclosedElements());
	}

	/**
	 * Determines the fields in the given type.
	 */
	public static List<VariableElement> fieldsIn(TypeElement element) {
		return ElementFilter.fieldsIn(element.getEnclosedElements());
	}

	/**
	 * Determines the method in the given type.
	 */
	public static List<ExecutableElement> methodsIn(TypeElement element) {
		return ElementFilter.methodsIn(element.getEnclosedElements());
	}

	/**
	 * Determines the enum constants in the given type.
	 * 
	 * @implNote Accessing using {@link TypeUtils#fieldsIn(TypeElement)} would also deliver other
	 *           fields, e.g. external name field of the enum.
	 */
	public static List<VariableElement> enumConstantsIn(TypeElement element) {
		return element.getEnclosedElements().stream()
			.filter(inner -> ElementKind.ENUM_CONSTANT == inner.getKind())
			.map(VariableElement.class::cast)
			.collect(Collectors.toList());
	}

	/**
	 * Determines the inner types in the given type.
	 */
	public static List<TypeElement> typesIn(Element elem) {
		return ElementFilter.typesIn(elem.getEnclosedElements());
	}

}
