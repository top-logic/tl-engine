/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * Describes the addition of a {@link TLAnnotation} to a {@link TLModelPart}.
 * 
 * @see SetAnnotations
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("add-annotations")
public interface AddAnnotations extends PartUpdate, AnnotatedConfig<TLAnnotation> {

	// Pure sum interface.

}
