/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.version;

import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

import de.haumacher.msgbuf.data.ReflectiveDataObject;

/**
 * {@link Accessor} of {@link VersionInfo} data.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LicenseDataAccessor extends ReadOnlyAccessor<ReflectiveDataObject> {

	/**
	 * Singleton {@link LicenseDataAccessor} instance.
	 */
	public static final LicenseDataAccessor INSTANCE = new LicenseDataAccessor();

	private LicenseDataAccessor() {
		// Singleton constructor.
	}

	@Override
	public Object getValue(ReflectiveDataObject object, String property) {
		return object.get(property);
	}

}
