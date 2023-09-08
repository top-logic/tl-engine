/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	public static ThemeImage ADD_BOOKMARK;

	public static ThemeImage BUTTON_REFRESH;

	public static ThemeImage BUTTON_REFRESH_DISABLED;

	public static ThemeImage COLLAPSE_ALL;

	public static ThemeImage COLLAPSE_ALL_DISABLED;

	public static ThemeImage EXPAND_ALL;

	public static ThemeImage EXPAND_ALL_DISABLED;

	/**
	 * Image that indicates a rendering error.
	 */
	public static ThemeImage RENDERING_ERROR_NORMAL;

	public static ThemeImage SELECT_FILTER;

	@DefaultValue("450px")
	public static ThemeVar<DisplayDimension> DIRTY_DIALOG_WIDTH;

	@DefaultValue("300px")
	public static ThemeVar<DisplayDimension> DIRTY_DIALOG_HEIGHT;

	/**
	 * Template defining a user size adjustable control.
	 */
	@TemplateType(VerticalSizableControl.class)
	public static ThemeVar<HTMLTemplateFragment> VERTICAL_SIZABLE_CONTROL_TEMPLATE;

}
