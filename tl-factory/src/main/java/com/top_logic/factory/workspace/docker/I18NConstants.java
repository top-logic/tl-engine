/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.docker;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The request to the Docker daemon at {0} failed: {1}
	 */
	public static ResKey2 ERROR_DOCKER_REQUEST_FAILED__HOST_MESSAGE;

	/**
	 * @en The image "{0}" could not be pulled: {1}
	 */
	public static ResKey2 ERROR_IMAGE_PULL_FAILED__IMAGE_MESSAGE;

	/**
	 * @en The Docker volume "{0}" already exists and does not belong to this workspace.
	 */
	public static ResKey1 ERROR_FOREIGN_VOLUME__NAME;

	/**
	 * @en The Docker container "{0}" already exists and does not belong to this application.
	 */
	public static ResKey1 ERROR_FOREIGN_CONTAINER__NAME;

	/**
	 * @en The image "{0}" could not be built: {1}
	 */
	public static ResKey2 ERROR_IMAGE_BUILD_FAILED__IMAGE_MESSAGE;

	/**
	 * @en There is no shared volume "{0}".
	 */
	public static ResKey1 ERROR_UNKNOWN_SHARED_VOLUME__NAME;

	/**
	 * @en The shared volume "{0}" is still used by a workspace.
	 */
	public static ResKey1 ERROR_SHARED_VOLUME_IN_USE__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
