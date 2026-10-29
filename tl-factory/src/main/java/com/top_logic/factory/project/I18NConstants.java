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
	 * @en You may not change "{0}".
	 */
	public static ResKey1 ERROR_NO_WRITE_ACCESS__OBJECT;

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

	/**
	 * @en "{0}" is not a valid artifact ID: the part "{1}" starts with a digit. Each part between
	 *     dashes becomes a name of the Java package of the application and has to start with a
	 *     letter, e.g. "my-app2" instead of "my-2app".
	 */
	public static ResKey2 ERROR_ARTIFACT_ID_PART_STARTS_WITH_DIGIT__VALUE_PART;

	/**
	 * @en "{0}" is not a valid artifact ID: it has an empty part between dashes or ends with a dash.
	 *     Each part between dashes becomes a name of the Java package of the application.
	 */
	public static ResKey1 ERROR_ARTIFACT_ID_EMPTY_PART__VALUE;

	/**
	 * @en "{0}" is not a valid artifact ID: it is a segment of the context path of the application,
	 *     and "view", "servlet" and "admin" are reserved there.
	 */
	public static ResKey1 ERROR_ARTIFACT_ID_RESERVED__VALUE;

	/**
	 * @en "{0}" cannot be used: its part "{1}" is a reserved word of Java and cannot be a name of
	 *     the Java package of the application.
	 */
	public static ResKey2 ERROR_JAVA_KEYWORD__VALUE_PART;

	static {
		initConstants(I18NConstants.class);
	}
}
