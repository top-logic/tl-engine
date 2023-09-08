/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * The configuration of a meta element belonging to a structured element.
 * 
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
@DisplayOrder({
	InterfaceConfig.NAME,
	InterfaceConfig.GENERALIZATIONS,
	InterfaceConfig.ANNOTATIONS,
	InterfaceConfig.ATTRIBUTES,
})
@TagName(InterfaceConfig.TAG_NAME)
public interface InterfaceConfig extends ObjectTypeConfig {
    
	/** Default tag for defining interfaces. */
	String TAG_NAME = "interface";

}
