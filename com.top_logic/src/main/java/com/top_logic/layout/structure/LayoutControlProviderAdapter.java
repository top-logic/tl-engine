/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link LayoutControlProviderAdapter} is an adapter for
 * {@link LayoutControlProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LayoutControlProviderAdapter implements LayoutControlProvider {

	private final LayoutControlProvider impl;

	public LayoutControlProviderAdapter(LayoutControlProvider impl) {
		if (impl == null) {
			throw new NullPointerException(LayoutControlProviderAdapter.class.getName() + " with 'null' as implementation is senseless.");
		}
		this.impl = impl;
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
		return impl.createLayoutControl(strategy, component);
	}

}
