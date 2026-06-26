/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link ReactBottomBarControl#SELECT_ITEM_COMMAND} command: which
 * bottom-bar item to select.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Select item '{itemId}'")
public interface BottomBarSelectItemArguments extends ReactCommandArguments {

	/** @see #getItemId() */
	String ITEM_ID = "itemId";

	/**
	 * The id of the bottom-bar item to select.
	 */
	@Name(ITEM_ID)
	@Mandatory
	String getItemId();

}
