/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search.quick;

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
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	public static ThemeImage SEARCH;

	/**
	 * Template for rendering a quick search view.
	 */
	@TemplateType(QuickSearchView.class)
	public static ThemeVar<HTMLTemplateFragment> QUICK_SEARCH_VIEW;

}
