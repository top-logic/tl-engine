/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config.annotation;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.BooleanAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLTypeAnnotation} to an {@link EnumConfig} that defines whether the enumeration type is a
 * system relevant enumeration.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("system-enum")
@TargetType(value = TLTypeKind.ENUMERATION)
public interface SystemEnum extends BooleanAnnotation, TLTypeAnnotation {

	// Pure sum/marker annotation.

}
