/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLTypeAnnotation} that marks a primitive type supporting attributes with multiple values.
 */
@TargetType(value = { TLTypeKind.BINARY, TLTypeKind.BOOLEAN, TLTypeKind.CUSTOM, TLTypeKind.DATE, TLTypeKind.FLOAT,
	TLTypeKind.INT, TLTypeKind.STRING })
@TagName("supports-multiple")
public interface TLSupportsMultiple extends TLTypeAnnotation {

	/**
	 * Whether the annotated type supports the declaration of attributes with multiple values.
	 */
	@BooleanDefault(true)
	boolean getValue();

}
