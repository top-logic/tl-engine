/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for the common display controls.
 *
 * @see ThemeImage
 */
public class Icons extends IconsBase {

	/** Icon of an {@link ReactAlertControl alert} giving an information. */
	@DefaultValue("css:fa-solid fa-circle-info")
	public static ThemeImage ALERT_INFO;

	/** Icon of an {@link ReactAlertControl alert} reporting a success. */
	@DefaultValue("css:fa-solid fa-circle-check")
	public static ThemeImage ALERT_SUCCESS;

	/** Icon of an {@link ReactAlertControl alert} giving a warning. */
	@DefaultValue("css:fa-solid fa-triangle-exclamation")
	public static ThemeImage ALERT_WARNING;

	/** Icon of an {@link ReactAlertControl alert} reporting an error. */
	@DefaultValue("css:fa-solid fa-circle-exclamation")
	public static ThemeImage ALERT_ERROR;

}
