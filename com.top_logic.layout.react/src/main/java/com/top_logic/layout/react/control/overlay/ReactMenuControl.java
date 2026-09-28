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
import java.util.function.Function;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

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

	/** @see #ReactMenuControl(ReactContext, String, List, Function, Runnable) */
	private static final String ANCHOR_ID = "anchorId";

	/** @see #open(int, int) */
	private static final String ANCHOR_X = "anchorX";

	/** @see #open(int, int) */
	private static final String ANCHOR_Y = "anchorY";

	/** @see #open() */
	private static final String OPEN = "open";

	/** @see #updateItems(List) */
	private static final String ITEMS = "items";

	/**
	 * Entry type discriminator, one of {@link #ENTRY_TYPE_ITEM}, {@link #ENTRY_TYPE_SEPARATOR} and
	 * {@link #ENTRY_TYPE_HEADER}.
	 */
	private static final String ENTRY_TYPE = "type";

	/** {@link #ENTRY_TYPE} of a selectable entry. */
	private static final String ENTRY_TYPE_ITEM = "item";

	/** {@link #ENTRY_TYPE} of a divider between groups of entries. */
	private static final String ENTRY_TYPE_SEPARATOR = "separator";

	/** {@link #ENTRY_TYPE} of a caption naming the group of entries beneath it; not selectable. */
	private static final String ENTRY_TYPE_HEADER = "header";

	/** Entry identifier within the menu. */
	private static final String ENTRY_ID = "id";

	/** Entry display label. */
	private static final String ENTRY_LABEL = "label";

	/** Entry CSS icon class. */
	private static final String ENTRY_ICON = "icon";

	/** Whether the entry is disabled. */
	private static final String ENTRY_DISABLED = "disabled";

	/** Whether the entry renders a command whose effect is in force, see {@link MenuEntry#active()}. */
	private static final String ENTRY_ACTIVE = "active";

	/** Additional CSS classes for the entry. */
	private static final String ENTRY_CSS_CLASSES = "cssClasses";

	/** The {@link ReactCommandHandler} that selects a menu item. */
	public static final String SELECT_ITEM_COMMAND = "selectItem";

	private Function<String, HandlerResult> _selectHandler;

	private Runnable _closeHandler;

	private List<MenuEntry> _entries = List.of();

	/**
	 * Creates a popup menu.
	 *
	 * @param anchorId
	 *        the ID of the anchor DOM element
	 * @param items
	 *        the menu items
	 * @param selectHandler
	 *        called with the item ID when an item is selected, see
	 *        {@link #setSelectHandler(Function)}
	 * @param closeHandler
	 *        called when the menu is closed
	 */
	public ReactMenuControl(ReactContext context, String anchorId, List<MenuEntry> items,
			Function<String, HandlerResult> selectHandler, Runnable closeHandler) {
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
		_entries = List.copyOf(items);
		List<Map<String, Object>> itemList = new ArrayList<>();
		for (MenuEntry entry : items) {
			Map<String, Object> map = new HashMap<>();
			map.put(ENTRY_TYPE, entry.type());
			if (ENTRY_TYPE_HEADER.equals(entry.type())) {
				map.put(ENTRY_LABEL, entry.label());
			} else if (ENTRY_TYPE_ITEM.equals(entry.type())) {
				map.put(ENTRY_ID, entry.id());
				map.put(ENTRY_LABEL, entry.label());
				if (entry.icon() != null) {
					map.put(ENTRY_ICON, entry.icon());
				}
				if (entry.disabled()) {
					map.put(ENTRY_DISABLED, true);
				}
				if (entry.active()) {
					map.put(ENTRY_ACTIVE, true);
				}
				if (entry.cssClasses() != null) {
					map.put(ENTRY_CSS_CLASSES, entry.cssClasses());
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
	 *
	 * <p>
	 * The handler receives the item ID and returns the result of what the selection does - the
	 * result of the command the entry stands for, a refusal included -, which is reported as the
	 * result of the selection.
	 * </p>
	 */
	public void setSelectHandler(Function<String, HandlerResult> selectHandler) {
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
	 *        The entry type, one of {@link #ENTRY_TYPE_ITEM}, {@link #ENTRY_TYPE_SEPARATOR} and
	 *        {@link #ENTRY_TYPE_HEADER}.
	 * @param id
	 *        The item identifier ({@code null} for separators and headers).
	 * @param label
	 *        The display label ({@code null} for separators).
	 * @param icon
	 *        An optional CSS icon class, or {@code null}.
	 * @param state
	 *        Whether the item is offered for selection; an item that is not
	 *        {@link ExecutableState#isExecutable() executable} is displayed as {@link #disabled()
	 *        disabled}, and selecting it is refused with this state, whose
	 *        {@link ExecutableState#getI18NReasonKey() reason} tells the user why.
	 * @param cssClasses
	 *        Additional CSS classes for the entry, separated by spaces, or {@code null}.
	 * @param active
	 *        Whether the effect of the command this entry renders is currently in force, so that
	 *        the entry is marked as the chosen one among its alternatives.
	 */
	public record MenuEntry(String type, String id, String label, String icon, ExecutableState state,
			String cssClasses, boolean active) {

		/**
		 * Whether the item is displayed as disabled, see {@link #state()}.
		 */
		public boolean disabled() {
			return !state.isExecutable();
		}

		/**
		 * Creates a simple menu item.
		 */
		public static MenuEntry item(String id, String label) {
			return new MenuEntry(ENTRY_TYPE_ITEM, id, label, null, ExecutableState.EXECUTABLE, null, false);
		}

		/**
		 * Creates a menu item with an icon.
		 */
		public static MenuEntry item(String id, String label, String icon) {
			return new MenuEntry(ENTRY_TYPE_ITEM, id, label, icon, ExecutableState.EXECUTABLE, null, false);
		}

		/**
		 * Creates a menu item with an icon and an explicit disabled state.
		 */
		public static MenuEntry item(String id, String label, String icon, boolean disabled) {
			return new MenuEntry(ENTRY_TYPE_ITEM, id, label, icon,
				disabled ? ExecutableState.NOT_EXEC_DISABLED : ExecutableState.EXECUTABLE, null, false);
		}

		/**
		 * Creates a menu item for a command in the given {@link MenuEntry#state() state}, carrying
		 * additional CSS classes, marked as {@link MenuEntry#active() active} when its command is
		 * the one in force.
		 */
		public static MenuEntry item(String id, String label, String icon, ExecutableState state,
				String cssClasses, boolean active) {
			return new MenuEntry(ENTRY_TYPE_ITEM, id, label, icon, state, cssClasses, active);
		}

		/**
		 * Creates a separator.
		 */
		public static MenuEntry separator() {
			return new MenuEntry(ENTRY_TYPE_SEPARATOR, null, null, null, ExecutableState.EXECUTABLE, null, false);
		}

		/**
		 * Creates a header: a caption naming the entries that follow it, which cannot be selected
		 * or focused.
		 */
		public static MenuEntry header(String label) {
			return new MenuEntry(ENTRY_TYPE_HEADER, null, label, null, ExecutableState.EXECUTABLE, null, false);
		}
	}

	/**
	 * Handles the selectItem command sent when a menu item is selected.
	 *
	 * <p>
	 * An item that is displayed as {@link MenuEntry#disabled() disabled} is not selected, whatever
	 * the client sends: the selection is refused with the item's {@link MenuEntry#state() state}.
	 * Otherwise the menu closes and the {@link #setSelectHandler(Function) select handler}'s result
	 * is the result of the selection.
	 * </p>
	 */
	@ReactCommandHandler(SELECT_ITEM_COMMAND)
	HandlerResult handleSelectItem(MenuSelectItemArguments args) {
		String itemId = args.getItemId();
		ExecutableState state = stateOf(itemId);
		if (!state.isExecutable()) {
			return HandlerResult.notExecutable(state);
		}
		close();
		return _selectHandler.apply(itemId);
	}

	/**
	 * The {@link MenuEntry#state() state} of the entry with the given id, whether selecting it is
	 * offered.
	 */
	private ExecutableState stateOf(String itemId) {
		for (MenuEntry entry : _entries) {
			if (entry.id() != null && entry.id().equals(itemId)) {
				return entry.state();
			}
		}
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * Handles the close command sent when the menu is closed without selection.
	 */
	@ReactCommandHandler(value = "close", technical = true)
	void handleClose() {
		close();
		_closeHandler.run();
	}

}
