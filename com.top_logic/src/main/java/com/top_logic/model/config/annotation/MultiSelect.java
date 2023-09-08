/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config.annotation;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.BooleanAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLTypeAnnotation} to an {@link EnumConfig} that defines the default for the multiselect
 * property of attributes of this enumeration type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("multi-select")
@TargetType(value = TLTypeKind.ENUMERATION)
@InApp
public interface MultiSelect extends BooleanAnnotation, TLTypeAnnotation {

	// Pure sum/marker interface.

}
