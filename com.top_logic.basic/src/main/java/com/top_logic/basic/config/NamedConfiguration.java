/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link ConfigurationItem} for an object that has a name.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface NamedConfiguration extends ConfigurationItem {

	/** Configuration name of the {@link #getName()} property. */
	String NAME_ATTRIBUTE = "name";

	/**
	 * Name of the configured object.
	 */
	@Name(NAME_ATTRIBUTE)
	String getName();

	/**
	 * Setter for {@link #getName()}.
	 */
	void setName(String name);

}

