/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout.views;


import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.DisplayDimension;
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
	 * Height of the logo in the right side of modern themes the main tabbar in pixels.
	 */
	@DefaultValue("55px")
	public static ThemeVar<DisplayDimension> HEADER_RIGHT_IMAGE_HEIGHT;

	/**
	 * Width of the logo in the right side of modern themes the main tabbar in pixels.
	 */
	@DefaultValue("150px")
	public static ThemeVar<DisplayDimension> HEADER_RIGHT_IMAGE_WIDTH;

}
