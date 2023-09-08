/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelProvider} that delegates to a {@link LayoutComponent} implementing the
 * {@link ModelProvider} interface by itself.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelfModelProvider implements ModelProvider {

	/**
	 * Singleton {@link SelfModelProvider} instance.
	 */
	public static final SelfModelProvider INSTANCE = new SelfModelProvider();

	private SelfModelProvider() {
		// Singleton constructor.
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return ((ModelProvider) businessComponent).getBusinessModel(businessComponent);
	}
}
