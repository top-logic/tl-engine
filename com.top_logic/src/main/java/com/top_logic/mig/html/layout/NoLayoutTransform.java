/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * The identity transform.
 * 
 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
 */
public class NoLayoutTransform implements LayoutTransform {
	/**
	 * Singleton {@link NoLayoutTransform} instance.
	 */
	public static final NoLayoutTransform INSTANCE = new NoLayoutTransform();

	private NoLayoutTransform() {
		// Singleton constructor.
	}

	@Override
	public Config transform(Config config) {
		return config;
	}
}
