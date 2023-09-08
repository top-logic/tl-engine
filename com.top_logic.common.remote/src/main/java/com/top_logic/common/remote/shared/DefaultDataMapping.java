/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * Default {@link DataMapping} implementation that can only handle {@link SharedObject} instances.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDataMapping extends AbstractDataMapping {

	/**
	 * Singleton {@link DefaultDataMapping} instance.
	 */
	public static final DefaultDataMapping INSTANCE = new DefaultDataMapping();

	private DefaultDataMapping() {
		// Singleton constructor.
	}

	@Override
	protected ObjectData nonSharedObjectData(ObjectScope scope, Object obj) {
		return null;
	}

}
