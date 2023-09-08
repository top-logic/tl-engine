/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.resolver;

import com.top_logic.model.TLObject;

/**
 * {@link InstanceResolver} that cannot resolve any instances.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoInstanceResolver implements InstanceResolver {

	/**
	 * Singleton {@link NoInstanceResolver} instance.
	 */
	public static final NoInstanceResolver INSTANCE = new NoInstanceResolver();

	private NoInstanceResolver() {
		// Singleton constructor.
	}

	@Override
	public TLObject resolve(String kind, String id) {
		throw new UnsupportedOperationException("No instance resolver for kind '" + kind + "': " + id);
	}

	@Override
	public String buildId(TLObject obj) {
		throw new UnsupportedOperationException("No instance resolver for '" + obj + "'.");
	}

}
