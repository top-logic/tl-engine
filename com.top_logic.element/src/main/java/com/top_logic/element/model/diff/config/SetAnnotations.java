/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * Describes the setting of {@link TLAnnotation}s to a {@link TLModelPart}.
 * 
 * @see AddAnnotations
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("set-annotations")
public interface SetAnnotations extends PartUpdate, AnnotatedConfig<TLAnnotation> {

	// Pure sum interface.

}
