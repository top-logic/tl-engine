/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Returns a {@link CheckScopeProvider} which constantly returns an &quot;empty&quot;
 * {@link CheckScope}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NoCheckScopeProvider implements CheckScopeProvider {

	/** Singleton {@link NoCheckScopeProvider} instance. */
	public static final NoCheckScopeProvider INSTANCE = new NoCheckScopeProvider();

	private NoCheckScopeProvider() {
		// singleton instance
	}

	/**
	 * Constantly returns {@link CheckScope#NO_CHECK}.
	 */
	@Override
	public CheckScope getCheckScope(LayoutComponent component) {
		return CheckScope.NO_CHECK;
	}

}
