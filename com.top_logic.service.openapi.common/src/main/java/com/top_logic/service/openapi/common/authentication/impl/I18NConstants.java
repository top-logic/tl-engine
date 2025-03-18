/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.impl;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Multiple user authentication for different users: {0} vs. {1}.
	 */
	public static ResKey2 ERROR_MULTIPLE_USERS__USER1_USER2;

	/**
	 * @en Multiple authentication attempts failed: {0}.
	 */
	public static ResKey1 ERROR_AUTH_FAILED__REASONS;

	/** @en Missing authentication secret. */
	public static ResKey ERROR_MISSING_AUTHENTICATION_SECRET;

	static {
		initConstants(I18NConstants.class);
	}
}
