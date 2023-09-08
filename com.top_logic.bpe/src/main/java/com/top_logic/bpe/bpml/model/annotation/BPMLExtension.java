/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model.annotation;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * Annotation for a {@link TLStructuredTypePart} marking it as a BPML extension (not part of the
 * BPML core model).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("bpml-extension")
public interface BPMLExtension extends TLAttributeAnnotation {

	// Pure marker annotation.

}
