/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Base class for option provider functions selecting compatible type annotations based on the
 * {@link TLTypeKind}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TypeAnnotationOptionsBase extends AnnotationOptionsBase {

	/**
	 * Creates a {@link TypeAnnotationOptionsBase}.
	 */
	public TypeAnnotationOptionsBase(DeclarativeFormOptions options, TLTypeKind targetType) {
		super(options, targetType, TLTypeAnnotation.class, x -> true);
	}

}
