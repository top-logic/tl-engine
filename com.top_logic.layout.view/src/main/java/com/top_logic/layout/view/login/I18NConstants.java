/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the React view login UI.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Please enter user name and password.
	 */
	public static ResKey LOGIN_MISSING_CREDENTIALS;

	/**
	 * @en Login failed. Please check your credentials.
	 */
	public static ResKey LOGIN_FAILED;

	static {
		initConstants(I18NConstants.class);
	}
}
