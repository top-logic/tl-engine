/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.ArrayUtil;

/**
 * Implementation of {@link AnnotatedElement} based on given {@link Annotation}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleAnnotatedElement implements AnnotatedElement {

	/** {@link AnnotatedElement} without any annotations */
	public static final AnnotatedElement NO_ANNOTATIONS = new SimpleAnnotatedElement(Collections.emptyMap());

	private final Map<Class<? extends Annotation>, Annotation> _directAnnotations;

	/**
	 * @param annotations
	 *        A {@link Map} mapping {@link Annotation#annotationType() annotation type} to the
	 *        corresponding annotation.
	 */
	public SimpleAnnotatedElement(Map<Class<? extends Annotation>, Annotation> annotations) {
		Objects.requireNonNull(annotations, "Annotations must not be null.");
		annotations.forEach(SimpleAnnotatedElement::checkAnnotationEntry);
		_directAnnotations = annotations;
	}

	private static void checkAnnotationEntry(Class<? extends Annotation> key, Annotation value) {
		Objects.requireNonNull(value, () -> "Value for key " + key + " must not be null.");
		if (key != value.annotationType()) {
			throw new IllegalArgumentException(
				"Key '" + key + "' for annotation '" + value + "' is not its annotation type.");
		}
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		Objects.requireNonNull(annotationClass, "Annotation class must not be null");
		return annotationClass.cast(_directAnnotations.get(annotationClass));
	}

	@Override
	public Annotation[] getAnnotations() {
		return getDeclaredAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return _directAnnotations.values().toArray(ArrayUtil.EMPTY_ANNOTATION_ARRAY);
	}

	@Override
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
		// All annotations are "declared"
		return getAnnotation(annotationClass);
	}

}

