/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import com.top_logic.basic.col.Mapping;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link CheckScopeProviderAdapter} dispatches its
 * {@link #getCheckScope(LayoutComponent) main method} to another
 * {@link CheckScopeProvider}. The component which is transmitted to the
 * implementation is the image of the asked component under the given
 * {@link Mapping}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CheckScopeProviderAdapter implements CheckScopeProvider {

	private final CheckScopeProvider impl;
	private final Mapping<? super LayoutComponent, ? extends LayoutComponent> map;

	public CheckScopeProviderAdapter(CheckScopeProvider impl, Mapping<? super LayoutComponent, ? extends LayoutComponent> map) {
		this.impl = impl;
		this.map = map;
	}

	@Override
	public CheckScope getCheckScope(LayoutComponent component) {
		final LayoutComponent mappedComponent = map.map(component);
		return impl.getCheckScope(mappedComponent);
	}

}
