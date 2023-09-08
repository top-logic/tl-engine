/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of some resource.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ResourceDeclaration extends ConfigurationItem {

	/**
	 * Name of the "resource" property.
	 */
	String RESOURCE_ATTRIBUTE = "resource";

	/**
	 * The configured resource.
	 */
	@Name(RESOURCE_ATTRIBUTE)
	String getResource();

	/**
	 * Sets the value of {@link #getResource()}.
	 */
	void setResource(String newResource);

}

