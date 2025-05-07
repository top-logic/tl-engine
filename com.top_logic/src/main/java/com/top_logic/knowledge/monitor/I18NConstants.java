/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix FAILED_LOGIN_REASONS = legacyPrefix("admin.sys.failedLogins.reason.");

	public static ResKey DELETED_USER = legacyKey("personHistory.deleted");

	/**
	 * @en Logged in user: {0}
	 */
	public static ResKey1 LOGGED_IN_USER__USER;

	/**
	 * @en Logged out user: {0}
	 */
	public static ResKey1 LOGGED_OUT_USER__USER;

	/**
	 * @en Removed unused accounts.
	 */
	protected static ResKey REMOVED_UNUSED_ACCOUNTS;

	/**
	 * @en Recorded failed login: {0}
	 */
	public static ResKey1 RECORDED_FAILED_LOGIN__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
