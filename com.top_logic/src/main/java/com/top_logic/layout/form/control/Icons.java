/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Color;

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

	public static ThemeImage ACCUMULATED_TRISTATE_FALSE;

	public static ThemeImage ACCUMULATED_TRISTATE_FALSE_DISABLED;

	public static ThemeImage ACCUMULATED_TRISTATE_NULL;

	public static ThemeImage ACCUMULATED_TRISTATE_NULL_DISABLED;

	public static ThemeImage ACCUMULATED_TRISTATE_TRUE;

	public static ThemeImage ACCUMULATED_TRISTATE_TRUE_DISABLED;

	/**
	 * Image for an error message.
	 */
	public static ThemeImage ALERT;

	/**
	 * Placeholder image for an error message that is currently not displayed.
	 */
	public static ThemeImage ALERT_SPACER;

	public static ThemeImage ARROW_LEFT;

	public static ThemeImage ARROW_RIGHT;

	public static ThemeImage ARROW_UP;

	public static ThemeImage BOX_COLLAPSED;

	public static ThemeImage BOX_EXPANDED;

	public static ThemeImage BUTTON_MISSING;

	public static ThemeImage BUTTON_MISSING_DISABLED;

	/**
	 * Image that displays a calendar.
	 */
	public static ThemeImage CALENDAR;

	public static ThemeImage CIRCLE;

	/**
	 * Image that displays a clock.
	 */
	public static ThemeImage CLOCK;

	public static ThemeImage COLORLINE;

	public static ThemeImage FAST_BACKWARD;

	public static ThemeImage FAST_FORWARD;

	public static ThemeImage FORM_EDITOR__ADD;

	public static ThemeImage FORM_EDITOR__BINARY;

	public static ThemeImage FORM_EDITOR__BOOLEAN;

	public static ThemeImage FORM_EDITOR__COLUMNS;

	public static ThemeImage FORM_EDITOR__CUSTOM;

	public static ThemeImage FORM_EDITOR__DATE;

	public static ThemeImage FORM_EDITOR__EMPTY_CELL;

	public static ThemeImage FORM_EDITOR__FLOAT;

	public static ThemeImage FORM_EDITOR__FRAME;

	public static ThemeImage FORM_EDITOR__GROUP;

	public static ThemeImage FORM_EDITOR__INT;

	public static ThemeImage FORM_EDITOR__LIST;

	public static ThemeImage FORM_EDITOR__NO_ICON;

	public static ThemeImage FORM_EDITOR__REFERENCE;

	public static ThemeImage FORM_EDITOR__SEPARATOR;

	public static ThemeImage FORM_EDITOR__TABLE;

	public static ThemeImage FORM_EDITOR__TITLE;

	public static ThemeImage FORM_EDITOR__STRING;

	public static ThemeImage FORM_EDITOR__STRING_MULTILINE;

	public static ThemeImage FORM_EDITOR__TRISTATE;

	public static ThemeImage ICON_CHOOSER__EXPERT_MODE_BUTTON;

	public static ThemeImage ICON_CHOOSER__OK_BUTTON;

	public static ThemeImage ICON_CHOOSER__RESET_BUTTON;

	public static ThemeImage ICON_CHOOSER__SEARCH;

	public static ThemeImage ICON_CHOOSER__SEARCH_MODE_BUTTON;

	public static ThemeImage LINE;

	public static ThemeImage MANDATORY_FALSE;

	public static ThemeImage MANDATORY_FALSE_DISABLED;

	public static ThemeImage MANDATORY_NULL;

	public static ThemeImage MANDATORY_NULL_DISABLED;

	public static ThemeImage MANDATORY_TRUE;

	public static ThemeImage MANDATORY_TRUE_DISABLED;

	public static ThemeImage OPEN_TEXT_EDITOR;

	public static ThemeImage OPEN_TEXT_EDITOR_DISABLED;

	public static ThemeImage PDF_MISSING;

	public static ThemeImage RESET_COLORS;

	public static ThemeImage TODAY;

	public static ThemeImage TRANSPARENT_BACKGROUND;

	public static ThemeImage TRISTATE_FALSE;

	public static ThemeImage TRISTATE_FALSE_DISABLED;

	public static ThemeImage TRISTATE_NULL;

	public static ThemeImage TRISTATE_NULL_DISABLED;

	public static ThemeImage TRISTATE_TRUE;

	public static ThemeImage TRISTATE_TRUE_DISABLED;

	public static ThemeImage UPLOAD_ICON;

	public static ThemeImage URL_LINK;

	public static ThemeImage WARN;

	public static ThemeImage EDIT_MAXIMIZE;

	public static ThemeImage EDIT_MINIMIZE;

	public static ThemeImage EMPTY_ICON;

	public static ThemeImage CHECKLIST_ALL;

	public static ThemeImage CHECKLIST_NONE;

	/**
	 * Color for an entered weak password.
	 */
	@DefaultValue("#FF0000")
	public static ThemeVar<Color> PASSWORD_BAR_WEAK_COLOR;

	/**
	 * Color for an entered password.
	 */
	@DefaultValue("#FF6600")
	public static ThemeVar<Color> PASSWORD_BAR_NORMAL_COLOR;

	/**
	 * Color for an entered medium password.
	 */
	@DefaultValue("#FFD400")
	public static ThemeVar<Color> PASSWORD_BAR_MEDIUM_COLOR;

	/**
	 * Color for an entered strong password.
	 */
	@DefaultValue("#33CC00")
	public static ThemeVar<Color> PASSWORD_BAR_STRONG_COLOR;

	/**
	 * Color for an entered very strong password.
	 */
	@DefaultValue("#33FF00")
	public static ThemeVar<Color> PASSWORD_BAR_VERY_STRONG_COLOR;

	@DefaultValue("16")
	public static ThemeVar<Integer> TAB_COMPONENT_DEFAULT_TAB_HEIGHT;

	@DefaultValue("3")
	public static ThemeVar<Integer> MEGA_MENU_BORDER_WIDTH;

	/**
	 * Template for rendering an option inside a mega menu.
	 */
	@TemplateType(MegaMenuOptionControl.class)
	public static ThemeVar<HTMLTemplateFragment> MEGA_MENU_OPTION_TEMPLATE;

	/**
	 * The template to render text input fields in edit mode.
	 */
	@TemplateType(TextInputControl.class)
	public static ThemeVar<HTMLTemplateFragment> TEXT_INPUT_EDIT_SINGLE_TEMPLATE;

	/**
	 * The template to render multi-line text boxes in edit mode.
	 */
	@TemplateType(TextInputControl.class)
	public static ThemeVar<HTMLTemplateFragment> TEXT_INPUT_EDIT_MULTI_TEMPLATE;

	/**
	 * The template to render text attributes in view mode.
	 */
	@TemplateType(TextInputControl.class)
	public static ThemeVar<HTMLTemplateFragment> TEXT_INPUT_IMMUTABLE_SINGLE_TEMPLATE;

	/**
	 * The template to render multi-line text boxes in view mode.
	 */
	@TemplateType(TextInputControl.class)
	public static ThemeVar<HTMLTemplateFragment> TEXT_INPUT_IMMUTABLE_MULTI_TEMPLATE;

	/**
	 * The template to render values that consist of a text input and one or more appended buttons.
	 * 
	 * <p>
	 * Available properties are all of TEXT_INPUT_EDIT_SINGLE_TEMPLATE with an additional
	 * {@value TextInputControl#BUTTONS_PROPERTY} property that renders the additional buttons.
	 * </p>
	 * 
	 * @see #TEXT_INPUT_EDIT_SINGLE_TEMPLATE
	 * @see DateInputControl
	 * @see TimeInputControl
	 * @see ExpandableTextInputControl
	 */
	@TemplateType(TextInputControl.class)
	public static ThemeVar<HTMLTemplateFragment> TEXT_INPUT_WITH_BUTTONS_EDIT_TEMPLATE;

	/**
	 * The template to render values that consist of a textarea input and one or more appended
	 * buttons.
	 * 
	 * <p>
	 * Available properties are all of TEXT_INPUT_EDIT_MULTI_TEMPLATE with an additional
	 * {@value TextInputControl#BUTTONS_PROPERTY} property that renders the additional buttons.
	 * </p>
	 * 
	 * @see #TEXT_INPUT_EDIT_MULTI_TEMPLATE
	 * @see ExpandableTextInputControl
	 */
	@TemplateType(TextInputControl.class)
	public static ThemeVar<HTMLTemplateFragment> TEXT_INPUT_WITH_BUTTONS_EDIT_MULTI_TEMPLATE;

	/**
	 * The template to render password input fields in edit mode.
	 */
	@TemplateType(PasswordInputControl.class)
	public static ThemeVar<HTMLTemplateFragment> PASSWORD_INPUT_EDIT_TEMPLATE;

}