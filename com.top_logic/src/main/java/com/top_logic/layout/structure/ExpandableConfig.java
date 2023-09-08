/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link ConfigurationItem Configuration} for an {@link Expandable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExpandableConfig extends ConfigurationItem {

	/**
	 * @see ExpandableConfig#isInitiallyMinimized()
	 */
	String INITIALLY_MINIMIZED = "initiallyMinimized";

	/**
	 * Whether the configured {@link Expandable} should be displayed minimized initially.
	 */
	@Name(INITIALLY_MINIMIZED)
	boolean isInitiallyMinimized();

	/**
	 * @see #isInitiallyMinimized()
	 */
	void setInitiallyMinimized(boolean value);

}

