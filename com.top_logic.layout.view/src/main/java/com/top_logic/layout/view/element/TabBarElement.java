/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.AccessControl;
import com.top_logic.layout.view.security.SecurityScope;
import com.top_logic.layout.view.security.WithAccessControl;
import com.top_logic.util.Resources;

/**
 * UIElement that wraps {@link ReactTabBarControl}.
 *
 * <p>
 * Renders a tab bar with lazily created content for each tab. Each {@code <tab>} child in the
 * configuration defines a tab with an ID, label, and inline content elements. Selecting a tab
 * creates the content controls on demand and caches them.
 * </p>
 */
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
		 */
		@Name(TABS)
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
	public interface TabConfig extends WithAccessControl {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/** Configuration name for {@link #getRoute()}. */
		String ROUTE = "route";

		/**
		 * The unique tab identifier.
		 */
		@Name(ID)
		String getId();

		/**
		 * The tab display label.
		 */
		@Name(LABEL)
		ResKey getLabel();

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

		/**
		 * The content elements shown when this tab is active.
		 */
		@Name(CHILDREN)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();
	}

	private final List<TabEntry> _tabs;

	private final String _activeTab;

	/**
	 * Creates a new {@link TabBarElement} from configuration.
	 */
	@CalledByReflection
	public TabBarElement(InstantiationContext context, Config config) {
		_tabs = new ArrayList<>();
		for (TabConfig tabConfig : config.getTabs()) {
			List<UIElement> children = tabConfig.getChildren().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
			String label = Resources.getInstance().getString(tabConfig.getLabel());
			String route = tabConfig.getRoute();
			_tabs.add(new TabEntry(tabConfig.getId(), label, route, tabConfig.getAccessControl(), children));
		}
		_activeTab = config.getActiveTab();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<TabDefinition> tabDefs = new ArrayList<>();
		for (TabEntry entry : _tabs) {
			if (!AccessChecks.isAccessible(entry._accessControl)) {
				// Access denied for the current user: omit the tab entirely.
				continue;
			}
			DirtyChannel dirtyChannel = new DirtyChannel();
			TabDefinition tabDef = new TabDefinition(entry._id, entry._label,
				() -> createContent(entry, context, dirtyChannel), dirtyChannel);
			String effectiveRoute = SidebarElement.resolveRoute(entry._route, entry._id);
			if (effectiveRoute != null) {
				tabDef.withRoute(effectiveRoute);
			}
			tabDefs.add(tabDef);
		}
		String activeTab = _activeTab != null && !_activeTab.isEmpty() ? _activeTab : null;
		return new ReactTabBarControl(context, null, tabDefs, activeTab);
	}

	private static ReactControl createContent(TabEntry entry, ViewContext context,
			DirtyChannel dirtyChannel) {
		// Per-tab context: extend the personalization key with "tab" (legacy) and extend the slot
		// path with the tab id so same-named <slot-content> in two tabs route into independent
		// positions. Channels are NOT forked: only a <view> declares channels, so tab content
		// shares the enclosing view's channels. A tab needing its own channel namespace embeds a
		// <view-ref> to a separate <view> and binds across that boundary.
		ViewContext baseContext = context.childContext("tab")
			.withChildSlotPath(entry._id);
		// Establish the tab's security scope so command rules in its content default to it.
		SecurityScope scope = AccessChecks.resolveScope(entry._accessControl());
		ViewContext tabContext = scope != null ? baseContext.withSecurityScope(scope) : baseContext;
		tabContext.setDirtyChannel(dirtyChannel);

		List<UIElement> elements = entry._children;
		if (elements.size() == 1) {
			return (ReactControl) elements.get(0).createControl(tabContext);
		}
		List<ReactControl> children = elements.stream()
			.map(e -> (ReactControl) e.createControl(tabContext))
			.collect(Collectors.toList());
		return new ReactStackControl(tabContext, children);
	}

	private record TabEntry(String _id, String _label, String _route, AccessControl _accessControl,
			List<UIElement> _children) {
	}
}
