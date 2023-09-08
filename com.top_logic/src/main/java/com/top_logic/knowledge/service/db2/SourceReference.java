/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.dob.schema.config.MetaObjectConfig;

/**
 * Special {@link AssociationReference} for the built-in source reference.
 * 
 * @see DestinationReference
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(MetaObjectConfig.SOURCE_REFERENCE_PROPERTY)
public interface SourceReference extends AssociationReference {

	/**
	 * Name of the source reference.
	 */
	String REFERENCE_SOURCE_NAME = "source";

	@Override
	@StringDefault(REFERENCE_SOURCE_NAME)
	String getAttributeName();

}

