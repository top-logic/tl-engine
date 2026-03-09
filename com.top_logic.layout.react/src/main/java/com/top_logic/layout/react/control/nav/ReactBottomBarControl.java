/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Bottom navigation bar for mobile screens.
 *
 * <p>
 * Renders 3-5 navigation items with icons and labels. The active item is highlighted. Supports
 * optional badge display on items.
 * </p>
 */
public class ReactBottomBarControl extends ReactControl {

	private static final String REACT_MODULE = "TLBottomBar";

	/** @see #updateItems(List) */
	private static final String ITEMS = "items";

	/** @see #setActiveItem(String) */
	private static final String ACTIVE_ITEM_ID = "activeItemId";

	/** Entry identifier within the bar. */
	private static final String ENTRY_ID = "id";

	/** Entry display label. */
	private static final String ENTRY_LABEL = "label";

	/** Entry CSS icon class. */
	private static final String ENTRY_ICON = "icon";

	/** Entry badge text. */
	private static final String ENTRY_BADGE = "badge";

	/** Command argument: the selected item identifier. */
	private static final String ITEM_ID_ARG = "itemId";

	private final Consumer<String> _selectHandler;

	/**
	 * Creates a bottom navigation bar.
	 *
	 * @param items
	 *        The navigation items.
	 * @param activeItemId
	 *        The ID of the currently active item.
	 * @param selectHandler
	 *        Called with the item ID when an item is tapped.
	 */
	public ReactBottomBarControl(ReactContext context, List<BottomBarEntry> items, String activeItemId,
			Consumer<String> selectHandler) {
		super(context, null, REACT_MODULE);
		_selectHandler = selectHandler;
		updateItems(items);
		setActiveItem(activeItemId);
	}

	/**
	 * Updates the navigation items.
	 *
	 * @param items
	 *        The new navigation items.
	 */
	public void updateItems(List<BottomBarEntry> items) {
		List<Map<String, Object>> itemList = new ArrayList<>();
		for (BottomBarEntry entry : items) {
			Map<String, Object> map = new HashMap<>();
			map.put(ENTRY_ID, entry.id());
			map.put(ENTRY_LABEL, entry.label());
			map.put(ENTRY_ICON, entry.icon());
			if (entry.badge() != null) {
				map.put(ENTRY_BADGE, entry.badge());
			}
			itemList.add(map);
		}
		putState(ITEMS, itemList);
	}

	/**
	 * Sets the active item.
	 *
	 * @param activeItemId
	 *        The ID of the item to mark as active.
	 */
	public void setActiveItem(String activeItemId) {
		putState(ACTIVE_ITEM_ID, activeItemId);
	}

	/**
	 * A single entry in the bottom navigation bar.
	 *
	 * @param id
	 *        The entry identifier.
	 * @param label
	 *        The display label.
	 * @param icon
	 *        The CSS icon class.
	 * @param badge
	 *        Optional badge text, or {@code null}.
	 */
	public record BottomBarEntry(String id, String label, String icon, String badge) {

		/**
		 * Creates an entry without a badge.
		 *
		 * @param id
		 *        The entry identifier.
		 * @param label
		 *        The display label.
		 * @param icon
		 *        The CSS icon class.
		 */
		public BottomBarEntry(String id, String label, String icon) {
			this(id, label, icon, null);
		}
	}

	// -- Commands --

	/**
	 * Handles item selection from the client.
	 */
	@ReactCommand("selectItem")
	void handleSelectItem(Map<String, Object> arguments) {
		String itemId = (String) arguments.get(ITEM_ID_ARG);
		_selectHandler.accept(itemId);
	}

}
