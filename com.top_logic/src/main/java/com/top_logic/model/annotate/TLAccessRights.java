/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Annotation declaring access rights to the annotated model element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("access-rights")
public interface TLAccessRights
		extends TLModuleAnnotation, TLTypeAnnotation, TLAttributeAnnotation, AccessRightsConfig {

	// Marker interface combining module, type, and attribute annotation with access rights configuration.

}

