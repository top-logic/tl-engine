/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.version;

import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * {@link Accessor} of the raw value of the name of a dependency.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DependencyNameAccessor extends ReadOnlyAccessor<VersionInfo> {

	/**
	 * Singleton {@link DependencyNameAccessor} instance.
	 */
	public static final DependencyNameAccessor INSTANCE = new DependencyNameAccessor();

	private DependencyNameAccessor() {
		// Singleton constructor.
	}

	@Override
	public Object getValue(VersionInfo object, String property) {
		String name = object.getName();
		if (name == null) {
			name = object.getArtifactId();
		}
		return name;
	}

}
