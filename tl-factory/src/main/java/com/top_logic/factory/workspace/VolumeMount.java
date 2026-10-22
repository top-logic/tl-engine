/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.util.Objects;

/**
 * A named volume of a workspace and the path it is mounted at inside the workspace container.
 *
 * <p>
 * The volume belongs to the workspace: it is created with the workspace, survives stopping and
 * restarting its container and is removed together with the workspace.
 * </p>
 */
public final class VolumeMount {

	private final String _name;

	private final String _mountPath;

	private final boolean _readOnly;

	/**
	 * Creates a writable {@link VolumeMount}.
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
	 * Creates a {@link VolumeMount}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param mountPath
	 *        See {@link #getMountPath()}.
	 * @param readOnly
	 *        See {@link #isReadOnly()}.
	 */
	public VolumeMount(String name, String mountPath, boolean readOnly) {
		_name = Objects.requireNonNull(name, "name");
		_mountPath = Objects.requireNonNull(mountPath, "mountPath");
		_readOnly = readOnly;
	}

	/**
	 * The name of the volume, unique within its workspace.
	 *
	 * <p>
	 * This is the logical name the workspace refers to the volume by, not the name of the volume
	 * in the container platform: a provider derives the platform name from the workspace and this
	 * name.
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

	@Override
	public String toString() {
		return _name + ":" + _mountPath + (_readOnly ? ":ro" : "");
	}

}
