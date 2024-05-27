/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.model;

import java.util.Objects;

import com.top_logic.knowledge.service.migration.Version;

/**
 * The version name of a {@link MigrationRef}.
 */
public class VersionID {

	private final String _module;

	private final String _version;

	/**
	 * Creates a {@link VersionID}.
	 *
	 * @param module
	 *        See {@link #getModule()}.
	 * @param version
	 *        The name of the version.
	 */
	public VersionID(String module, String version) {
		this._module = module;
		this._version = version;
	}

	/**
	 * Builds a {@link VersionID} from a version config.
	 */
	public static VersionID of(Version v) {
		return new VersionID(v.getModule(), v.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getModule(), _version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionID other = (VersionID) obj;
		return Objects.equals(getModule(), other.getModule()) && Objects.equals(_version, other._version);
	}

	/**
	 * The module in which this version was created.
	 */
	String getModule() {
		return _module;
	}

	@Override
	public String toString() {
		return _version + "(" + _module + ")";
	}
}
