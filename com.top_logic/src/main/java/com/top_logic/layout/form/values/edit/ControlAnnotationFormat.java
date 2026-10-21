/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.format.ClassFormat;
import com.top_logic.layout.form.values.edit.annotation.ControlAnnotation;

/**
 * {@link ClassFormat} that accepts only annotation types marked with {@link ControlAnnotation}.
 *
 * <p>
 * Used for the annotation that keys an entry of a map from annotations to controls, so that an
 * entry for an annotation no form looks up is rejected when the configuration is loaded.
 * </p>
 *
 * @see ControlAnnotationOptions
 */
public class ControlAnnotationFormat extends ClassFormat {

	/**
	 * Singleton {@link ControlAnnotationFormat} instance.
	 */
	@SuppressWarnings("hiding")
	public static final ControlAnnotationFormat INSTANCE = new ControlAnnotationFormat();

	/**
	 * Creates a {@link ControlAnnotationFormat}.
	 */
	protected ControlAnnotationFormat() {
		super();
	}

	@Override
	protected Class<?> getValueNonEmpty(String propertyName, CharSequence className) throws ConfigurationException {
		Class<?> type = super.getValueNonEmpty(propertyName, className);
		if (!isControlAnnotation(type)) {
			throw new ConfigurationException(I18NConstants.ERROR_NO_CONTROL_ANNOTATION__CLASS.fill(type.getName()),
				propertyName, className);
		}
		return type;
	}

	/**
	 * Whether the given type is an annotation type marked with {@link ControlAnnotation}.
	 */
	public static boolean isControlAnnotation(Class<?> type) {
		return type.isAnnotation() && type.isAnnotationPresent(ControlAnnotation.class);
	}

}
