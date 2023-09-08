/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Option provider function retrieving only {@link TLTypeAnnotation} compatible with
 * {@link TLTypeKind#ENUMERATION}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EnumAnnotationOptions extends TypeAnnotationOptionsBase {

	/**
	 * Creates a {@link EnumAnnotationOptions}.
	 */
	@CalledByReflection
	public EnumAnnotationOptions(DeclarativeFormOptions options) {
		super(options, TLTypeKind.ENUMERATION);
	}

}
