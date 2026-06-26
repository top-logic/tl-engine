/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link ReactSidebarControl#SELECT_ITEM_COMMAND} command: which navigation
 * item to select.
 */
public interface SelectItemArguments extends ReactCommandArguments {

	/** @see #getItemId() */
	String ITEM_ID = "itemId";

	/**
	 * The {@link NavigationItem#getId() id} of the navigation item to select (one of the sidebar
	 * items).
	 */
	@Name(ITEM_ID)
	@Mandatory
	String getItemId();

}
