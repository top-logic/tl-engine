/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.sidebar.NavigationItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SeparatorItem;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.structure.PersonalizingExpandable;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactSidebarControl}.
 *
 * <p>
 * Renders a sidebar with navigation items. Each {@code <nav-item>} has inline content elements that
 * are lazily created when the item is selected. Separators can be added between items.
 * </p>
 */
public class SidebarElement implements UIElement {

	/**
	 * Configuration for {@link SidebarElement}.
	 */
	@TagName("sidebar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SidebarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getItems()}. */
		String ITEMS = "items";

		/** Configuration name for {@link #getActiveItem()}. */
		String ACTIVE_ITEM = "active-item";

		/** Configuration name for {@link #getCollapsed()}. */
		String COLLAPSED = "collapsed";

		/**
		 * The sidebar navigation items.
		 */
		@Name(ITEMS)
		List<PolymorphicConfiguration<? extends SidebarItemElement>> getItems();

		/**
		 * The ID of the initially active item, or empty for the first navigation item.
		 */
		@Name(ACTIVE_ITEM)
		String getActiveItem();

		/**
		 * Whether the sidebar starts collapsed.
		 */
		@Name(COLLAPSED)
		@BooleanDefault(false)
		boolean getCollapsed();
	}

	/**
	 * Base interface for sidebar item elements.
	 */
	public interface SidebarItemElement {

		/**
		 * Creates a {@link SidebarItem} for use with {@link ReactSidebarControl}.
		 */
		SidebarItem createSidebarItem(ViewContext context);
	}

	/**
	 * A navigation item with content.
	 */
	@TagName("nav-item")
	public interface NavItemConfig extends PolymorphicConfiguration<SidebarItemElement> {

		@Override
		@ClassDefault(NavItemElement.class)
		Class<? extends SidebarItemElement> getImplementationClass();

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * The unique item identifier.
		 */
		@Name(ID)
		String getId();

		/**
		 * The display label.
		 */
		@Name(LABEL)
		String getLabel();

		/**
		 * The CSS icon class (e.g. "bi bi-speedometer2").
		 */
		@Name(ICON)
		String getIcon();

		/**
		 * The content elements shown when this item is selected.
		 */
		@Name(CHILDREN)
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();
	}

	/**
	 * A separator line between sidebar items.
	 */
	@TagName("separator")
	public interface SeparatorConfig extends PolymorphicConfiguration<SidebarItemElement> {

		@Override
		@ClassDefault(SeparatorElement.class)
		Class<? extends SidebarItemElement> getImplementationClass();
	}

	/**
	 * Implementation of a navigation sidebar item.
	 */
	public static class NavItemElement implements SidebarItemElement {

		private final String _id;

		private final String _label;

		private final String _icon;

		private final List<UIElement> _children;

		/**
		 * Creates a {@link NavItemElement}.
		 */
		@CalledByReflection
		public NavItemElement(InstantiationContext context, NavItemConfig config) {
			_id = config.getId();
			_label = config.getLabel();
			_icon = config.getIcon();
			_children = config.getChildren().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
		}

		@Override
		public SidebarItem createSidebarItem(ViewContext context) {
			return new NavigationItem(_id, _label, _icon, () -> createContent(_children, context));
		}
	}

	/**
	 * Implementation of a separator sidebar item.
	 */
	public static class SeparatorElement implements SidebarItemElement {

		private static int _counter;

		/**
		 * Creates a {@link SeparatorElement}.
		 */
		@CalledByReflection
		public SeparatorElement(InstantiationContext context, SeparatorConfig config) {
			// No configuration needed.
		}

		@Override
		public SidebarItem createSidebarItem(ViewContext context) {
			return new SeparatorItem("sep" + (_counter++));
		}
	}

	private final List<SidebarItemElement> _items;

	private final String _activeItem;

	private final boolean _collapsed;

	/**
	 * Creates a new {@link SidebarElement} from configuration.
	 */
	@CalledByReflection
	public SidebarElement(InstantiationContext context, Config config) {
		_items = config.getItems().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
		_activeItem = config.getActiveItem();
		_collapsed = config.getCollapsed();
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		String key = resolveKey(context, "sidebar");

		boolean collapsed = PersonalizingExpandable.loadCollapsed(key + ".collapsed", _collapsed);
		Map<String, Boolean> groupStates = loadGroupStates(key);

		List<SidebarItem> sidebarItems = new ArrayList<>();
		for (SidebarItemElement itemElement : _items) {
			sidebarItems.add(itemElement.createSidebarItem(context));
		}
		String activeItem = _activeItem != null && !_activeItem.isEmpty() ? _activeItem : null;
		return new ReactSidebarControl(
			sidebarItems, activeItem,
			collapsed, groupStates,
			c -> PersonalizingExpandable.saveCollapsed(key + ".collapsed", c, _collapsed),
			(gid, exp) -> saveGroupState(key, gid, exp),
			null, null, null, null);
	}

	private static String resolveKey(ViewContext context, String defaultSegment) {
		return context.getPersonalizationKey() + "." + defaultSegment;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Boolean> loadGroupStates(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return null;
		}
		Object value = pc.getJSONValue(key + ".groups");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<String, Boolean> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				if (entry.getValue() instanceof Boolean) {
					result.put(entry.getKey(), (Boolean) entry.getValue());
				}
			}
			return result;
		}
		return null;
	}

	private static void saveGroupState(String key, String groupId, boolean expanded) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<String, Boolean> states = loadGroupStates(key);
		if (states == null) {
			states = new HashMap<>();
		}
		states.put(groupId, Boolean.valueOf(expanded));
		if (states.isEmpty()) {
			pc.setJSONValue(key + ".groups", null);
		} else {
			pc.setJSONValue(key + ".groups", states);
		}
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
}
