/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactSidebarControl#SELECT_ITEM_COMMAND} command: which navigation
 * item to select.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans (the {@code {itemId}} reference expands to the
 * selected item).
 * </p>
 */
@Label("Navigate to '{itemId}'")
public interface SelectItemArguments extends ReactCommand {

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
