/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.xml.annotation;

import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;

/**
 * Marking a {@link MOClass} as framework internal.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SystemAnnotation extends MOAnnotation {
	// Pure marker annotation.
}
