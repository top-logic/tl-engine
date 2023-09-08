/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.border;

import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;

/**
 * Icon constants for this package.
 *
 * @see ThemeVar
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	@DefaultValue("8px")
	public static ThemeVar<DisplayDimension> BOX_HSPACE_SIZE;

}
