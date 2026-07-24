/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for form field chrome.
 *
 * @see ThemeImage
 */
public class Icons extends IconsBase {

	/** Icon displayed in front of a validation error message. */
	@DefaultValue("css:fas fa-exclamation-triangle")
	public static ThemeImage VALIDATION_ERROR;

	/** Icon displayed in front of a validation warning message. */
	@DefaultValue("css:fas fa-exclamation-triangle")
	public static ThemeImage VALIDATION_WARNING;

}
