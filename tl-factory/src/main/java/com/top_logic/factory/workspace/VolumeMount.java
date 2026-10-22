/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.util.Objects;

/**
 * A named volume and the path it is mounted at inside the workspace container.
 *
 * <p>
 * A volume of the workspace belongs to the workspace: it is created with the workspace, survives
 * stopping and restarting its container and is removed together with the workspace. A
 * {@link #isShared() shared} volume belongs to no workspace: it is created with
 * {@link WorkspaceProvider#ensureSharedVolume(String)}, any number of workspaces mount it, and
 * removing a workspace leaves it alone.
 * </p>
 */
public final class VolumeMount {

	private final String _name;

	private final String _mountPath;

	private final boolean _readOnly;

	private final boolean _shared;

	/**
	 * Creates a writable {@link VolumeMount} of a volume of the workspace.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param mountPath
	 *        See {@link #getMountPath()}.
	 */
	public VolumeMount(String name, String mountPath) {
		this(name, mountPath, false);
	}

	/**
	 * Creates a {@link VolumeMount} of a volume of the workspace.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param mountPath
	 *        See {@link #getMountPath()}.
	 * @param readOnly
	 *        See {@link #isReadOnly()}.
	 */
	public VolumeMount(String name, String mountPath, boolean readOnly) {
		this(name, mountPath, readOnly, false);
	}

	private VolumeMount(String name, String mountPath, boolean readOnly, boolean shared) {
		_name = Objects.requireNonNull(name, "name");
		_mountPath = Objects.requireNonNull(mountPath, "mountPath");
		_readOnly = readOnly;
		_shared = shared;
	}

	/**
	 * Creates a {@link VolumeMount} of a {@link #isShared() shared} volume.
	 *
	 * @param name
	 *        The name the volume was created with by
	 *        {@link WorkspaceProvider#ensureSharedVolume(String)}.
	 * @param mountPath
	 *        See {@link #getMountPath()}.
	 * @param readOnly
	 *        See {@link #isReadOnly()}.
	 */
	public static VolumeMount shared(String name, String mountPath, boolean readOnly) {
		return new VolumeMount(name, mountPath, readOnly, true);
	}

	/**
	 * The name of the volume, unique within its workspace, or among the shared volumes for a
	 * {@link #isShared() shared} volume.
	 *
	 * <p>
	 * This is the logical name the workspace refers to the volume by, not the name of the volume
	 * in the container platform: a provider derives the platform name from the workspace (if the
	 * volume is not shared) and this name.
	 * </p>
	 */
	public String getName() {
		return _name;
	}

	/**
	 * The absolute path inside the container the volume is mounted at.
	 */
	public String getMountPath() {
		return _mountPath;
	}

	/**
	 * Whether the container may only read the volume.
	 */
	public boolean isReadOnly() {
		return _readOnly;
	}

	/**
	 * Whether the volume is shared between workspaces instead of belonging to the workspace.
	 *
	 * @see #shared(String, String, boolean)
	 */
	public boolean isShared() {
		return _shared;
	}

	@Override
	public String toString() {
		return (_shared ? "shared " : "") + _name + ":" + _mountPath + (_readOnly ? ":ro" : "");
	}

}
