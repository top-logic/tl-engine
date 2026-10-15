/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en "{0}" is no temperature. Enter a number, optionally followed by a unit: °C, °F or K.
	 */
	public static ResKey1 ERROR_INVALID_TEMPERATURE__VALUE;

	/**
	 * @en "{0}" is below absolute zero.
	 */
	public static ResKey1 ERROR_BELOW_ABSOLUTE_ZERO__VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
