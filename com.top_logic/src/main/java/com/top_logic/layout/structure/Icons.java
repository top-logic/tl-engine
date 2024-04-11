/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
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

	public static ThemeImage HELP;

	public static ThemeImage CLOSE_DIALOG;

	public static ThemeImage WINDOW_MAXIMIZE;

	public static ThemeImage WINDOW_NORMALIZE;

	public static ThemeImage WINDOW_NORMALIZE_AFTER_MAXIMIZED;

	/**
	 * Image to minimize the window size.
	 */
	public static ThemeImage WINDOW_MINIMIZE;

	/**
	 * Image to display a view in a separate window.
	 */
	public static ThemeImage WINDOW_POP_OUT;

	@DefaultValue("22")
	public static ThemeVar<Integer> WINDOWLAYOUT_BAR_HEIGHT;

	@DefaultValue("12")
	public static ThemeVar<Integer> WINDOWLAYOUT_LEFT_SPACER_WIDTH;

	/**
	 * Default {@link LayoutControl} provider for tabbars.
	 */
	public static ThemeVar<LayoutControlProvider> TAB_COMPONENT_DEFAULT_CONTROL_PROVIDER;

	public static ThemeImage BUTTON_INSPECTOR;

	/**
	 * Vertical border width of dialogs.
	 */
	@DefaultValue("5px")
	public static ThemeVar<DisplayDimension> DIALOG_VERTICAL_BORDER_WIDTH;

	/**
	 * Bottom border height of dialogs.
	 */
	@DefaultValue("5px")
	public static ThemeVar<DisplayDimension> DIALOG_BOTTOM_BORDER_HEIGHT;

	/**
	 * Height of dialog titles.
	 */
	@DefaultValue("24px")
	public static ThemeVar<DisplayDimension> DIALOG_TITLE_HEIGHT;

	@DefaultValue("22px")
	public static ThemeVar<DisplayDimension> COLLAPSIBLE_IMAGE_HEIGHT;

	@DefaultValue("22px")
	public static ThemeVar<DisplayDimension> COLLAPSIBLE_IMAGE_WIDTH;

	public static ThemeVar<HTMLTemplateFragment> LOGO_VIEW;

}
