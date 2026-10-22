/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The secret variable "{0}" is neither defined as system property nor as environment
	 *     variable.
	 */
	public static ResKey1 ERROR_SECRET_VARIABLE_UNDEFINED__NAME;

	/**
	 * @en The secret file "{0}" cannot be read.
	 */
	public static ResKey1 ERROR_SECRET_FILE_UNREADABLE__PATH;

	/**
	 * @en The secret file "{0}" is empty.
	 */
	public static ResKey1 ERROR_SECRET_FILE_EMPTY__PATH;

	/**
	 * @en No secret has been configured.
	 */
	public static ResKey ERROR_SECRET_NOT_CONFIGURED;

	static {
		initConstants(I18NConstants.class);
	}
}
