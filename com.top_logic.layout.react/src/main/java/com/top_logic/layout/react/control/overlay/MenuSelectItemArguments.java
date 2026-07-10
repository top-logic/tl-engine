/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactMenuControl#SELECT_ITEM_COMMAND} command: which menu item to
 * select.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Select item '{itemId}'")
public interface MenuSelectItemArguments extends ReactCommand {

	/** @see #getItemId() */
	String ITEM_ID = "itemId";

	/**
	 * The id of the menu item to select.
	 */
	@Name(ITEM_ID)
	@Mandatory
	String getItemId();

}
