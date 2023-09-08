/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import java.util.Collection;

/**
 * {@link PreloadOperation} that does no preload at all.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoPreload implements PreloadOperation {

	/**
	 * Singleton {@link NoPreload} instance.
	 */
	public static final NoPreload INSTANCE = new NoPreload();

	private NoPreload() {
		// Singleton constructor.
	}

	@Override
	public void prepare(PreloadContext context, Collection<?> baseObjects) {
		// Ignore.
	}

}
