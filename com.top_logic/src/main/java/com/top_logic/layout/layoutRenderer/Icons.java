/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.structure.CollapsibleControl;
import com.top_logic.layout.structure.DialogWindowControl;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	/**
	 * Pop up title height.
	 */
	@DefaultValue("24px")
	public static ThemeVar<DisplayDimension> POPUP_TITLE_HEIGHT;

	/**
	 * CSS class to set at the top-level element of the main window.
	 */
	public static ThemeVar<String> MAINLAYOUT_CSS_CLASS;

	/**
	 * Sets the collapsed size of a vertical adjustment grabber.
	 */
	@DefaultValue("1")
	public static ThemeVar<Integer> LAYOUT_ADJUSTMENT_GRABBER_COLLAPSED_SIZE;

	/**
	 * Sets the collapsed size of a vertical adjustment grabber.
	 */
	@DefaultValue("5")
	public static ThemeVar<Integer> LAYOUT_ADJUSTMENT_GRABBER_EXPANDED_SIZE;

	/**
	 * Template for rendering a component with a collapse/expand button to temporarily hide a region
	 * of the application.
	 */
	@TemplateType(CollapsibleControl.class)
	public static ThemeVar<HTMLTemplateFragment> COLLAPSIBLE_TEMPLATE;

	/**
	 * Template for rendering a dialog window.
	 */
	@TemplateType(DialogWindowControl.class)
	public static ThemeVar<HTMLTemplateFragment> DIALOG_WINDOW_TEMPLATE;

}
