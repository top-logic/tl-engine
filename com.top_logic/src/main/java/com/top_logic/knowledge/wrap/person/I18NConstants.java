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
	 * @en Stored personal configuration for user "{0}".
	 */
	public static ResKey STORED_PERSONAL_CONFIGURATION__USER;

	/**
	 * @en Created root account.
	 */
	public static ResKey CREATED_ROOT_ACCOUNT;

	/**
	 * @en Resetting password of root account.
	 */
	public static ResKey RESETTING_ROOT_PASSWORD;

	static {
		initConstants(I18NConstants.class);
	}
}
