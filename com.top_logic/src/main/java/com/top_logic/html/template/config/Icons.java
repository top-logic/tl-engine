/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
<<<<<<< Upstream, based on origin/master
 */
package com.top_logic.html.template.config;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Theme resources for this package.
 */
public class Icons extends IconsBase {

	/**
	 * The icon of the pop-up button that shows the HTML template specified in a theme variable in
	 * the theme editor.
	 */
	@DefaultValue("css:fas fa-code")
	public static ThemeImage TEMPLATE_POPUP_BUTTON;

	/**
	 * Template of the contents of the popup that displays the HTML template specified in a theme
	 * variable in the theme editor.
	 * 
	 * <p>
	 * The only property available is {@value DisplayTemplateCodeCommand#CONTENT_PROPERTY} that
	 * renders the template contents.
	 * </p>
	 */
	@TemplateType(DisplayTemplateCodeCommand.class)
	public static ThemeVar<HTMLTemplateFragment> TEMPLATE_POPUP_CONTENTS;

}
=======
 */
package com.top_logic.html.template.config;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Theme resources for this package.
 */
public class Icons extends IconsBase {

	/**
	 * The icon of the pop-up button that shows the HTML template specified in a theme variable in
	 * the theme editor.
	 */
	@DefaultValue("css:fas fa-code")
	public static ThemeImage POPUP_BUTTON_TEMPLATE;

	/**
	 * Template of the contents of the popup that displays the HTML template specified in a theme
	 * variable in the theme editor.
	 * 
	 * <p>
	 * The only property available is {@value DisplayTemplateCodeCommand#CONTENT_PROPERTY} that
	 * renders the template contents.
	 * </p>
	 */
	@TemplateType(DisplayTemplateCodeCommand.class)
	public static ThemeVar<HTMLTemplateFragment> POPUP_CONTENTS_TEMPLATE;

}
>>>>>>> 57f33fc Ticket #27410: Instantiation of TEXT_INPUT_EDIT_SINGLE_TEMPLATE, TEXT_INPUT_EDIT_MULTI_TEMPLATE, TEXT_INPUT_IMMUTABLE_SINGLE_TEMPLATE, TEXT_INPUT_IMMUTABLE_MULTI_TEMPLATE, TEXT_INPUT_WITH_BUTTONS_EDIT_TEMPLATE, PASSWORD_INPUT_EDIT_TEMPLATE.
