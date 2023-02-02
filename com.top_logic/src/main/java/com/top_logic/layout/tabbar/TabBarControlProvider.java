/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.structure.DecoratingLayoutControlProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link TabBarControlProvider} is a {@link LayoutControlProvider} which creates a
 * control for the tab bar of a {@link TabComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TabBarControlProvider extends DecoratingLayoutControlProvider<TabBarControlProvider.Config> {

	/**
	 * Typed configuration interface definition for {@link TabBarControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<TabBarControlProvider> {

	}

	/** Singleton {@link TabBarControlProvider} instance. */
	public static final TabBarControlProvider INSTANCE = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
		.getInstance(TypedConfiguration.newConfigItem(Config.class));

	/** Creates a {@link TabBarControlProvider}. */
	public TabBarControlProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Control mkLayout(Strategy strategy, LayoutComponent component) {
		TabComponent aTabComponent = (TabComponent) component;
		TabBarControl theTabBar = new TabBarControl(aTabComponent.getTabBarModel());
		theTabBar.setTabVisibilityFilter(aTabComponent.getTabVisibilityFilter());
		theTabBar.addViews(aTabComponent.getConfiguredViews());
		theTabBar.setRenderer(aTabComponent.getRenderer());
		theTabBar.addTabSwitchVetoListener(DefaultTabSwitchVetoListener.INSTANCE);

		LayoutControlAdapter layoutControlAdapter =
			new LayoutControlAdapter(theTabBar);
		layoutControlAdapter.setConstraint(new DefaultLayoutData(DisplayDimension.HUNDERED_PERCENT, 100, DisplayDimension.dim(aTabComponent.getRenderer().getBarSize()
				+ aTabComponent.getRenderer().getBottomSpacerSize(), DisplayUnit.PIXEL),
			100, Scrolling.NO));

		return layoutControlAdapter;
	}

}
