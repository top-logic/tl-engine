/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * Annotation to a {@link TypeConfig}.
 * 
 * @see TypeConfig#getAnnotations()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface TLTypeAnnotation extends TLAnnotation {
	// Pure base interface.
}
