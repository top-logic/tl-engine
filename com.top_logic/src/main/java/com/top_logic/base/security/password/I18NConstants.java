/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.password;

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

	public static ResKey PWD_OK;

	public static ResKey PWD_USED_BEFORE;

	public static ResKey PWD_TO_SHORT;

	public static ResKey PWD_CONTENT_INVALID;

	/**
	 * @en Updated password hash for user: {0}
	 */
	public static ResKey1 UPDATED_PASSWORD_HASH__USER;

	static {
		initConstants(I18NConstants.class);
	}
}
