/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

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
	 * Application logo displayed on the login and logout pages.
	 */
	public static ThemeImage APP_LOGO;

	/**
	 * Whether to hide the application's title on the login page.
	 * 
	 * <p>
	 * Enable this setting in the application's default theme, if the application logo is customized
	 * and makes the application's title redundant on the login page.
	 * </p>
	 */
	@DefaultValue("false")
	public static ThemeVar<Boolean> HIDE_APP_TITLE_ON_LOGIN_PAGE;

	public static ThemeImage V;

	@DefaultValue("true")
	public static ThemeVar<Boolean> GROUP_BORDER;

	/**
	 * General font.
	 */
	@DefaultValue("Arial,Helvetica,sans-serif")
	public static ThemeVar<String> TEXT_FAMILY;

	@DefaultValue("12")
	public static ThemeVar<Integer> FONT_SIZE_IMAGE;

	/**
	 * Filter dialog width.
	 */
	@DefaultValue("300")
	public static ThemeVar<Integer> FILTER_DIALOG_WIDTH;

	/**
	 * Filter dialog height.
	 */
	@DefaultValue("200")
	public static ThemeVar<Integer> FILTER_DIALOG_HEIGHT;

	/**
	 * Height of a header row of tables with fixed columns in pixels.
	 */
	@DefaultValue("28px")
	public static ThemeVar<DisplayDimension> FROZEN_TABLE_HEADER_ROW_HEIGHT;

	/**
	 * Height of a data row of tables with fixed columns in pixels.
	 */
	@DefaultValue("28px")
	public static ThemeVar<DisplayDimension> FROZEN_TABLE_ROW_HEIGHT;

	/**
	 * Height of footer of tables with fixed columns in pixels.
	 */
	@DefaultValue("22px")
	public static ThemeVar<DisplayDimension> FROZEN_TABLE_FOOTER_HEIGHT;

	/**
	 * Height of button bars.
	 */
	@DefaultValue("39px")
	public static ThemeVar<DisplayDimension> BUTTON_COMP_HEIGHT;

	/**
	 * Whether a separated options selection list view or an input field should be used for the
	 * select field.
	 */
	@DefaultValue("false")
	public static ThemeVar<Boolean> POPUP_TEXT_SELECT_VIEW;

	/**
	 * Icon of the command to delete a component.
	 */
	@DefaultValue("css:fas fa-trash")
	public static ThemeImage DELETE_COMPONENT_COMMAND;

	/**
	 * Icon of the command to add a new component.
	 */
	@DefaultValue("colored:tl-icon plus")
	public static ThemeImage ADD_COMPONENT_COMMAND;

	/**
	 * Icon of the command to edit the given component.
	 */
	@DefaultValue("css:fas fa-edit")
	public static ThemeImage EDIT_COMPONENT_COMMAND;

	/**
	 * Icon of the command to edit the enclosing layout of the given component.
	 */
	@DefaultValue("css:fas fa-border-all")
	public static ThemeImage EDIT_LAYOUT_COMMAND;

	/**
	 * Icon of the command to edit the form layout.
	 */
	@DefaultValue("colored:tl-icon form-border")
	public static ThemeImage CONFIGURE_FORM_DEFINITION_COMMAND;

	/**
	 * Icon of the command to delete the current form layout.
	 */
	@DefaultValue("colored:tl-icon form-border-reset")
	public static ThemeImage DELETE_FORM_DEFINITION_COMMAND;

	/**
	 * Icon of the command to add a new tab to the given tabbar.
	 */
	@DefaultValue("colored:fas fa-columns overlay-add")
	public static ThemeImage ADD_TAB_COMMAND;

	/**
	 * Icon of the command to add a dialog to the given component.
	 */
	@DefaultValue("colored:far fa-window-maximize overlay-add")
	public static ThemeImage ADD_DIALOG_COMMAND;
}
