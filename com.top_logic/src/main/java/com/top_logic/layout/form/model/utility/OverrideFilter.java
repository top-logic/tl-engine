/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.util.AnnotationFilter;

/**
 * {@link AnnotationFilter} matching types not marked
 * {@link com.top_logic.model.annotate.AnnotationInheritance.Policy#FINAL}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OverrideFilter extends AnnotationFilter {

	/**
	 * Creates a {@link OverrideFilter}.
	 */
	public OverrideFilter(AnnotationCustomizations customizations) {
		super(customizations);
	}

	@Override
	public boolean test(Class<?> option) {
		return AnnotationInheritance.Policy.getInheritancePolicy(option) != Policy.FINAL;
	}
}
