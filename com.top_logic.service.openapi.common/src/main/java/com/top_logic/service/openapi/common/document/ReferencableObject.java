/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Item that can be referenced by the {@link ReferencableObject#getReferenceName()}.
 * 
 * @see ReferencingObject
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReferencableObject {

	/** @see #getReferenceName() */
	String REFERENCE_NAME = "reference-name";

	/**
	 * The name which can be used to reference this {@link ParameterObject}.
	 * 
	 * @see ReferencingParameterObject
	 */
	@Name(REFERENCE_NAME)
	@Nullable
	@Mandatory
	String getReferenceName();

	/**
	 * Setter for {@link #getReferenceName()}.
	 */
	void setReferenceName(String value);

}

