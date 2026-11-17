/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Nothing to reload is registered under the name "{0}".
	 */
	public static ResKey1 ERROR_RELOADABLE_UNKNOWN__NAME;

	/**
	 * @en The reload was refused without giving a reason.
	 */
	public static ResKey ERROR_RELOAD_REFUSED;

	/**
	 * @en The reload failed: {0}
	 */
	public static ResKey1 ERROR_RELOAD_FAILED__MESSAGE;

	/**
	 * @en "{0}" is not a configuration variable defined in the configuration of the application.
	 */
	public static ResKey1 ERROR_NOT_A_CONFIGURED_VARIABLE__NAME;

	/**
	 * @en Values of configuration variables cannot be stored, the file "{0}" is not writable.
	 */
	public static ResKey1 ERROR_VARIABLES_NOT_WRITABLE__FILE;

	/**
	 * @en Storing the values of configuration variables in the file "{0}" failed.
	 */
	public static ResKey1 ERROR_STORING_VARIABLES_FAILED__FILE;

	static {
		initConstants(I18NConstants.class);
	}
}
