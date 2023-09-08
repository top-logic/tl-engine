/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} that wraps the default layout for a component with a
 * {@link MaximizableControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MaximizableLayout implements LayoutControlProvider {

	/**
	 * Singleton {@link MaximizableLayout} instance.
	 */
	public static final MaximizableLayout INSTANCE = new MaximizableLayout();

	private MaximizableLayout() {
		// Singleton constructor.
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
		LayoutControl layout = strategy.createDefaultLayout(component);
		MaximizableControl result = new MaximizableControl(component);
		result.setChildControl(layout);
		return result;
	}

}
