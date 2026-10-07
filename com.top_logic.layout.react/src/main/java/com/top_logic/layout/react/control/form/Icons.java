/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for the form field controls.
 *
 * @see ThemeImage
 */
public class Icons extends IconsBase {

	/** Icon of the button of a {@link ReactCompactFieldControl} opening the full field editor. */
	@DefaultValue("css:fa-solid fa-up-right-and-down-left-from-center")
	public static ThemeImage COMPACT_FIELD_OPEN;

}
