/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Updated a workspace.
	 */
	public static ResKey COMMIT_WORKSPACE;

	/**
	 * @en The project "{0}" already has a workspace.
	 */
	public static ResKey1 ERROR_HAS_WORKSPACE__PROJECT;

	/**
	 * @en The project "{0}" has no workspace.
	 */
	public static ResKey1 ERROR_NO_WORKSPACE__PROJECT;

	/**
	 * @en An operation is already running on the workspace of the project "{0}".
	 */
	public static ResKey1 ERROR_WORKSPACE_BUSY__PROJECT;

	/**
	 * @en The workspace of the project "{0}" is in state "{1}", in which this is not possible.
	 */
	public static ResKey2 ERROR_INVALID_STATE__PROJECT_STATE;

	/**
	 * @en "{0}" is not a valid group ID: expected names of letters, digits and "_", separated by
	 *     dots, e.g. "com.example".
	 */
	public static ResKey1 ERROR_INVALID_GROUP_ID__VALUE;

	/**
	 * @en "{0}" is not a valid artifact ID: expected lower case letters, digits and "-", starting
	 *     with a letter, e.g. "my-app".
	 */
	public static ResKey1 ERROR_INVALID_ARTIFACT_ID__VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
