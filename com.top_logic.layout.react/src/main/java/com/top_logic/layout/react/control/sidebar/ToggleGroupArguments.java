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
 * Typed arguments of the {@link ReactSidebarControl#TOGGLE_GROUP_COMMAND} command: which sidebar
 * group to expand or collapse.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Toggle group '{itemId}'")
public interface ToggleGroupArguments extends ReactCommand {

	/** @see #getItemId() */
	String ITEM_ID = "itemId";

	/** @see #isExpanded() */
	String EXPANDED = "expanded";

	/**
	 * The id of the sidebar group to toggle.
	 */
	@Name(ITEM_ID)
	@Mandatory
	String getItemId();

	/**
	 * Whether to expand the group ({@code true}) or collapse it ({@code false}).
	 */
	@Name(EXPANDED)
	boolean isExpanded();

}
