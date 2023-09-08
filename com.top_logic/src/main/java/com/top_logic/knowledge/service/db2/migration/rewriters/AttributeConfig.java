/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of an attribute.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AttributeConfig extends ConfigurationItem {

	/**
	 * Name of configuration {@link #getAttribute()}.
	 */
	String ATTRIBUTE_NAME = "attribute";

	/**
	 * Name of the configured attribute.
	 */
	@Name(ATTRIBUTE_NAME)
	@Mandatory
	String getAttribute();

	/**
	 * Setter for {@link #getAttribute()}.
	 */
	void setAttribute(String value);

}
