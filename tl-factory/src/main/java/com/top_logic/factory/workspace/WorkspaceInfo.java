/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Snapshot of a workspace as a {@link WorkspaceProvider} sees it at the time of the request.
 */
public final class WorkspaceInfo {

	/**
	 * Value of {@link #getHostPort()} of a workspace whose port is not published.
	 */
	public static final int NO_PORT = 0;

	private final String _workspaceId;

	private final WorkspaceState _state;

	private final String _containerId;

	private final String _image;

	private final int _hostPort;

	private final List<String> _volumes;

	/**
	 * Creates a {@link WorkspaceInfo}.
	 *
	 * @param workspaceId
	 *        See {@link #getWorkspaceId()}.
	 * @param state
	 *        See {@link #getState()}.
	 * @param containerId
	 *        See {@link #getContainerId()}.
	 * @param image
	 *        See {@link #getImage()}.
	 * @param hostPort
	 *        See {@link #getHostPort()}.
	 * @param volumes
	 *        See {@link #getVolumes()}.
	 */
	public WorkspaceInfo(String workspaceId, WorkspaceState state, String containerId, String image,
			int hostPort, List<String> volumes) {
		_workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
		_state = Objects.requireNonNull(state, "state");
		_containerId = containerId;
		_image = image;
		_hostPort = hostPort;
		_volumes = Collections.unmodifiableList(new ArrayList<>(volumes));
	}

	/**
	 * Creates the {@link WorkspaceInfo} of a workspace without container.
	 *
	 * @param workspaceId
	 *        See {@link #getWorkspaceId()}.
	 * @param volumes
	 *        See {@link #getVolumes()}.
	 */
	public static WorkspaceInfo missing(String workspaceId, List<String> volumes) {
		return new WorkspaceInfo(workspaceId, WorkspaceState.MISSING, null, null, NO_PORT, volumes);
	}

	/**
	 * The identifier of the workspace.
	 *
	 * @see WorkspaceSpec#getWorkspaceId()
	 */
	public String getWorkspaceId() {
		return _workspaceId;
	}

	/**
	 * The state of the workspace container.
	 */
	public WorkspaceState getState() {
		return _state;
	}

	/**
	 * The identifier of the container in the container platform, {@code null} if the workspace is
	 * {@link WorkspaceState#MISSING}.
	 */
	public String getContainerId() {
		return _containerId;
	}

	/**
	 * The image of the container, {@code null} if the workspace is {@link WorkspaceState#MISSING}.
	 */
	public String getImage() {
		return _image;
	}

	/**
	 * The port of the host the {@link WorkspaceSpec#getContainerPort() container port} is
	 * published on, {@link #NO_PORT} if the workspace is not {@link WorkspaceState#RUNNING}.
	 */
	public int getHostPort() {
		return _hostPort;
	}

	/**
	 * Whether the {@link WorkspaceSpec#getContainerPort() container port} is published on the
	 * host.
	 *
	 * @see #getHostPort()
	 */
	public boolean hasHostPort() {
		return _hostPort != NO_PORT;
	}

	/**
	 * The names of the volumes of the workspace in the container platform, sorted by name.
	 */
	public List<String> getVolumes() {
		return _volumes;
	}

	@Override
	public String toString() {
		return "Workspace " + _workspaceId + ": " + _state
			+ (hasHostPort() ? " on port " + _hostPort : "")
			+ (_volumes.isEmpty() ? "" : ", volumes " + _volumes);
	}

}
