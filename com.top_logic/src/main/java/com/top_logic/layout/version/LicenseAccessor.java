/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.version;

import java.util.stream.Collectors;

import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Accessor for the raw license name of a dependency.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LicenseAccessor extends ReadOnlyAccessor<VersionInfo> {

	/**
	 * Singleton {@link LicenseAccessor} instance.
	 */
	public static final LicenseAccessor INSTANCE = new LicenseAccessor();

	private LicenseAccessor() {
		// Singleton constructor.
	}

	@Override
	public Object getValue(VersionInfo object, String property) {
		return object.getLicenses().stream().map(l -> l.getName()).collect(Collectors.toList());
	}

}
