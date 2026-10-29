/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import com.top_logic.factory.workspace.WorkspaceState;

/**
 * The stored state of the workspace of a project, the Java counterpart of the classifiers of the
 * enumeration {@link FactoryModel#WORKSPACE_STATUS}.
 *
 * <p>
 * Besides the states of the container a {@link com.top_logic.factory.workspace.WorkspaceProvider}
 * reports (see {@link WorkspaceState}), a workspace is in a {@link #isBusy() busy} state while an
 * operation runs on it, and in state {@link #ERROR} after an operation failed.
 * </p>
 */
public enum WorkspaceStatus {

	/**
	 * The container exists but has never been started.
	 */
	CREATED(false),

	/**
	 * The container is being created and the application is being generated and built.
	 */
	BUILDING(true),

	/**
	 * The application is starting; the state changes to {@link #RUNNING} as soon as it answers.
	 */
	STARTING(true),

	/**
	 * The application runs and answers at the preview URL.
	 */
	RUNNING(false),

	/**
	 * The container runs, its application does not: it was stopped, or it failed to start.
	 *
	 * <p>
	 * The agent of the workspace can work in this state, e.g. to repair the application.
	 * </p>
	 */
	APP_STOPPED(false),

	/**
	 * The container is being stopped.
	 */
	STOPPING(true),

	/**
	 * The container is stopped, its volumes are kept.
	 */
	STOPPED(false),

	/**
	 * The container and the volumes are being removed.
	 */
	REMOVING(true),

	/**
	 * The container is being created anew on the current workspace image, keeping the volumes.
	 */
	UPDATING(true),

	/**
	 * The last operation failed, or the container got lost; the log tells why.
	 */
	ERROR(false),

	/**
	 * The container platform did not tell the state, e.g. because it was not reachable.
	 */
	UNKNOWN(false);

	private final boolean _busy;

	private WorkspaceStatus(boolean busy) {
		_busy = busy;
	}

	/**
	 * Whether the container of a workspace in this state runs, so that commands and the agent can
	 * run in it.
	 */
	public boolean isContainerRunning() {
		return this == RUNNING || this == APP_STOPPED;
	}

	/**
	 * Whether an operation runs on a workspace in this state.
	 */
	public boolean isBusy() {
		return _busy;
	}

	/**
	 * The stored state of a workspace whose container is in the given state and on which no
	 * operation runs.
	 */
	public static WorkspaceStatus of(WorkspaceState state) {
		switch (state) {
			case CREATED:
				return CREATED;
			case RUNNING:
				return RUNNING;
			case STOPPED:
				return STOPPED;
			case MISSING:
				return ERROR;
			case UNKNOWN:
				return UNKNOWN;
		}
		throw new IllegalArgumentException("No such state: " + state);
	}

}
