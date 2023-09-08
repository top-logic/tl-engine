/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.InstantiationContext;


/**
 * {@link ComponentResolver} that does nothing.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class NoResolver extends ComponentResolver {

	/** Singleton {@link NoResolver} instance. */
	public static final NoResolver INSTANCE = new NoResolver();

	private NoResolver() {
		// singleton instance
	}

	@Override
	public void resolveComponent(InstantiationContext context, LayoutComponent component) {
		// no resolve here
	}

}

