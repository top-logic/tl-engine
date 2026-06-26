/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link ReactTreeControl} context-menu-action command: which context-menu
 * item was selected.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Invoke context-menu item '{itemId}'")
public interface ContextMenuActionArguments extends ReactCommandArguments {

	/** @see #getItemId() */
	String ITEM_ID = "itemId";

	/**
	 * The id of the selected context-menu item.
	 */
	@Name(ITEM_ID)
	@Mandatory
	String getItemId();

}
