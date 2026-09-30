/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Missing authentication data.
	 */
	public static ResKey AUTH_FAILED_MISSING_AUTHENTICATION_DATA;

	/**
	 * @en Invalid user or password.
	 */
	public static ResKey AUTH_FAILED_WRONG_AUTHENTICATION_DATA;

	/**
	 * @en Invalid authentication data for scheme basic.
	 */
	public static ResKey AUTH_FAILED_NO_BASIC_AUTH;

	static {
		initConstants(I18NConstants.class);
	}
}
