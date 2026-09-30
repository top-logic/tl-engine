/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en You can proceed via this path, but its condition is not fully satisfied.
	 */
	public static ResKey DEFAULT_MESSAGE_WARNING;

	/**
	 * @en You cannot proceed via this path due to an unmet condition.
	 */
	public static ResKey DEFAULT_MESSAGE_ERROR;

	static {
		initConstants(I18NConstants.class);
	}
}
