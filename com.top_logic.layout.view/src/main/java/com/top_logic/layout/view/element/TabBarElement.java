/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;

/**
 * UIElement that wraps {@link ReactTabBarControl}.
 *
 * <p>
 * Renders a tab bar with lazily created content for each tab. Each {@code <tab>} child in the
 * configuration defines a tab with an ID, label, and inline content elements. Selecting a tab
 * creates the content controls on demand and caches them.
 * </p>
 */
@InApp
public class TabBarElement implements UIElement {

	/**
	 * Configuration for {@link TabBarElement}.
	 */
	@TagName("tab-bar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TabBarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTabs()}. */
		String TABS = "tabs";

		/** Configuration name for {@link #getActiveTab()}. */
		String ACTIVE_TAB = "active-tab";

		/**
		 * The tab definitions.
		 *
		 * @implNote Written directly as {@code <tab>} children of the {@code <tab-bar>} (the
		 *           {@code <tabs>} wrapper is optional). Keyed by {@link TabConfig#getId()} so that a
		 *           configuration fragment in another module can add, reposition
		 *           ({@code config:position}) or override individual tabs.
		 */
		@Name(TABS)
		@Key(TabConfig.ID)
		@DefaultContainer
		@TreeProperty
		List<TabConfig> getTabs();

		/**
		 * The ID of the initially active tab, or empty for the first tab.
		 */
		@Name(ACTIVE_TAB)
		String getActiveTab();
	}

	/**
	 * Configuration for a single tab.
	 */
	@TagName("tab")
	public interface TabConfig extends ContentSectionConfig {

		/** Configuration name for {@link #getRoute()}. */
		String ROUTE = "route";

		/**
		 * The route segment for this tab.
		 *
		 * <p>
		 * By default (not set), the tab's {@link #getId() ID} is used as the route
		 * segment. Set to {@code "none"} to explicitly opt out of routing. Set to a
		 * custom value to use a route segment different from the ID.
		 * Routes are always relative (no leading slash).
		 * </p>
		 */
		@Name(ROUTE)
		String getRoute();
	}

	/** Personalization key segment of the content of a tab. */
	private static final String TAB_SEGMENT = "tab";

	private final List<TabEntry> _tabs;

	private final String _activeTab;

	private final String _cssClass;

	/**
	 * Creates a new {@link TabBarElement} from configuration.
	 */
	@CalledByReflection
	public TabBarElement(InstantiationContext context, Config config) {
		_tabs = new ArrayList<>();
		for (TabConfig tabConfig : config.getTabs()) {
			_tabs.add(new TabEntry(ContentSection.of(context, tabConfig), tabConfig.getRoute()));
		}
		_activeTab = config.getActiveTab();
		_cssClass = config.getCssClass();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return _tabs.stream()
			.map(tab -> ChildGroup.keyed(tab._section().getId(), tab._section().getChildren()))
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		RevealPath here = RevealPath.of(context);
		List<TabDefinition> tabDefs = new ArrayList<>();
		for (TabEntry entry : _tabs) {
			ContentSection section = entry._section();
			if (!section.isAccessible()) {
				// Access denied for the current user: omit the tab entirely.
				continue;
			}
			// The tab lies within the scope enclosing the tab bar (a sidebar item, say), so what a
			// form of the tab holds unsaved is held unsaved there as well.
			DirtyChannel dirtyChannel = new DirtyChannel(context.getDirtyChannel());
			// The content of a tab is created only when the tab is first activated, so the tab's
			// context must already say where that content will sit.
			ViewContext tabContext = context.withScope(RevealPath.class, here.append(this, section.getId()));
			TabDefinition tabDef = new TabDefinition(section.getId(), section.label(),
				() -> createContent(section, tabContext, dirtyChannel), dirtyChannel);
			if (section.getIcon() != null) {
				tabDef.withIcon(section.getIcon());
			}
			String effectiveRoute = SidebarElement.resolveRoute(entry._route(), section.getId());
			if (effectiveRoute != null) {
				tabDef.withRoute(effectiveRoute);
			}
			tabDefs.add(tabDef);
		}
		String activeTab = _activeTab != null && !_activeTab.isEmpty() ? _activeTab : null;
		ReactTabBarControl tabBar = new ReactTabBarControl(context, null, tabDefs, activeTab);
		tabBar.setCssClass(_cssClass);

		RevealRegistry registry = context.getRevealRegistry();
		if (registry != null) {
			tabBar.addCleanupAction(registry.registerContainer(this, here, tabBar));
		}

		return tabBar;
	}

	private static ReactControl createContent(ContentSection section, ViewContext context,
			DirtyChannel dirtyChannel) {
		ViewContext tabContext = section.contentContext(context, TAB_SEGMENT);
		tabContext.setDirtyChannel(dirtyChannel);
		return section.createContent(tabContext);
	}

	/**
	 * A configured tab, as far as it is the same for every session.
	 */
	private record TabEntry(ContentSection _section, String _route) {
	}
}
