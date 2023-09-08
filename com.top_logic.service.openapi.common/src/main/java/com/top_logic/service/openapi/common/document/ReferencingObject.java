/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Item that references a {@link ReferencableObject} by a reference name.
 * 
 * @see ReferencableObject
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReferencingObject extends ConfigurationItem {

	/** Configuration name for {@link #getReference()} */
	String $REF = "$ref";

	/**
	 * The reference name of {@link ReferencableObject referenced object}.
	 */
	@Name($REF)
	@Nullable
	@Mandatory
	String getReference();

	/**
	 * Setter for {@link #getReference()}.
	 */
	void setReference(String value);

}

