/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Invalid input "{0}": expected a channel name, optionally followed by "as" and the name of
	 *     the argument.
	 */
	public static ResKey1 ERROR_INVALID_INPUT__VALUE;

	/**
	 * @en Unexpected element "{0}" in a list of inputs, expected "{1}" {2}.
	 */
	public static ResKey3 ERROR_UNEXPECTED_INPUT_ELEMENT__NAME_EXPECTED_LOCATION;

	/**
	 * @en An input requires the attribute "{0}" naming its channel {1}.
	 */
	public static ResKey2 ERROR_MISSING_INPUT_CHANNEL__ATTRIBUTE_LOCATION;

	static {
		initConstants(I18NConstants.class);
	}
}
