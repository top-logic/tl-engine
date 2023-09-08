/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	/**
	 * Image for the button to edit the row object in a grid.
	 */
	public static ThemeImage OPEN_DETAIL;

	/**
	 * Disabled image for the button to edit the row object in a grid.
	 */
	public static ThemeImage OPEN_DETAIL_DISABLED;

	public static ThemeImage SAVE_BUTTON_ICON;

	public static ThemeImage SAVE_BUTTON_ICON_DISABLED;

	public static ThemeImage SAVE_BUTTON_MODIFIED;

	/**
	 * Grids technical column width in pixels.
	 * 
	 * <p>
	 * This column contains commands to edit the row object.
	 * </p>
	 */
	@DefaultValue("31")
	public static ThemeVar<Integer> GRID_EDIT_WIDTH;

}
