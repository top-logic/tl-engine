/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link CheckScopeProvider} return {@link CheckScope}s for
 * {@link LayoutComponent}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CheckScopeProvider {

	/**
	 * Returns a {@link CheckScope} for the given {@link LayoutComponent}.
	 * 
	 * @param component
	 *        the component for which a {@link CheckScope} is demanded.
	 * @return never <code>null</code>.
	 */
	public CheckScope getCheckScope(LayoutComponent component);
}
