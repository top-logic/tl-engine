/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;

/**
 * Icon constants for this package.
 */
public class Icons extends IconsBase {

	/**
	 * Application icon that is announced to the browser with the link relation <code>icon</code>.
	 *
	 * <p>
	 * This is the icon that the browser displays in the tab of the application and in its
	 * bookmarks. The value is a path relative to the context path of the web application. To use a
	 * different icon, either deliver an own file at this location, or set this variable to another
	 * location.
	 * </p>
	 */
	@DefaultValue("/images/favicon.png")
	public static ThemeVar<String> DEFAULT_ICON;

}
