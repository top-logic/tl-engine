/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Popup menu triggered by an anchor element.
 *
 * <p>
 * Positioned below the anchor via {@code getBoundingClientRect()} with fixed positioning. Flips if
 * near screen edge. Keyboard navigation: {@code ArrowUp}/{@code ArrowDown}, Enter, Escape. Closes
 * on outside click.
 * </p>
 */
public class ReactMenuControl extends ReactControl {

	private static final String REACT_MODULE = "TLMenu";

	/** @see #ReactMenuControl(ReactContext, String, List, Consumer, Runnable) */
	private static final String ANCHOR_ID = "anchorId";

	/** @see #open(int, int) */
	private static final String ANCHOR_X = "anchorX";

	/** @see #open(int, int) */
	private static final String ANCHOR_Y = "anchorY";

	/** @see #open() */
	private static final String OPEN = "open";

	/** @see #updateItems(List) */
	private static final String ITEMS = "items";

	/** Entry type discriminator ({@code "item"} or {@code "separator"}). */
	private static final String ENTRY_TYPE = "type";

	/** Entry identifier within the menu. */
	private static final String ENTRY_ID = "id";

	/** Entry display label. */
	private static final String ENTRY_LABEL = "label";

	/** Entry CSS icon class. */
	private static final String ENTRY_ICON = "icon";

	/** Whether the entry is disabled. */
	private static final String ENTRY_DISABLED = "disabled";

	/** Command argument: the selected item identifier. */
	private static final String ITEM_ID_ARG = "itemId";

	private Consumer<String> _selectHandler;

	private Runnable _closeHandler;

	/**
	 * Creates a popup menu.
	 *
	 * @param anchorId
	 *        the ID of the anchor DOM element
	 * @param items
	 *        the menu items
	 * @param selectHandler
	 *        called with the item ID when an item is selected
	 * @param closeHandler
	 *        called when the menu is closed
	 */
	public ReactMenuControl(ReactContext context, String anchorId, List<MenuEntry> items,
			Consumer<String> selectHandler, Runnable closeHandler) {
		super(context, null, REACT_MODULE);
		_selectHandler = selectHandler;
		_closeHandler = closeHandler;
		setAnchorId(anchorId);
		close();
		updateItems(items);
	}

	/**
	 * Updates the menu items.
	 */
	public void updateItems(List<MenuEntry> items) {
		List<Map<String, Object>> itemList = new ArrayList<>();
		for (MenuEntry entry : items) {
			Map<String, Object> map = new HashMap<>();
			map.put(ENTRY_TYPE, entry.type());
			if ("item".equals(entry.type())) {
				map.put(ENTRY_ID, entry.id());
				map.put(ENTRY_LABEL, entry.label());
				if (entry.icon() != null) {
					map.put(ENTRY_ICON, entry.icon());
				}
				if (entry.disabled()) {
					map.put(ENTRY_DISABLED, true);
				}
			}
			itemList.add(map);
		}
		putState(ITEMS, itemList);
	}

	/**
	 * Sets the anchor element ID for positioning.
	 */
	public void setAnchorId(String anchorId) {
		putState(ANCHOR_ID, anchorId);
	}

	/**
	 * Opens the menu.
	 */
	public void open() {
		putState(OPEN, true);
	}

	/**
	 * Opens the menu at the given viewport pixel coordinates.
	 *
	 * <p>
	 * Clears any previously set anchor element and positions the popup absolutely at {@code (x, y)}.
	 * </p>
	 */
	public void open(int x, int y) {
		putState(ANCHOR_ID, null);
		putState(ANCHOR_X, x);
		putState(ANCHOR_Y, y);
		putState(OPEN, true);
	}

	/**
	 * Sets the handler invoked when a menu item is selected.
	 */
	public void setSelectHandler(Consumer<String> selectHandler) {
		_selectHandler = selectHandler;
	}

	/**
	 * Sets the handler invoked when the menu is closed without selection.
	 */
	public void setCloseHandler(Runnable closeHandler) {
		_closeHandler = closeHandler;
	}

	/**
	 * Closes the menu.
	 */
	public void close() {
		putState(OPEN, false);
	}

	/**
	 * A single entry in the popup menu.
	 *
	 * @param type
	 *        The entry type: {@code "item"} or {@code "separator"}.
	 * @param id
	 *        The item identifier (may be {@code null} for separators).
	 * @param label
	 *        The display label (may be {@code null} for separators).
	 * @param icon
	 *        An optional CSS icon class, or {@code null}.
	 * @param disabled
	 *        Whether the item is disabled.
	 */
	public record MenuEntry(String type, String id, String label, String icon, boolean disabled) {

		/**
		 * Creates a simple menu item.
		 */
		public static MenuEntry item(String id, String label) {
			return new MenuEntry("item", id, label, null, false);
		}

		/**
		 * Creates a menu item with an icon.
		 */
		public static MenuEntry item(String id, String label, String icon) {
			return new MenuEntry("item", id, label, icon, false);
		}

		/**
		 * Creates a menu item with an icon and an explicit disabled state.
		 */
		public static MenuEntry item(String id, String label, String icon, boolean disabled) {
			return new MenuEntry("item", id, label, icon, disabled);
		}

		/**
		 * Creates a separator.
		 */
		public static MenuEntry separator() {
			return new MenuEntry("separator", null, null, null, false);
		}
	}

	/**
	 * Handles the selectItem command sent when a menu item is selected.
	 */
	@ReactCommand("selectItem")
	void handleSelectItem(Map<String, Object> arguments) {
		String itemId = (String) arguments.get(ITEM_ID_ARG);
		close();
		_selectHandler.accept(itemId);
	}

	/**
	 * Handles the close command sent when the menu is closed without selection.
	 */
	@ReactCommand("close")
	void handleClose() {
		close();
		_closeHandler.run();
	}

}
