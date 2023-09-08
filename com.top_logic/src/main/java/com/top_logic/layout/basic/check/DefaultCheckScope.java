/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import java.util.Collection;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link DefaultCheckScope} encapsulates a {@link CheckScopeProvider}
 * and a {@link LayoutComponent} and dispatches its
 * {@link #getAffectedFormHandlers()} method to the {@link CheckScope} returned
 * by its {@link CheckScopeProvider} for its {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultCheckScope implements CheckScope {

	private final LayoutComponent component;
	private final CheckScopeProvider scope;

	public DefaultCheckScope(CheckScopeProvider scope, LayoutComponent component) {
		this.scope = scope;
		this.component = component;
	}

	@Override
	public Collection<? extends ChangeHandler> getAffectedFormHandlers() {
		return scope.getCheckScope(component).getAffectedFormHandlers();
	}

}
