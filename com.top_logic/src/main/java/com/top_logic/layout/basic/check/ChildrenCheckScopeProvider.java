/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import com.top_logic.layout.form.FormHandler;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link ChildrenCheckScopeProvider} returns for a component
 * <code>comp</code> a {@link CheckScope} consists of all visible
 * {@link FormHandler} children of <code>comp</code>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChildrenCheckScopeProvider implements CheckScopeProvider {

	public static final ChildrenCheckScopeProvider INSTANCE = new ChildrenCheckScopeProvider();

	private ChildrenCheckScopeProvider() {
		// singleton instance
	}

	@Override
	public CheckScope getCheckScope(LayoutComponent component) {
		return new ChildrenCheckScope(component);
	}

}
