/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en There is no workspace "{0}".
	 */
	public static ResKey1 ERROR_UNKNOWN_WORKSPACE__ID;

	/**
	 * @en The workspace "{0}" already exists.
	 */
	public static ResKey1 ERROR_WORKSPACE_EXISTS__ID;

	/**
	 * @en The workspace "{0}" is not running.
	 */
	public static ResKey1 ERROR_WORKSPACE_NOT_RUNNING__ID;

	/**
	 * @en The command "{1}" in the workspace "{0}" did not finish within {2} seconds and was terminated.
	 */
	public static ResKey3 ERROR_COMMAND_TIMEOUT__WORKSPACE_COMMAND_SECONDS;

	static {
		initConstants(I18NConstants.class);
	}
}
