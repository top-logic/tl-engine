/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.app;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The command "{1}" in the workspace "{0}" failed with exit code {2}: {3}
	 */
	public static ResKey4 ERROR_COMMAND_FAILED__WORKSPACE_COMMAND_CODE_OUTPUT;

	/**
	 * @en The application in the workspace "{0}" does not answer at {1} after {2} seconds.
	 */
	public static ResKey3 ERROR_APPLICATION_NOT_REACHABLE__WORKSPACE_URL_SECONDS;

	/**
	 * @en The workspace "{0}" stopped before its application answered.
	 */
	public static ResKey1 ERROR_WORKSPACE_STOPPED__WORKSPACE;

	/**
	 * @en The application in the workspace "{0}" did not start, see the end of its log.
	 */
	public static ResKey1 ERROR_APPLICATION_FAILED__WORKSPACE;

	/**
	 * @en The resource "{0}" of the workspace image is missing.
	 */
	public static ResKey1 ERROR_MISSING_IMAGE_RESOURCE__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
