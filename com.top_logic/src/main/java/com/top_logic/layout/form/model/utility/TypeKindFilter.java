/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.util.AnnotationFilter;

/**
 * {@link AnnotationFilter} selecting implementations that are compatible with a given
 * {@link TLTypeKind}.
 * 
 * @see TargetTypeFilter
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeKindFilter extends AnnotationFilter {

	private final TLTypeKind _expectedKind;

	/**
	 * Creates a {@link TypeKindFilter}.
	 */
	public TypeKindFilter(AnnotationCustomizations customizations, TLTypeKind expectedKind) {
		super(customizations);
		_expectedKind = expectedKind;
	}

	@Override
	public boolean test(Class<?> option) {
		TargetType targetAnnotation = getAnnotation(option, TargetType.class);
		if (targetAnnotation != null) {
			for (TLTypeKind kind : targetAnnotation.value()) {
				if (kind == _expectedKind) {
					// Prevent looking up specialized implementations in a context, where the
					// concrete type is not known.
					return targetAnnotation.name().length == 0;
				}
			}

			// No matching target type found.
			return false;
		}

		return true;
	}

}
