/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
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
	 * @en Wrong credentials for user "{0}".
	 */
	public static ResKey1 AUTH_FAILED_WRONG_AUTHENTICATION_DATA__USER;

	/**
	 * @en Not a BasicAuth authentication.
	 */
	public static ResKey AUTH_FAILED_NO_BASIC_AUTH;

	static {
		initConstants(I18NConstants.class);
	}
}
