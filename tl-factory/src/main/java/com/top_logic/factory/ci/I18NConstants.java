/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

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
	 * @en No CI server is configured.
	 */
	public static ResKey ERROR_NO_CI;

	/**
	 * @en The CI server builds only projects on a built-in git connection; "{0}" is on a git
	 *     connection of its tenant.
	 */
	public static ResKey1 ERROR_CI_NEEDS_BUILT_IN_CONNECTION__PROJECT;

	/**
	 * @en The project "{0}" has no repository on the git hosting service.
	 */
	public static ResKey1 ERROR_NO_REPOSITORY__PROJECT;

	/**
	 * @en The repository of the project "{0}" has no application yet; create the workspace of the
	 *     project first.
	 */
	public static ResKey1 ERROR_NO_SOURCES__PROJECT;

	/**
	 * @en The project "{0}" has no CI job; set up its CI first.
	 */
	public static ResKey1 ERROR_NO_CI_JOB__PROJECT;

	/**
	 * @en The project "{0}" has a CI job already.
	 */
	public static ResKey1 ERROR_HAS_CI_JOB__PROJECT;

	/**
	 * @en CI set up with the CI job "{0}". {1}
	 */
	public static ResKey2 CI_SET_UP__JOB_BUILD;

	/**
	 * @en CI set up with the CI job "{0}". {1} The access token of the git connection may not
	 *     administer the repository, so the pipeline file {2} is not protected; protect it on the
	 *     git hosting service.
	 */
	public static ResKey3 CI_SET_UP_UNPROTECTED__JOB_BUILD_FILE;

	/**
	 * @en The CI server {0} cannot be reached: {1}
	 */
	public static ResKey2 ERROR_CI_UNREACHABLE__SERVICE_PROBLEM;

	/**
	 * @en The CI server {0} rejects the credentials of the control application.
	 */
	public static ResKey1 ERROR_CI_AUTHENTICATION_FAILED__SERVICE;

	/**
	 * @en The credentials of the control application do not permit this operation on the CI job
	 *     "{0}".
	 */
	public static ResKey1 ERROR_CI_PERMISSION_DENIED__JOB;

	/**
	 * @en The CI job "{0}" does not exist.
	 */
	public static ResKey1 ERROR_CI_JOB_NOT_FOUND__JOB;

	/**
	 * @en The CI job "{0}" has no build {1}.
	 */
	public static ResKey2 ERROR_CI_BUILD_NOT_FOUND__JOB_BUILD;

	/**
	 * @en The CI server {0} answered the request {1} with status {2}: {3}
	 */
	public static ResKey4 ERROR_CI_REQUEST_FAILED__SERVICE_OPERATION_STATUS_MESSAGE;

	/**
	 * @en "{0}" is not the URL of a CI server.
	 */
	public static ResKey1 ERROR_INVALID_CI_URL__URL;

	/**
	 * @en The template "{0}" cannot be read.
	 */
	public static ResKey1 ERROR_MISSING_TEMPLATE__NAME;

	/**
	 * @en Build {0} of the CI job "{1}" started.
	 */
	public static ResKey2 BUILD_STARTED__NUMBER_JOB;

	/**
	 * @en A build of the CI job "{0}" waits for an executor.
	 */
	public static ResKey1 BUILD_QUEUED__JOB;

	/**
	 * @en Running
	 */
	public static ResKey BUILD_RUNNING;

	/**
	 * @en Successful
	 */
	public static ResKey BUILD_SUCCESS;

	/**
	 * @en Failed
	 */
	public static ResKey BUILD_FAILURE;

	/**
	 * @en Unstable
	 */
	public static ResKey BUILD_UNSTABLE;

	/**
	 * @en Aborted
	 */
	public static ResKey BUILD_ABORTED;

	/**
	 * @en Not built
	 */
	public static ResKey BUILD_NOT_BUILT;

	static {
		initConstants(I18NConstants.class);
	}
}
