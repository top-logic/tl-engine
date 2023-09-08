/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelProvider} that constantly returns <code>null</code>.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NullModelProvider implements ModelProvider {

	/** Singleton {@link NullModelProvider} instance. */
	public static final NullModelProvider INSTANCE = new NullModelProvider();

	private NullModelProvider() {
		// singleton instance
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return null;
	}

}

