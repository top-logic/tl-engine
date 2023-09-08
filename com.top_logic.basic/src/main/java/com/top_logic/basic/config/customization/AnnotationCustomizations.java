/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.customization;

import java.lang.annotation.Annotation;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Strategy for resolving annotations on types and {@link PropertyDescriptor}s.
 * 
 * <p>
 * By redirecting {@link Class#getAnnotation(Class)} and
 * {@link PropertyDescriptor#getAnnotation(Class)} to such strategy, it is possible to customize
 * statically declared annotations.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AnnotationCustomizations {

	/**
	 * Looks up the given annotation on the given configuration type.
	 * 
	 * @param configType
	 *        The {@link ConfigurationDescriptor} to resolve the annotation for.
	 * @param annotationType
	 *        The {@link Annotation} to resolve.
	 * @return The {@link Annotation} of the given annotation type, or <code>null</code>, if no such
	 *         annotation is specified on the given type.
	 * 
	 * @see Class#getAnnotation(Class)
	 */
	<T extends Annotation> T getAnnotation(ConfigurationDescriptor configType, Class<T> annotationType);

	/**
	 * Looks up the given annotation on the given property.
	 * 
	 * @param property
	 *        The {@link PropertyDescriptor} to resolve annotations for.
	 * @param annotationType
	 *        The {@link Annotation} to resolve.
	 * @return The {@link Annotation} of the given annotation type, or <code>null</code>, if no such
	 *         annotation is specified on the given property.
	 * 
	 * @see PropertyDescriptor#getAnnotation(Class)
	 */
	<T extends Annotation> T getAnnotation(PropertyDescriptor property, Class<T> annotationType);

	/**
	 * Looks up the given annotation on the given type.
	 * 
	 * @param type
	 *        The Java type to resolve the annotation for.
	 * @param annotationType
	 *        The {@link Annotation} to resolve.
	 * @return The {@link Annotation} of the given annotation type, or <code>null</code>, if no such
	 *         annotation is specified on the given type.
	 * 
	 * @see Class#getAnnotation(Class)
	 */
	<T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationType);

}
