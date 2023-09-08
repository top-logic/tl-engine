/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	/**
	 * Template for rendering a tab bar.
	 */
	@TemplateType(TabBarControl.class)
	public static ThemeVar<HTMLTemplateFragment> TAB_BAR_TEMPLATE;

	/**
	 * Template for rendering a tab.
	 */
	@TemplateType(TabContent.class)
	public static ThemeVar<HTMLTemplateFragment> TAB_TEMPLATE;

	public static ThemeImage TAB_BAR_COMMANDS_MENU;

	@DefaultValue("css:fas fa-caret-left")
	public static ThemeImage TAB_LEFT_SCROLL_BUTTON;

	@DefaultValue("css:fas fa-caret-left")
	public static ThemeImage TAB_LEFT_SCROLL_BUTTON_HOVER;

	@DefaultValue("css:fas fa-caret-right")
	public static ThemeImage TAB_RIGHT_SCROLL_BUTTON;

	@DefaultValue("css:fas fa-caret-right")
	public static ThemeImage TAB_RIGHT_SCROLL_BUTTON_HOVER;

}
