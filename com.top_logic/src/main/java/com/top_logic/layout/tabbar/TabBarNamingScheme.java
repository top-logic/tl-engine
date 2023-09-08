/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import com.top_logic.layout.component.ComponentBasedNamingScheme;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.TabComponent.TabComponentTabBarModel;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelNamingScheme} for the {@link TabBarModel} of a {@link TabComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TabBarNamingScheme extends
		ComponentBasedNamingScheme<TabComponentTabBarModel, TabBarNamingScheme.TabBarName> {

	/**
	 * Identifier of the {@link TabBarModel} within a {@link TabComponent}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface TabBarName extends ComponentBasedNamingScheme.ComponentBasedName {
		// Pure marker interface.
	}

	/**
	 * Singleton {@link TabBarNamingScheme} instance.
	 */
	public static final TabBarNamingScheme INSTANCE = new TabBarNamingScheme();

	private TabBarNamingScheme() {
		super(TabComponentTabBarModel.class, TabBarName.class);
	}

	@Override
	protected LayoutComponent getContextComponent(TabComponentTabBarModel model) {
		return model.getComponent();
	}

	@Override
	public TabComponentTabBarModel locateModel(ActionContext context, TabBarName name) {
		return ((TabComponent) locateComponent(context, name)).getTabBar();
	}

}