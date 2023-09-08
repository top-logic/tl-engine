/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.customization;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.PropertyDescriptor;

/**
 * {@link DefaultCustomizations} that are filled programmatically.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ProgrammaticCustomizations extends DefaultCustomizations {

	private Map<Class<?>, Map<Class<?>, Annotation>> _typeAnnotations = Collections.emptyMap();

	private Map<PropertyDescriptor, Map<Class<?>, Annotation>> _propertyAnnotations = Collections.emptyMap();

	/**
	 * Adds the given annotation for the given type.
	 * 
	 * <p>
	 * If there is already a annotation of the same {@link Annotation#annotationType() type}, the
	 * former one is replaced.
	 * </p>
	 * 
	 * @param type
	 *        The type to add {@link Annotation} for.
	 * @param annotation
	 *        The {@link Annotation} to return for the given type.
	 */
	public void addAnnotation(Class<?> type, Annotation annotation) {
		_typeAnnotations = addAnnotation(_typeAnnotations, type, annotation);
	}

	/**
	 * Adds the given annotation for the given {@link PropertyDescriptor}.
	 * 
	 * <p>
	 * If there is already a annotation of the same {@link Annotation#annotationType() type}, the
	 * former one is replaced.
	 * </p>
	 * 
	 * @param property
	 *        The {@link PropertyDescriptor} to add {@link Annotation} for.
	 * @param annotation
	 *        The {@link Annotation} to return for the given property.
	 */
	public void addAnnotation(PropertyDescriptor property, Annotation annotation) {
		_propertyAnnotations = addAnnotation(_propertyAnnotations, property, annotation);
	}

	private <T> Map<T, Map<Class<?>, Annotation>> addAnnotation(Map<T, Map<Class<?>, Annotation>> annotations, T key,
			Annotation annotation) {
		switch (annotations.size()) {
			case 0: {
				annotations = new HashMap<>();
				putForNewKey(annotations, key, annotation);
				break;
			}
			default: {
				Map<Class<?>, Annotation> formerValue = annotations.get(key);
				if (formerValue == null) {
					putForNewKey(annotations, key, annotation);
				} else {
					if (formerValue.size() == 1) {
						formerValue = new HashMap<>(formerValue);
						annotations.put(key, formerValue);
					}
					formerValue.put(annotation.annotationType(), annotation);
				}
			}
		}
		return annotations;
	}

	private <T> void putForNewKey(Map<T, Map<Class<?>, Annotation>> annotations, T key, Annotation annotation) {
		annotations.put(key, Collections.singletonMap(annotation.annotationType(), annotation));
	}

	@Override
	protected <T extends Annotation> T getLocalAnnotation(PropertyDescriptor property, Class<T> annotationType) {
		Map<Class<?>, Annotation> annotationByProperty = _propertyAnnotations.get(property);
		if (annotationByProperty != null) {
			Annotation annotation = annotationByProperty.get(annotationType);
			if (annotation != null) {
				return annotationType.cast(annotation);
			}
		}
		return super.getLocalAnnotation(property, annotationType);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationType) {
		Map<Class<?>, Annotation> annotationByType = _typeAnnotations.get(type);
		if (annotationByType != null) {
			Annotation annotation = annotationByType.get(annotationType);
			if (annotation != null) {
				return annotationType.cast(annotation);
			}
		}
		return super.getAnnotation(type, annotationType);
	}

}
