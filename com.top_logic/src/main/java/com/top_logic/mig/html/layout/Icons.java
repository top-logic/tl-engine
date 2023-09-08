/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
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

	@DefaultValue("width=device-width, initial-scale=1, shrink-to-fit=no")
	public static ThemeVar<String> META_VIEWPORT;

	@ClassDefault(NoLayoutTransform.class)
	public static ThemeVar<LayoutTransform> LAYOUT_TRANSFORM;

}
