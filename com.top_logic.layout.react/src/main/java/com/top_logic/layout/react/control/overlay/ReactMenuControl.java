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
import java.util.Objects;
import java.util.function.Function;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.react.state.MenuState;
import com.top_logic.layout.react.state.MenuState.EntryType;
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
			map.put(MenuState.Entry.TYPE__PROP, entry.type().protocolName());
			if (entry.type() == EntryType.HEADER) {
				map.put(MenuState.Entry.LABEL__PROP, entry.label());
			} else if (entry.type() == EntryType.ITEM) {
				map.put(MenuState.Entry.ID__PROP, entry.id());
				map.put(MenuState.Entry.LABEL__PROP, entry.label());
				if (entry.icon() != null) {
					map.put(MenuState.Entry.ICON__PROP, entry.icon());
				}
				if (entry.disabled()) {
					map.put(MenuState.Entry.DISABLED__PROP, true);
				}
				if (entry.active()) {
					map.put(MenuState.Entry.ACTIVE__PROP, true);
				}
				if (entry.cssClasses() != null) {
					map.put(MenuState.Entry.CSS_CLASSES__PROP, entry.cssClasses());
				}
				if (entry.tone() != ButtonTone.DEFAULT) {
					map.put(MenuState.Entry.TONE__PROP, entry.tone().getExternalName());
				}
			}
			itemList.add(map);
		}
		putState(MenuState.ITEMS__PROP, itemList);
	}

	/**
	 * Sets the anchor element ID for positioning.
	 */
	public void setAnchorId(String anchorId) {
		putState(MenuState.ANCHOR_ID__PROP, anchorId);
	}

	/**
	 * Opens the menu.
	 */
	public void open() {
		putState(MenuState.OPEN__PROP, true);
	}

	/**
	 * Opens the menu at the given viewport pixel coordinates.
	 *
	 * <p>
	 * Clears any previously set anchor element and positions the popup absolutely at {@code (x, y)}.
	 * </p>
	 */
	public void open(int x, int y) {
		putState(MenuState.ANCHOR_ID__PROP, null);
		putState(MenuState.ANCHOR_X__PROP, x);
		putState(MenuState.ANCHOR_Y__PROP, y);
		putState(MenuState.OPEN__PROP, true);
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
		putState(MenuState.OPEN__PROP, false);
	}

	/**
	 * A single entry in the popup menu.
	 *
	 * @param type
	 *        The entry type, one of {@link EntryType#ITEM}, {@link EntryType#SEPARATOR} and
	 *        {@link EntryType#HEADER}.
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
	 * @param tone
	 *        The kind of action the command this entry renders stands for; a
	 *        {@link ButtonTone#DANGER destructive} entry is drawn as such. Never {@code null}.
	 */
	public record MenuEntry(EntryType type, String id, String label, String icon, ExecutableState state,
			String cssClasses, boolean active, ButtonTone tone) {

		/**
		 * Creates a {@link MenuEntry}.
		 */
		public MenuEntry {
			Objects.requireNonNull(tone, "tone");
		}

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
			return item(id, label, null);
		}

		/**
		 * Creates a menu item with an icon.
		 */
		public static MenuEntry item(String id, String label, String icon) {
			return item(id, label, icon, false);
		}

		/**
		 * Creates a menu item with an icon and an explicit disabled state.
		 */
		public static MenuEntry item(String id, String label, String icon, boolean disabled) {
			return item(id, label, icon, disabled ? ExecutableState.NOT_EXEC_DISABLED : ExecutableState.EXECUTABLE,
				null, false);
		}

		/**
		 * Creates a menu item for an ordinary command in the given {@link MenuEntry#state() state},
		 * carrying additional CSS classes, marked as {@link MenuEntry#active() active} when its
		 * command is the one in force.
		 */
		public static MenuEntry item(String id, String label, String icon, ExecutableState state,
				String cssClasses, boolean active) {
			return item(id, label, icon, state, cssClasses, active, ButtonTone.DEFAULT);
		}

		/**
		 * Creates a menu item for a command of the given {@link MenuEntry#tone() tone}, see
		 * {@link #item(String, String, String, ExecutableState, String, boolean)}.
		 */
		public static MenuEntry item(String id, String label, String icon, ExecutableState state,
				String cssClasses, boolean active, ButtonTone tone) {
			return new MenuEntry(EntryType.ITEM, id, label, icon, state, cssClasses, active, tone);
		}

		/**
		 * Creates a separator.
		 */
		public static MenuEntry separator() {
			return new MenuEntry(EntryType.SEPARATOR, null, null, null, ExecutableState.EXECUTABLE, null, false,
				ButtonTone.DEFAULT);
		}

		/**
		 * Creates a header: a caption naming the entries that follow it, which cannot be selected
		 * or focused.
		 */
		public static MenuEntry header(String label) {
			return new MenuEntry(EntryType.HEADER, null, label, null, ExecutableState.EXECUTABLE, null, false,
				ButtonTone.DEFAULT);
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
