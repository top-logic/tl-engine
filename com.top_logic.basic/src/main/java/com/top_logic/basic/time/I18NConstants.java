/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.time;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey1;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 ERROR_INVALID_TIME_FORMAT__VALUE;

	public static ResKey1 ERROR_INVALID_HOUR__VALUE;

	public static ResKey1 ERROR_INVALID_MINUTE__VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
