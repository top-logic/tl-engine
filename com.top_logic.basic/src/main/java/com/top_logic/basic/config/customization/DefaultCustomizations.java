/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.customization;

import java.lang.annotation.Annotation;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Base-class for {@link AnnotationCustomizations}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultCustomizations implements AnnotationCustomizations {

	@Override
	public final <T extends Annotation> T getAnnotation(ConfigurationDescriptor descriptor, Class<T> annotationType) {
		return descriptor.getAnnotation(this, annotationType);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationType) {
		return type.getAnnotation(annotationType);
	}

	@Override
	public final <T extends Annotation> T getAnnotation(PropertyDescriptor property, Class<T> annotationType) {
		T localAnnotation = getLocalAnnotation(property, annotationType);
		if (localAnnotation != null) {
			return localAnnotation;
		}

		// Use the default from the value type.
		Class<?> instanceType = property.getInstanceType();
		if (instanceType != null) {
			T instanceAnnotation = getAnnotation(instanceType, annotationType);
			if (instanceAnnotation != null) {
				return instanceAnnotation;
			}
		}

		Class<?> elementType = property.getElementType();
		if (elementType != null) {
			return getAnnotation(elementType, annotationType);
		} else {
			return getAnnotation(property.getType(), annotationType);
		}
	}

	/**
	 * Looks up the given annotation if it is directly given at the property (not its value type).
	 */
	protected <T extends Annotation> T getLocalAnnotation(PropertyDescriptor property, Class<T> annotationType) {
		return property.getAnnotation(annotationType);
	}

}
