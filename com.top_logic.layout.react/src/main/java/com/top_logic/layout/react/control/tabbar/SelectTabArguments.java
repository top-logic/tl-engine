/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tabbar;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link ReactTabBarControl#SELECT_TAB_COMMAND} command: which tab to
 * activate.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Activate tab '{tabId}'")
public interface SelectTabArguments extends ReactCommandArguments {

	/** @see #getTabId() */
	String TAB_ID = "tabId";

	/**
	 * The {@link TabDefinition#getId() id} of the tab to activate (one of the tabs in this tab bar).
	 */
	@Name(TAB_ID)
	@Mandatory
	String getTabId();

}
