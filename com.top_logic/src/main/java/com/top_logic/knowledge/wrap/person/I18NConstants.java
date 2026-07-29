/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 REFRESH_USERS_SUCCESS__DURATION;

	public static ResKey REFRESH_USERS_COMMIT_FAILED;

	public static ResKey1 INVALID_HOMEPAGE__HOMEPAGE;

	public static ResKey ERROR_NO_MORE_USERS;

	/**
	 * @en Updated account list.
	 */
	public static ResKey UPDATED_ACCOUNT_LIST;

	/**
	 * @en Stored personal configuration.
	 */
	public static ResKey STORED_PERSONAL_CONFIGURATION;

	/**
	 * @en Created root account.
	 */
	public static ResKey CREATED_ROOT_ACCOUNT;

	/**
	 * @en Created anonymous account.
	 */
	public static ResKey CREATED_ANONYMOUS_ACCOUNT;

	/**
	 * @en Resetting password of root account.
	 */
	public static ResKey RESETTING_ROOT_PASSWORD;

	/**
	 * @en An account with the name "{0}" already exists (account names are case-insensitive).
	 */
	public static ResKey1 ERROR_DUPLICATE_ACCOUNT_NAME__NAME;

	/**
	 * @en The account name "{0}" is invalid. Allowed are letters, digits and the characters . @ + ~ - _ (no whitespace).
	 */
	public static ResKey1 ERROR_INVALID_ACCOUNT_NAME__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
