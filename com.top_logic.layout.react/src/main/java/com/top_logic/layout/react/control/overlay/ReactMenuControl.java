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

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

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

	private static final String ANCHOR_ID = "anchorId";

	private static final String OPEN = "open";

	private static final String ITEMS = "items";

	private static final String ENTRY_TYPE = "type";

	private static final String ENTRY_ID = "id";

	private static final String ENTRY_LABEL = "label";

	private static final String ENTRY_ICON = "icon";

	private static final String ENTRY_DISABLED = "disabled";

	private static final String ITEM_ID_ARG = "itemId";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new SelectItemCommand(), new CloseCommand());

	private final Consumer<String> _selectHandler;

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
	public ReactMenuControl(String anchorId, List<MenuEntry> items,
			Consumer<String> selectHandler, Runnable closeHandler) {
		super(null, REACT_MODULE, COMMANDS);
		_selectHandler = selectHandler;
		_closeHandler = closeHandler;
		putState(ANCHOR_ID, anchorId);
		putState(OPEN, false);
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
	 * Opens the menu.
	 */
	public void open() {
		putState(OPEN, true);
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
		 * Creates a separator.
		 */
		public static MenuEntry separator() {
			return new MenuEntry("separator", null, null, null, false);
		}
	}

	/**
	 * Command sent when a menu item is selected.
	 */
	public static class SelectItemCommand extends ControlCommand {

		/** Creates a {@link SelectItemCommand}. */
		public SelectItemCommand() {
			super("selectItem");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_MENU_SELECT;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactMenuControl menu = (ReactMenuControl) control;
			String itemId = (String) arguments.get(ITEM_ID_ARG);
			menu.close();
			menu._selectHandler.accept(itemId);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command sent when the menu is closed without selection.
	 */
	public static class CloseCommand extends ControlCommand {

		/** Creates a {@link CloseCommand}. */
		public CloseCommand() {
			super("close");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_MENU_CLOSE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactMenuControl menu = (ReactMenuControl) control;
			menu.close();
			menu._closeHandler.run();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
