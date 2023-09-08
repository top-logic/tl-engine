/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import java.util.function.Predicate;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.customization.AnnotationCustomizations;

/**
 * {@link Predicate} of a {@link Class} excluding {@link Hidden} ones.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HiddenFilter extends AnnotationFilter {

	/**
	 * Creates a {@link HiddenFilter}.
	 */
	public HiddenFilter(AnnotationCustomizations customizations) {
		super(customizations);
	}

	@Override
	public boolean test(Class<?> option) {
		Hidden hidden = getAnnotation(option, Hidden.class);
		return hidden == null || !hidden.value();
	}

}
