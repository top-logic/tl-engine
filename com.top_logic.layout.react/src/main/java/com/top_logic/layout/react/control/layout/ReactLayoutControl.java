/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.selection.ItemSelection;
import com.top_logic.layout.react.control.selection.ItemSelector;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.table.SelectionMode;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactControl} arranging a list of children.
 *
 * <p>
 * What a layout container contributes is the placement of its children: the flow of a
 * {@link ReactStackControl stack}, the columns of a {@link ReactGridControl grid}. The children
 * themselves come from outside and are exchanged while the container is displayed, whenever the
 * model the display follows changes, so the list of children and the wrapper each child is placed
 * in are common to every layout.
 * </p>
 *
 * <p>
 * A container whose children each display an object is made selectable by handing it the children
 * together with their objects ({@link #setItems(List)}). Clicking an item then makes its object the
 * {@link #getSelection() selection} and reports it to the {@link #setSelectionListener(Consumer)
 * selection listener}; the selected items are highlighted. The selection behaves like the one of a
 * kanban board, see {@link ItemSelection}: in the {@link #setSelectionMode(SelectionMode) selection
 * mode} {@link SelectionMode#MULTI}, a click that {@link SelectItemArguments#isToggle() toggles} adds
 * an item to the selection or takes it out again, and one that {@link SelectItemArguments#isRange()
 * extends} the selection adds the items from the one clicked last up to the clicked one.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@link #CHILDREN}: the child controls in display order.</li>
 * <li>{@link #ITEM_CLASS}, {@link #MAX_WIDTH}: the wrapper class of each child and the width
 * bound.</li>
 * </ul>
 * <p>
 * A selectable container in addition:
 * </p>
 * <ul>
 * <li>{@link #SELECTABLE}: {@code true}.</li>
 * <li>{@link #ITEM_KEYS}: the client key of each child's object, aligned with {@link #CHILDREN}.</li>
 * <li>{@link #SELECTED}: the keys of the displayed selected items.</li>
 * <li>{@link #MULTI_SELECT}: whether several items may be selected.</li>
 * </ul>
 */
public abstract class ReactLayoutControl extends ReactControl implements ItemSelector {

	/** @see #setChildren(List) */
	protected static final String CHILDREN = "children";

	/** @see #setItemClass(String) */
	protected static final String ITEM_CLASS = "itemClass";

	/** @see #setMaxWidth(String) */
	protected static final String MAX_WIDTH = "maxWidth";

	/** State key telling the client that its items are selectable, see {@link #setItems(List)}. */
	public static final String SELECTABLE = "selectable";

	/**
	 * State key of the list of the client keys of the items' objects, aligned with
	 * {@link #CHILDREN}; present while the container is {@link #SELECTABLE}.
	 */
	public static final String ITEM_KEYS = "itemKeys";

	/** State key of the list of the keys of the selected items. */
	public static final String SELECTED = "selected";

	/** State key telling the client whether several items may be selected. */
	public static final String MULTI_SELECT = "multiSelect";

	/** Command selecting an item by its key, see {@link SelectItemArguments}. */
	public static final String CMD_SELECT_ITEM = "selectItem";

	/**
	 * Command selecting an item by the business identity of its object, see
	 * {@link SelectItemByKeyArguments}.
	 */
	public static final String CMD_SELECT_ITEM_BY_KEY = "selectItemByKey";

	/**
	 * A child of a selectable container together with the object it displays.
	 *
	 * @param value
	 *        The displayed object, which becomes the selection when the item is clicked.
	 * @param content
	 *        The child control displaying the object.
	 */
	public record Item(Object value, ReactControl content) {
		// Pure value.
	}

	private final List<ReactControl> _children;

	/**
	 * The client keys of the displayed objects and the selection among them; {@code null} while the
	 * container is not {@link #isSelectable() selectable}.
	 */
	private ItemSelection _selection;

	/**
	 * Creates a {@link ReactLayoutControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param reactModule
	 *        The React module identifier rendering the layout on the client.
	 * @param children
	 *        The child controls to arrange.
	 */
	protected ReactLayoutControl(ReactContext context, String reactModule, List<? extends ReactControl> children) {
		super(context, null, reactModule);
		_children = new ArrayList<>(children);
		putState(CHILDREN, _children);
	}

	/**
	 * Replaces the displayed children.
	 *
	 * <p>
	 * A dropped child is not cleaned up automatically, since callers may re-add it later (e.g. an
	 * unchanged item in a refreshed list). Callers that remove a child for good must call
	 * {@link #cleanupTree()} on it themselves.
	 * </p>
	 *
	 * <p>
	 * A {@link #isSelectable() selectable} container displays its children together with their
	 * objects, see {@link #setItems(List)}.
	 * </p>
	 *
	 * @param children
	 *        The new child controls, replacing the current ones.
	 * @throws IllegalStateException
	 *         If the container is selectable.
	 */
	public void setChildren(List<? extends ReactControl> children) {
		if (isSelectable()) {
			throw new IllegalStateException(
				"A selectable container displays its children together with their objects, see setItems().");
		}
		replaceChildren(children);
	}

	private void replaceChildren(List<? extends ReactControl> children) {
		_children.clear();
		_children.addAll(children);
		putState(CHILDREN, new ArrayList<>(_children));
	}

	/**
	 * Replaces the displayed children by the contents of the given items, and makes the container
	 * selectable over the items' objects.
	 *
	 * <p>
	 * An object that was displayed before keeps its client key, so the client reuses what it renders
	 * for it. The selection is kept, also of objects no longer displayed. A content that is no longer
	 * displayed is not cleaned up, see {@link #setChildren(List)}.
	 * </p>
	 *
	 * @param items
	 *        The items to display in display order, each object at most once.
	 */
	public void setItems(List<Item> items) {
		boolean becomesSelectable = !isSelectable();
		ItemSelection selection = selection();

		List<Object> values = new ArrayList<>(items.size());
		List<ReactControl> contents = new ArrayList<>(items.size());
		for (Item item : items) {
			values.add(item.value());
			contents.add(item.content());
		}
		selection.setItems(values);

		List<String> keys = new ArrayList<>(values.size());
		for (Object value : values) {
			keys.add(selection.keyOf(value));
		}

		if (becomesSelectable) {
			putState(SELECTABLE, Boolean.TRUE);
			putState(MULTI_SELECT, Boolean.valueOf(selection.isMulti()));
		}
		replaceChildren(contents);
		putState(ITEM_KEYS, keys);
		putState(SELECTED, selection.selectedKeys());
	}

	/**
	 * Whether the items of this container are selected by a click, see {@link #setItems(List)}.
	 */
	public boolean isSelectable() {
		return _selection != null;
	}

	private ItemSelection selection() {
		if (_selection == null) {
			_selection = new ItemSelection(getReactContext()::allocateId, keys -> putState(SELECTED, keys));
		}
		return _selection;
	}

	private ItemSelection requireSelection() {
		if (_selection == null) {
			throw new IllegalStateException(
				"The container is not selectable, its items are set with their objects by setItems().");
		}
		return _selection;
	}

	/**
	 * Whether one item or several may be selected.
	 */
	@Override
	public SelectionMode getSelectionMode() {
		return _selection == null ? SelectionMode.SINGLE : _selection.getMode();
	}

	/**
	 * Sets whether one item or several may be selected.
	 *
	 * @throws IllegalStateException
	 *         If the container is not {@link #isSelectable() selectable}.
	 */
	public void setSelectionMode(SelectionMode mode) {
		ItemSelection selection = requireSelection();
		selection.setMode(mode);
		putState(MULTI_SELECT, Boolean.valueOf(selection.isMulti()));
	}

	/**
	 * The selected objects, empty if there are none or the container is not {@link #isSelectable()
	 * selectable}.
	 *
	 * <p>
	 * The selection is kept while an object's item is not displayed, and highlighted again once it
	 * is.
	 * </p>
	 */
	@Override
	public Set<Object> getSelection() {
		return _selection == null ? Set.of() : _selection.getSelection();
	}

	/**
	 * Whether the container displays an item for the given object, see {@link #setItems(List)}.
	 */
	@Override
	public boolean displays(Object item) {
		return _selection != null && _selection.displays(item);
	}

	/**
	 * Highlights the items of the given objects, without reporting them to the
	 * {@link #setSelectionListener(Consumer) selection listener}.
	 *
	 * @param items
	 *        The selected objects, empty for no selection. In the selection mode
	 *        {@link SelectionMode#SINGLE}, the caller passes at most one.
	 * @throws IllegalStateException
	 *         If the container is not {@link #isSelectable() selectable}.
	 */
	@Override
	public void setSelection(Collection<?> items) {
		requireSelection().setSelection(items);
	}

	/**
	 * Sets the listener informed when the user changes the selection.
	 *
	 * @param listener
	 *        Receives the selected objects; {@code null} for no listener.
	 * @throws IllegalStateException
	 *         If the container is not {@link #isSelectable() selectable}.
	 */
	@Override
	public void setSelectionListener(Consumer<Set<Object>> listener) {
		requireSelection().setListener(listener);
	}

	/**
	 * Selects the item with the given key, as the user clicks it.
	 *
	 * <p>
	 * A key that names no displayed item - the item went away while the click was on its way -
	 * changes nothing, and so does a click on a container that is not {@link #isSelectable()
	 * selectable}. A report the {@link #setSelectionListener(Consumer) selection listener} refuses -
	 * unsaved changes the selection would replace - restores the selection displayed before and
	 * propagates.
	 * </p>
	 *
	 * @see ItemSelection#select(String, boolean, boolean)
	 */
	@ReactCommandHandler(CMD_SELECT_ITEM)
	HandlerResult handleSelectItem(SelectItemArguments args) {
		if (_selection != null) {
			_selection.select(args.getKey(), args.isToggle(), args.isRange());
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Selects the item of the object named by the given {@link ModelName key} - the replay-stable
	 * counterpart of {@link #handleSelectItem(SelectItemArguments)}.
	 *
	 * <p>
	 * A recorded key that designates no displayed item is an explicit failure, never a silent no-op.
	 * </p>
	 */
	@ReactCommandHandler(CMD_SELECT_ITEM_BY_KEY)
	HandlerResult handleSelectItemByKey(SelectItemByKeyArguments args) {
		if (_selection == null) {
			return HandlerResult.error(I18NConstants.ERROR_ITEM_KEY_UNRESOLVED__KEY.fill(args.getKey()));
		}
		return _selection.selectByName(args, I18NConstants.ERROR_ITEM_KEY_UNRESOLVED__KEY);
	}

	/**
	 * Records an item selection by the business identity of the item's object, since the client keys
	 * are allocated per session.
	 */
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (CMD_SELECT_ITEM.equals(command) && _selection != null) {
			RecordedCommand recorded = _selection.recordByName(arguments, SelectItemArguments.KEY,
				SelectItemByKeyArguments.class, CMD_SELECT_ITEM_BY_KEY);
			if (recorded != null) {
				return recorded;
			}
		}
		return super.recordCommand(command, arguments);
	}

	/**
	 * The selection commands, while the container is not {@link #isSelectable() selectable}.
	 */
	@Override
	protected Set<String> scriptingHiddenCommands() {
		if (isSelectable()) {
			return super.scriptingHiddenCommands();
		}
		Set<String> result = new LinkedHashSet<>(super.scriptingHiddenCommands());
		result.add(CMD_SELECT_ITEM);
		result.add(CMD_SELECT_ITEM_BY_KEY);
		return result;
	}

	/**
	 * Wraps each child in an element of the given CSS class, carrying the 0-based position of the
	 * child as the CSS custom property {@code --tl-item-index}.
	 *
	 * <p>
	 * A stylesheet composes a per-item value from that position, a staggered entrance animation
	 * being the case it is meant for. Without a class, the children are placed in the container
	 * directly.
	 * </p>
	 *
	 * @param itemClass
	 *        The CSS class of the wrapper around each child, or {@code null} for no wrapper.
	 */
	public void setItemClass(String itemClass) {
		putState(ITEM_CLASS, itemClass);
	}

	/**
	 * Limits the container to the given width and centers it in the space it is given.
	 *
	 * <p>
	 * The bound is what keeps a column of running text readable on a wide screen: the container
	 * takes the width offered up to the bound, and the space left over is split between its two
	 * sides. Below the bound nothing changes, so the same container still fills a narrow screen.
	 * </p>
	 *
	 * @param maxWidth
	 *        The largest width the container takes, as a CSS length such as {@code "60rem"}, or
	 *        {@code null} for the width its container offers.
	 */
	public void setMaxWidth(String maxWidth) {
		putState(MAX_WIDTH, maxWidth);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), ITEM_CLASS, MAX_WIDTH);
	}

}
