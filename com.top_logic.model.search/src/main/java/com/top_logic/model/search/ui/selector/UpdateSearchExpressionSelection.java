/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.ui.SearchComponent;

/**
 * Updates the selection of the given loaded search expression.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class UpdateSearchExpressionSelection extends ComponentResolver {

	@Override
	public void resolveComponent(InstantiationContext context, LayoutComponent component) {
		TabComponent tabbar = (TabComponent) component;
		SearchComponent searchComponent = (SearchComponent) tabbar.getParent();

		TabBarModel tabBarModel = tabbar.getTabBarModel();
		tabBarModel.addTabBarListener(new SearchExpressionEditorChangeListener(searchComponent));
	}

}
