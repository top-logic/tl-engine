/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.function.Predicate;

import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;

/**
 * Base class for option provider functions selecting compatible attribute annotations based on the
 * {@link TLTypeKind}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AttributeAnnotationOptions extends AnnotationOptionsBase {

	/**
	 * Creates a {@link AttributeAnnotationOptions}.
	 */
	public AttributeAnnotationOptions(DeclarativeFormOptions options, TLTypePart attribute,
			Predicate<Class<?>> hasValidClassifier) {
		super(options, TLTypeKind.getTLTypeKind(attribute), TLAttributeAnnotation.class, hasValidClassifier);
	}

}
