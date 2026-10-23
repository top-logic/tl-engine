/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description of a workspace to {@link WorkspaceProvider#create(WorkspaceSpec) create}: the image
 * of its container, its environment, its volumes and the port its application listens on.
 */
public final class WorkspaceSpec {

	/**
	 * The {@link #getContainerPort() container port} of a workspace that does not set one.
	 */
	public static final int DEFAULT_CONTAINER_PORT = 8080;

	private final String _workspaceId;

	private final String _image;

	private List<String> _command = Collections.emptyList();

	private final Map<String, String> _env = new LinkedHashMap<>();

	private final List<VolumeMount> _volumes = new ArrayList<>();

	private final Map<String, String> _labels = new LinkedHashMap<>();

	private int _containerPort = DEFAULT_CONTAINER_PORT;

	/**
	 * Creates a {@link WorkspaceSpec}.
	 *
	 * @param workspaceId
	 *        See {@link #getWorkspaceId()}.
	 * @param image
	 *        See {@link #getImage()}.
	 */
	public WorkspaceSpec(String workspaceId, String image) {
		_workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
		_image = Objects.requireNonNull(image, "image");
	}

	/**
	 * The identifier of the workspace, chosen by the caller.
	 *
	 * <p>
	 * All later operations address the workspace by this identifier. It consists of letters,
	 * digits, {@code _}, {@code .} and {@code -} and starts with a letter or a digit.
	 * </p>
	 */
	public String getWorkspaceId() {
		return _workspaceId;
	}

	/**
	 * The image the workspace container is created from, e.g. {@code busybox:1.37}.
	 */
	public String getImage() {
		return _image;
	}

	/**
	 * The command the container runs, empty for the default command of the {@link #getImage()
	 * image}.
	 */
	public List<String> getCommand() {
		return _command;
	}

	/**
	 * Sets the command the container runs.
	 *
	 * @see #getCommand()
	 */
	public WorkspaceSpec setCommand(List<String> command) {
		_command = Collections.unmodifiableList(new ArrayList<>(command));
		return this;
	}

	/**
	 * Environment variables of the container.
	 */
	public Map<String, String> getEnv() {
		return Collections.unmodifiableMap(_env);
	}

	/**
	 * Adds an environment variable to the container.
	 *
	 * @see #getEnv()
	 */
	public WorkspaceSpec putEnv(String name, String value) {
		_env.put(name, value);
		return this;
	}

	/**
	 * The volumes of the workspace and where they are mounted.
	 */
	public List<VolumeMount> getVolumes() {
		return Collections.unmodifiableList(_volumes);
	}

	/**
	 * Adds a volume to the workspace.
	 *
	 * @see #getVolumes()
	 */
	public WorkspaceSpec addVolume(VolumeMount volume) {
		_volumes.add(volume);
		return this;
	}

	/**
	 * Additional labels of the container and the volumes of the workspace.
	 *
	 * <p>
	 * The labels a provider sets to recognize its own objects take precedence over labels of the
	 * same name given here.
	 * </p>
	 */
	public Map<String, String> getLabels() {
		return Collections.unmodifiableMap(_labels);
	}

	/**
	 * Adds a label to the container and the volumes of the workspace.
	 *
	 * @see #getLabels()
	 */
	public WorkspaceSpec putLabel(String name, String value) {
		_labels.put(name, value);
		return this;
	}

	/**
	 * The port the application inside the container listens on.
	 *
	 * <p>
	 * When the workspace is started, the provider publishes this port on a free port of the host,
	 * see {@link WorkspaceInfo#getHostPort()}.
	 * </p>
	 */
	public int getContainerPort() {
		return _containerPort;
	}

	/**
	 * Sets the port the application inside the container listens on.
	 *
	 * @see #getContainerPort()
	 */
	public WorkspaceSpec setContainerPort(int containerPort) {
		_containerPort = containerPort;
		return this;
	}

	@Override
	public String toString() {
		return "Workspace " + _workspaceId + " (" + _image + ")";
	}

}
