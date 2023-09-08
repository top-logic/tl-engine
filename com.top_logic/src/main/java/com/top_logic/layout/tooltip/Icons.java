/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tooltip;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
public class Icons extends IconsBase {

	/**
	 * Template for rendering a tool-tip that has a caption.
	 */
	@TemplateType(ToolTip.class)
	public static ThemeVar<HTMLTemplateFragment> TOOLTIP_WITH_CAPTION;

	/**
	 * Template for rendering a simple tool-tip without a caption.
	 */
	@TemplateType(ToolTip.class)
	public static ThemeVar<HTMLTemplateFragment> TOOLTIP_WITHOUT_CAPTION;

}