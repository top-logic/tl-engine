/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.annotation.Name;

/**
 * Full qualified name of a model part.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface FullQualifiedName {

	/**
	 * @see #getFullQualifiedName()
	 */
	String FULL_QUALIFIED_NAME = "fullQualifiedName";

	/**
	 * Fully qualified name of the given model object.
	 */
	@Name(FULL_QUALIFIED_NAME)
	String getFullQualifiedName();

	/**
	 * @see #getFullQualifiedName()
	 */
	void setFullQualifiedName(String name);

}
