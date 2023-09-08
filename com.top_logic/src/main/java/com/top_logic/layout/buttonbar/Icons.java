/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.buttonbar;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImage.Img;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
public class Icons extends IconsBase {

	/** Image to display when the command is a long running command. */
	public static ThemeVar<Img> SLIDER_IMG;

	/**
	 * Template for rendering a button bar.
	 */
	@TemplateType(ButtonBarControl.class)
	public static ThemeVar<HTMLTemplateFragment> BUTTON_BAR_TEMPLATE;

}
