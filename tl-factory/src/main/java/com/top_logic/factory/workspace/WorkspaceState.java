/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

/**
 * The state of the container of a workspace, as reported by a {@link WorkspaceProvider}.
 */
public enum WorkspaceState {

	/**
	 * The container exists but has never been started.
	 */
	CREATED,

	/**
	 * The container is running.
	 */
	RUNNING,

	/**
	 * The container has been started before and is not running now. Its volumes are kept.
	 */
	STOPPED,

	/**
	 * The container is in a state the provider does not manage, e.g. paused, being removed or
	 * dead.
	 */
	UNKNOWN,

	/**
	 * The workspace has no container. Volumes of the workspace may still exist.
	 */
	MISSING;

}
