/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.ReferenceAttributeConfig;

/**
 * {@link ReferenceAttributeConfig} which has {@link KIReference} as default implementation class.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(MetaObjectConfig.REFERENCE_ELEMENT_PROPERTY)
public interface KIReferenceConfig extends ReferenceAttributeConfig {

	@Override
	@ClassDefault(KIReference.class)
	Class<? extends MOAttribute> getImplementationClass();

}

