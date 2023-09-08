/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Adds metadata to a single tag that is used by the Operation Object. It is not mandatory to have a
 * {@link TagObject} per tag defined in the {@link OperationObject} instances.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	TagObject.NAME_ATTRIBUTE,
	TagObject.DESCRIPTION,
})
@Label("Tag")
public interface TagObject extends NamedConfigMandatory, Described {

	/**
	 * The name of the tag.
	 * 
	 * @see com.top_logic.basic.config.NamedConfigMandatory#getName()
	 */
	@Override
	String getName();

	/**
	 * A short description for the tag.
	 * 
	 * <p>
	 * <i>CommonMark</i> syntax may be used for rich text representation.
	 * </p>
	 */
	@Override
	String getDescription();

}

