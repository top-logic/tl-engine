/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link TLAttributeAnnotation} that marks a model part as delete protected.
 * 
 * <p>
 * A delete protected model part cannot be deleted in a running application. This annotation is
 * useful to set on attributes that are required for some code to function properly.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("delete-protected")
public interface TLDeleteProtected extends TLAttributeAnnotation {

	// Pure marker annotation.

}
