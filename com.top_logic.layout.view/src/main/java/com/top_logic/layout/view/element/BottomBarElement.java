/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.nav.ReactBottomBarControl;
import com.top_logic.layout.react.control.nav.ReactBottomBarControl.BottomBarEntry;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactBottomBarControl}.
 *
 * <p>
 * Renders a bottom navigation bar with icon items. Since this is a declarative element without
 * server-side command handling, item selection is currently visual only.
 * </p>
 */
public class BottomBarElement implements UIElement {

	/**
	 * Configuration for {@link BottomBarElement}.
	 */
	@TagName("bottom-bar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(BottomBarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getItems()}. */
		String ITEMS = "items";

		/** Configuration name for {@link #getActiveItem()}. */
		String ACTIVE_ITEM = "active-item";

		/**
		 * The navigation items.
		 */
		@Name(ITEMS)
		List<ItemConfig> getItems();

		/**
		 * The ID of the initially active item.
		 */
		@Name(ACTIVE_ITEM)
		String getActiveItem();
	}

	/**
	 * Configuration for a single bottom bar item.
	 */
	@TagName("item")
	public interface ItemConfig extends com.top_logic.basic.config.ConfigurationItem {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/**
		 * The item identifier.
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
	}

	private final List<BottomBarEntry> _items;

	private final String _activeItem;

	/**
	 * Creates a new {@link BottomBarElement} from configuration.
	 */
	@CalledByReflection
	public BottomBarElement(InstantiationContext context, Config config) {
		_items = new ArrayList<>();
		for (ItemConfig item : config.getItems()) {
			_items.add(new BottomBarEntry(item.getId(), item.getLabel(), item.getIcon()));
		}
		_activeItem = config.getActiveItem();
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		String activeItem = _activeItem != null && !_activeItem.isEmpty()
			? _activeItem
			: (!_items.isEmpty() ? _items.get(0).id() : "");
		return new ReactBottomBarControl(_items, activeItem, itemId -> {
			// Visual-only selection in the declarative view; no server-side handler.
		});
	}
}
