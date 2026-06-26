/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.model.search.react} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Result
	 */
	public static ResKey SCRIPT_RESULT_COLUMN;

	/**
	 * @en The script could not be parsed: {0}
	 */
	public static ResKey1 ERROR_SCRIPT_PARSE__MSG;

	/**
	 * @en The script could not be evaluated: {0}
	 */
	public static ResKey1 ERROR_SCRIPT_EVALUATION__MSG;

	static {
		initConstants(I18NConstants.class);
	}
}
