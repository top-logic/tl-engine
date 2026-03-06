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
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

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
	public interface TabConfig extends com.top_logic.basic.config.ConfigurationItem {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * The unique tab identifier.
		 */
		@Name(ID)
		String getId();

		/**
		 * The tab display label.
		 */
		@Name(LABEL)
		String getLabel();

		/**
		 * The content elements shown when this tab is active.
		 */
		@Name(CHILDREN)
		@DefaultContainer
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
			_tabs.add(new TabEntry(tabConfig.getId(), tabConfig.getLabel(), children));
		}
		_activeTab = config.getActiveTab();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<TabDefinition> tabDefs = new ArrayList<>();
		for (TabEntry entry : _tabs) {
			tabDefs.add(new TabDefinition(entry._id, entry._label, () -> createContent(entry._children, context)));
		}
		String activeTab = _activeTab != null && !_activeTab.isEmpty() ? _activeTab : null;
		return new ReactTabBarControl(null, tabDefs, activeTab);
	}

	private static ReactControl createContent(List<UIElement> elements, ViewContext context) {
		if (elements.size() == 1) {
			return (ReactControl) elements.get(0).createControl(context);
		}
		List<ReactControl> children = elements.stream()
			.map(e -> (ReactControl) e.createControl(context))
			.collect(Collectors.toList());
		return new ReactStackControl(children);
	}

	private record TabEntry(String _id, String _label, List<UIElement> _children) {
	}
}
