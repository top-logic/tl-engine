/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.app;

import com.top_logic.util.error.TopLogicException;

/**
 * The application of a workspace did not start, while the container of the workspace runs.
 *
 * <p>
 * The supervisor in the container reports the application as
 * {@link ApplicationWorkspaces.ApplicationState#FAILED failed}; the end of its log has been written
 * to the output of the operation that waited for it.
 * </p>
 */
public class ApplicationFailedException extends TopLogicException {

	/**
	 * Creates an {@link ApplicationFailedException}.
	 *
	 * @param workspaceId
	 *        The workspace whose application failed.
	 */
	public ApplicationFailedException(String workspaceId) {
		super(I18NConstants.ERROR_APPLICATION_FAILED__WORKSPACE.fill(workspaceId));
	}

}
