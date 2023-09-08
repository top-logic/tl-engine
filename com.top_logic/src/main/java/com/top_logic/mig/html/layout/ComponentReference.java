/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Reference to a {@link LayoutComponent} within a component tree identified by its name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ComponentReference extends ConfigurationItem {

	/** @see #getName() */
	String NAME = "name";

	/**
	 * The {@link ComponentName} of the referenced {@link LayoutComponent}.
	 */
	@Name(NAME)
	@Mandatory
	ComponentName getName();

	/**
	 * Setter for {@link #getName()}.
	 */
	void setName(ComponentName name);

}
