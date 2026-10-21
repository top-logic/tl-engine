/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.edit.annotation.ControlAnnotation;

/**
 * Option provider listing all annotation types in the application marked with
 * {@link ControlAnnotation}, sorted by name.
 *
 * @see ControlAnnotationFormat
 */
public class ControlAnnotationOptions extends Function0<List<Class<?>>> {

	@Override
	public List<Class<?>> apply() {
		return controlAnnotations();
	}

	/**
	 * All annotation types in the application marked with {@link ControlAnnotation}, sorted by
	 * name.
	 */
	public static List<Class<?>> controlAnnotations() {
		return TypeIndex.getInstance().getSpecializations(Annotation.class, false, true, true).stream()
			.filter(ControlAnnotationFormat::isControlAnnotation)
			.sorted(Comparator.comparing(Class::getName))
			.toList();
	}

}
