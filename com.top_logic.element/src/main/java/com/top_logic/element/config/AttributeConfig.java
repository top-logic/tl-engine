/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.model.TLProperty;

/**
 * Configuration options for {@link TLProperty} model elements.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(ElementSchemaConstants.PROPERTY_ELEMENT)
public interface AttributeConfig extends PartConfig, TypedPartAspect {

	// Pure sum interface.

}
