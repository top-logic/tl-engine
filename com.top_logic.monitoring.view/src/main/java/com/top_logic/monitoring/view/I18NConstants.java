/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.view;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.monitoring.view} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The range contains more than {0} periods. Choose a longer period or a shorter range.
	 */
	public static ResKey1 ERROR_TOO_MANY_PERIODS__MAX;

	/**
	 * @en Unknown period "{0}". Valid periods are hour, day, week, month and year.
	 */
	public static ResKey1 ERROR_UNKNOWN_PERIOD__PERIOD;

	static {
		initConstants(I18NConstants.class);
	}
}
