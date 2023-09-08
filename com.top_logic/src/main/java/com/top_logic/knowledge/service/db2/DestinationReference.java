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
 * Special {@link AssociationReference} for the built-in destination reference.
 * 
 * @see SourceReference
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(MetaObjectConfig.DESTINATION_REFERENCE_PROPERTY)
public interface DestinationReference extends AssociationReference {

	/**
	 * Destination reference name.
	 */
	String REFERENCE_DEST_NAME = "dest";

	@Override
	@StringDefault(REFERENCE_DEST_NAME)
	String getAttributeName();
}

