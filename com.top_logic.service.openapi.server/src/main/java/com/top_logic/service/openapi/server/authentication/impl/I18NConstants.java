/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.impl;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
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

	/**
	 * @en Unable to get token.
	 */
	public static ResKey AUTH_FAILED_NO_TOKEN;

	/**
	 * @en Token declined: {0} {1}: {2}.
	 */
	public static ResKey3 AUTH_FAILED_TOKEN_DECLINED__STATUS_CODE__CODE__DESCRIPTION;

	/**
	 * @en Token inactive.
	 */
	public static ResKey AUTH_FAILED_TOKEN_INACTIVE;

	/** @en Authentication token is expired at {0}. */
	public static ResKey1 AUTH_FAILED_TOKEN_EXPIRED__DATE;

	/**
	 * @en Invalid server response when validating token: {0}
	 */
	public static ResKey1 AUTH_FAILED_ILLEGAL_TOKEN_VALIDATION_RESPONSE__MSG;

	/**
	 * @en No account for user ''{0}'' found.
	 */
	public static ResKey1 NO_SUCH_ACCOUNT__USER;

	/**
	 * @en Token introspection failed: {0}
	 */
	public static ResKey1 ERROR_TOKEN_INTROSPECTION_FAILED__MSG;

	static {
		initConstants(I18NConstants.class);
	}
}
