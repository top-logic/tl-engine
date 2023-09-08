/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

import com.top_logic.basic.config.customization.AnnotationCustomizations;

/**
 * {@link Predicate} of a {@link Class} based on {@link AnnotationCustomizations}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AnnotationFilter implements Predicate<Class<?>> {

	private final AnnotationCustomizations _customizations;

	/**
	 * Creates a {@link AnnotationFilter}.
	 */
	public AnnotationFilter(AnnotationCustomizations customizations) {
		_customizations = customizations;
	}

	/**
	 * The annotation of the requested type of the given option.
	 */
	protected <A extends Annotation> A getAnnotation(Class<?> option, Class<A> annotation) {
		return _customizations.getAnnotation(option, annotation);
	}

}
