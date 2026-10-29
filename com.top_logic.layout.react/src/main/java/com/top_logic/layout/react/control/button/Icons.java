/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for dialog buttons and floating actions.
 *
 * @see MessageButtons
 * @see ReactFloatingActionControl
 */
public class Icons extends IconsBase {

	/**
	 * Save button image (floppy disk).
	 */
	@DefaultValue("css:fa-solid fa-floppy-disk")
	public static ThemeImage BUTTON_SAVE;

	/**
	 * Discard button image (trash).
	 */
	@DefaultValue("css:fa-solid fa-trash")
	public static ThemeImage BUTTON_DISCARD;

	/**
	 * Image of the button opening the speed dial of several floating actions (plus).
	 */
	@DefaultValue("css:fa-solid fa-plus")
	public static ThemeImage FLOATING_ACTION_DIAL;

}
