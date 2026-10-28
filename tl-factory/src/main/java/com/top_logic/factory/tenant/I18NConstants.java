/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.tenant;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en "{0}" is already a member of the tenant "{1}"; a user is a member of one tenant only.
	 */
	public static ResKey2 ERROR_MEMBER_OF_OTHER_TENANT__USER_TENANT;

	static {
		initConstants(I18NConstants.class);
	}
}
