/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import java.awt.Color;

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

	@DefaultValue("#CCCCCC")
	public static ThemeVar<Color> GRAD4;

	@DefaultValue("#FFFFFF")
	public static ThemeVar<Color> GRAD3;

	@DefaultValue("#FF9900")
	public static ThemeVar<Color> GRAD2;

	@DefaultValue("#D00000")
	public static ThemeVar<Color> GRAD1;

	@DefaultValue("#000066")
	public static ThemeVar<Color> GRAD0;

}
