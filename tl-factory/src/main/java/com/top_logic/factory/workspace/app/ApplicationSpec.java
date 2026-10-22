/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.app;

import java.util.Objects;

/**
 * Description of a TopLogic application to {@link ApplicationWorkspaces#create(ApplicationSpec,
 * com.top_logic.factory.workspace.ExecOutput) create} in a workspace of its own.
 */
public final class ApplicationSpec {

	private final String _workspaceId;

	private final String _groupId;

	private final String _artifactId;

	private String _engineVersion;

	private String _initialPassword;

	/**
	 * Creates an {@link ApplicationSpec}.
	 *
	 * @param workspaceId
	 *        See {@link #getWorkspaceId()}.
	 * @param groupId
	 *        See {@link #getGroupId()}.
	 * @param artifactId
	 *        See {@link #getArtifactId()}.
	 */
	public ApplicationSpec(String workspaceId, String groupId, String artifactId) {
		_workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
		_groupId = Objects.requireNonNull(groupId, "groupId");
		_artifactId = Objects.requireNonNull(artifactId, "artifactId");
	}

	/**
	 * The identifier of the workspace the application is created in.
	 *
	 * @see com.top_logic.factory.workspace.WorkspaceSpec#getWorkspaceId()
	 */
	public String getWorkspaceId() {
		return _workspaceId;
	}

	/**
	 * The Maven group of the application, e.g. {@code com.example}.
	 */
	public String getGroupId() {
		return _groupId;
	}

	/**
	 * The Maven artifact of the application, e.g. {@code my-app}.
	 *
	 * <p>
	 * It also names the directory of the application in the workspace and the context path the
	 * application is served under.
	 * </p>
	 */
	public String getArtifactId() {
		return _artifactId;
	}

	/**
	 * The TopLogic version the application is built on, {@code null} for the
	 * {@link ApplicationWorkspaces.Config#getEngineVersion() configured default}.
	 */
	public String getEngineVersion() {
		return _engineVersion;
	}

	/**
	 * Sets the TopLogic version the application is built on.
	 *
	 * @see #getEngineVersion()
	 */
	public ApplicationSpec setEngineVersion(String engineVersion) {
		_engineVersion = engineVersion;
		return this;
	}

	/**
	 * The password the account {@code root} of the application gets when the application creates
	 * its database, {@code null} for a random one, which the application only writes to its log.
	 *
	 * @see ApplicationWorkspaces#ENV_INITIAL_PASSWORD
	 */
	public String getInitialPassword() {
		return _initialPassword;
	}

	/**
	 * Sets the initial password of the account {@code root} of the application.
	 *
	 * @see #getInitialPassword()
	 */
	public ApplicationSpec setInitialPassword(String initialPassword) {
		_initialPassword = initialPassword;
		return this;
	}

	@Override
	public String toString() {
		return "Application " + _groupId + ":" + _artifactId + " in workspace " + _workspaceId;
	}

}
