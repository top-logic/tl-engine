/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

/**
 * {@link PageRenderer} that renders a fixed title bar.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StaticPageRenderer extends PageRenderer {

	/**
	 * Singleton {@link StaticPageRenderer} instance.
	 */
	public static final StaticPageRenderer INSTANCE = new StaticPageRenderer();

	private StaticPageRenderer() {
		// Singleton constructor.
	}

}
