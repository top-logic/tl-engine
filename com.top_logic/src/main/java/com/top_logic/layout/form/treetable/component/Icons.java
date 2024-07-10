/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.DisplayDimension;
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

	@DefaultValue("css:bi bi-trash")
	public static ThemeImage DELETE_MENU;

	@DefaultValue("css:bi bi-trash")
	public static ThemeImage DELETE_MENU_DISABLED;

	public static ThemeImage INVERT_SELECTION;

	public static ThemeImage INVERT_SELECTION_DISABLED;

	public static ThemeImage MOVE_ROW_LEFT;

	public static ThemeImage MOVE_ROW_LEFT_DISABLED;

	public static ThemeImage MOVE_ROW_RIGHT;

	public static ThemeImage MOVE_ROW_RIGHT_DISABLED;

	public static ThemeImage REMOVE_CHECKBOX;

	public static ThemeImage REMOVE_CHECKBOX_DISABLED;

	public static ThemeImage SELECT_ALL_CHECKBOXES;

	public static ThemeImage SELECT_ALL_CHECKBOXES_DISABLED;

	public static ThemeImage SELECT_SIBLINGS;

	public static ThemeImage SELECT_SIBLINGS_DISABLED;

	/**
	 * Height of the title of tree tables with fixed columns.
	 */
	@DefaultValue("22px")
	public static ThemeVar<DisplayDimension> FROZEN_TREE_TABLE_TITLE_HEIGHT;

	/**
	 * Height of the title of tables with fixed columns.
	 */
	@DefaultValue("22px")
	public static ThemeVar<DisplayDimension> FROZEN_TABLE_TITLE_HEIGHT;

}
