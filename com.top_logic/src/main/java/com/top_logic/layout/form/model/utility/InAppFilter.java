/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.model.annotate.util.AnnotationFilter;

/**
 * {@link AnnotationFilter} matching {@link InApp} types.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InAppFilter extends AnnotationFilter {

	/**
	 * Creates a {@link InAppFilter}.
	 */
	public InAppFilter(AnnotationCustomizations customizations) {
		super(customizations);
	}

	@Override
	public boolean test(Class<?> option) {
		InApp inapp = getAnnotation(option, InApp.class);
		if (inapp == null || !inapp.value()) {
			return false;
		}
		return true;
	}

}
