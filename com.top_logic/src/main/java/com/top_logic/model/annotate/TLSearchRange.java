/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that marks a type or attribute to support search ranges.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("search-range")
public interface TLSearchRange extends BooleanAnnotation, TLTypeAnnotation, TLAttributeAnnotation {

	// Pure sum/marker interface.

}
