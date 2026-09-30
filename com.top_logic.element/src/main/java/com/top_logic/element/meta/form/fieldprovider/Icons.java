/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
public class Icons extends IconsBase {

	/** Image for the command to copy a row. */
	@CalledByReflection
	public static ThemeImage COPY_ROW;

	/** Disabled image for the command to copy a row. */
	@CalledByReflection
	public static ThemeImage COPY_ROW_DISABLED;

}
