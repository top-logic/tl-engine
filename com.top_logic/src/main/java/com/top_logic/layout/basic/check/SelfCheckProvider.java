/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link CheckScopeProvider} reporting {@link SingletonCheckScope}s for {@link ChangeHandler}s.
 * 
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class SelfCheckProvider implements CheckScopeProvider {

	/** Singleton instance of this class. */
	public static final SelfCheckProvider INSTANCE = new SelfCheckProvider();

	@Override
	public CheckScope getCheckScope(final LayoutComponent component) {
		return component instanceof ChangeHandler ? new SingletonCheckScope((ChangeHandler) component)
			: CheckScope.NO_CHECK;
	}

}
