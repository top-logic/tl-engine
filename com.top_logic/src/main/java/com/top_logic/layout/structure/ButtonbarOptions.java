/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * Option to add a button bar to a component.
 */
@Abstract
public interface ButtonbarOptions extends ConfigurationItem {

	/**
	 * @see #hasButtonbar()
	 */
	String BUTTONBAR = "buttonbar";

	/**
	 * Whether to automatically allocate a button bar at the bottom border of this component.
	 * 
	 * <p>
	 * An allocated button bar is used for displaying commands of this component and all nested
	 * components that have no own button bar.
	 * </p>
	 */
	@Name(BUTTONBAR)
	boolean hasButtonbar();

}
