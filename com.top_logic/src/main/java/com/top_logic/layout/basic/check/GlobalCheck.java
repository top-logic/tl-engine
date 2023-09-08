/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * The class {@link GlobalCheck} is a provider which returns a
 * {@link CheckScope} containing all visible {@link FormHandler} children of the
 * {@link MainLayout} of a given {@link LayoutComponent} which have to be
 * checked.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GlobalCheck extends CheckScopeProviderAdapter {

	public static final GlobalCheck INSTANCE = new GlobalCheck();

	private GlobalCheck() {
		super(ChildrenCheckScopeProvider.INSTANCE, new Mapping<LayoutComponent, LayoutComponent>() {

			@Override
			public LayoutComponent map(LayoutComponent input) {
				return input.getMainLayout();
			}
		});
	}
}
