/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants of the captcha.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The security check was not completed or has expired. Please confirm it again.
	 */
	public static ResKey ERROR_CAPTCHA_INVALID;

	static {
		initConstants(I18NConstants.class);
	}
}
