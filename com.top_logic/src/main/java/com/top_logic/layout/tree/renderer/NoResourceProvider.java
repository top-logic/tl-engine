/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;

/**
 * {@link ResourceProvider} that provides no resources at all.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoResourceProvider extends AbstractResourceProvider {

	/**
	 * Singleton {@link NoResourceProvider} instance.
	 */
	public static final NoResourceProvider INSTANCE = new NoResourceProvider();

	private NoResourceProvider() {
		// Singleton constructor.
	}

}
