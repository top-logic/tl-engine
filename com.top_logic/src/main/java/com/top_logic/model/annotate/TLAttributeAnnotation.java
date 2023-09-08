/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Base interface for {@link TLAnnotation}s on {@link TLStructuredTypePart}s.
 * <p>
 * All {@link TLAttributeAnnotation}s are {@link TLTypeAnnotation}s too: If they are used on a type,
 * the annotation is the default for attributes of that type.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface TLAttributeAnnotation extends TLAnnotation {
	// Pure marker interface.
}
