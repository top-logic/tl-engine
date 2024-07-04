/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.control.AbstractButtonControl;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	@DefaultValue("css:bi bi-chevron-right")
	public static ThemeImage BUTTON_ARROW;

	public static ThemeImage BUTTON_ARROW_DISABLED;

	public static ThemeImage STACKTRACE;

	@DefaultValue("tabBar")
	public static ThemeVar<String> TABBAR_CSS_CLASS;

	/**
	 * Height of tab bars in pixels.
	 */
	@DefaultValue("36")
	public static ThemeVar<Integer> TABBAR_HEIGHT;

	/**
	 * Whether command-provided icons are rendered.
	 */
	@DefaultValue("true")
	public static ThemeVar<Boolean> BUTTONBAR_CUSTOM_ICONS;

	/**
	 * Whether to show arrow decoration, if a command does not provide a custom icon.
	 */
	@DefaultValue("true")
	public static ThemeVar<Boolean> BUTTONBAR_ARROW_DECORATION;

	/**
	 * Template for displaying a button that is part of a group of multiple buttons. For example,
	 * the one at the bottom right.
	 */
	@TemplateType(AbstractButtonControl.class)
	public static ThemeVar<HTMLTemplateFragment> BUTTON_COMPONENT_BUTTON_TEMPLATE;

}
