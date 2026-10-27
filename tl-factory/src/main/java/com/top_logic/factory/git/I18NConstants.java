/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No git hosting service is configured.
	 */
	public static ResKey ERROR_NO_GIT_HOSTING;

	/**
	 * @en The repository "{0}" already exists.
	 */
	public static ResKey1 ERROR_REPOSITORY_EXISTS__REPOSITORY;

	/**
	 * @en The repository "{0}" does not exist or is not accessible.
	 */
	public static ResKey1 ERROR_REPOSITORY_NOT_FOUND__REPOSITORY;

	/**
	 * @en The git hosting service does not accept "{0}" as name of a repository.
	 */
	public static ResKey1 ERROR_INVALID_REPOSITORY_NAME__NAME;

	/**
	 * @en The organization or user "{0}" does not exist on the git hosting service.
	 */
	public static ResKey1 ERROR_OWNER_NOT_FOUND__OWNER;

	/**
	 * @en The credentials of the control application do not permit this operation on "{0}".
	 */
	public static ResKey1 ERROR_PERMISSION_DENIED__REPOSITORY;

	/**
	 * @en The git hosting service {0} rejects the credentials of the control application.
	 */
	public static ResKey1 ERROR_AUTHENTICATION_FAILED__SERVICE;

	/**
	 * @en The git hosting service {0} cannot be reached: {1}
	 */
	public static ResKey2 ERROR_SERVICE_UNREACHABLE__SERVICE_PROBLEM;

	/**
	 * @en The git hosting service {0} answered the request {1} with status {2}: {3}
	 */
	public static ResKey4 ERROR_REQUEST_FAILED__SERVICE_OPERATION_STATUS_MESSAGE;

	/**
	 * @en "{0}" is not the URL of a git hosting service.
	 */
	public static ResKey1 ERROR_INVALID_BASE_URL__URL;

	/**
	 * @en The git hosting service has no repository "{0}", or the control application cannot read
	 *     it.
	 */
	public static ResKey1 ERROR_UNKNOWN_REPOSITORY__REFERENCE;

	/**
	 * @en The workspace has no access to the git hosting service: the workspace proxy is switched
	 *     off or has no git route.
	 */
	public static ResKey ERROR_NO_GIT_ROUTE;

	/**
	 * @en The repository "{0}" has no "pom.xml" in its root directory, it holds no TopLogic
	 *     application.
	 */
	public static ResKey1 ERROR_NO_POM__REPOSITORY;

	/**
	 * @en The "pom.xml" of the repository "{0}" cannot be read: {1}
	 */
	public static ResKey2 ERROR_INVALID_POM__REPOSITORY_PROBLEM;

	/**
	 * @en The application in the repository "{0}" has the artifact ID "{1}", the project expects
	 *     "{2}".
	 */
	public static ResKey3 ERROR_ARTIFACT_MISMATCH__REPOSITORY_FOUND_EXPECTED;

	/**
	 * @en The application in the repository "{0}" is built on TopLogic {1}, the project on {2}.
	 */
	public static ResKey3 ERROR_ENGINE_VERSION_MISMATCH__REPOSITORY_FOUND_EXPECTED;

	static {
		initConstants(I18NConstants.class);
	}
}
