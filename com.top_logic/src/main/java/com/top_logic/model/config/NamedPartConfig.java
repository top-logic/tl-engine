/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Named configuration for a model part.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface NamedPartConfig {

	/**
	 * Property name of {@link #getName()}.
	 */
	String NAME = "name";

	/**
	 * Returns the name.
	 */
	@Name(NAME)
	@Mandatory
	public String getName();

	/** @see #getName() */
	void setName(String aName);

}
