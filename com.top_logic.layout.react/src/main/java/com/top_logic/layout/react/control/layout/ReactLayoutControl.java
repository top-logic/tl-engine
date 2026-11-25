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

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.identity.DisplayedObjectId;
import com.top_logic.layout.react.control.identity.ObjectIdentities;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.control.selection.ItemSelection;
import com.top_logic.layout.react.control.selection.ItemSelector;
import com.top_logic.table.SelectionMode;

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
 * A container whose children each display an object can be made activatable
 * ({@link #setActivation(ItemActivation, boolean)}): the user opens an item, and the
 * {@link ItemActivation activation} runs with the item's object. How an item is opened depends on
 * whether the container is selectable: in a container that is not, a click on the item opens it, as
 * {@code Enter} or {@code Space} on the focused item do; in a selectable one, a click selects the item,
 * and a double-click, {@code Enter} or the item's open button opens it, after making it the
 * selection. The children of an activatable container that is not selectable are handed over
 * together with their objects as well ({@link #showItems(List)}).
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
 * <p>
 * An activatable container in addition:
 * </p>
 * <ul>
 * <li>{@link #ITEM_KEYS}: as above.</li>
 * <li>{@link #ITEM_ACTIONS}: the {@link ItemAction#toState() action} of each child, {@code null} for
 * a child that cannot be activated; aligned with {@link #CHILDREN}.</li>
 * <li>{@link #ACTIVATION_BUTTON}: whether the items of a selectable container show an open
 * button.</li>
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
	 * State key of the list of the {@link ItemAction#toState() actions} of the items, aligned with
	 * {@link #CHILDREN}; present while the container is activatable, see
	 * {@link #setActivation(ItemActivation, boolean)}.
	 */
	public static final String ITEM_ACTIONS = "itemActions";

	/**
	 * State key telling the client whether each item of a selectable, activatable container shows
	 * an open button.
	 */
	public static final String ACTIVATION_BUTTON = "activationButton";

	/** Command activating an item by its key, see {@link ActivateItemArguments}. */
	public static final String CMD_ACTIVATE_ITEM = "activateItem";

	/**
	 * A child of a selectable or activatable container together with the object it displays.
	 *
	 * @param value
	 *        The displayed object, which becomes the selection when the item is clicked, and
	 *        which an activation of the item runs with.
	 * @param content
	 *        The child control displaying the object.
	 */
	public record Item(Object value, ReactControl content) {
		// Pure value.
	}

	private final List<ReactControl> _children;

	/**
	 * The client keys of the displayed objects and the selection among them; {@code null} while the
	 * children are displayed without their objects.
	 */
	private ItemSelection _selection;

	/** Whether the items are selected by a click, see {@link #setItems(List)}. */
	private boolean _selectable;

	/** What activating an item runs, {@code null} for a container whose items are not activatable. */
	private ItemActivation _activation;

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
	 *         If the container displays its children together with their objects.
	 */
	public void setChildren(List<? extends ReactControl> children) {
		if (hasItems()) {
			throw new IllegalStateException(
				"The container displays its children together with their objects, see setItems().");
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
		if (!_selectable) {
			_selectable = true;
			putState(SELECTABLE, Boolean.TRUE);
			putState(MULTI_SELECT, Boolean.valueOf(selection().isMulti()));
		}
		showItems(items);
	}

	/**
	 * Replaces the displayed children by the contents of the given items, each displayed together
	 * with its object, without making the container {@link #isSelectable() selectable}.
	 *
	 * <p>
	 * This is how the children of an {@link #setActivation(ItemActivation, boolean) activatable}
	 * container are displayed: the object of an item is what activating it runs with. A container
	 * made selectable by {@link #setItems(List)} stays selectable.
	 * </p>
	 *
	 * <p>
	 * An object that was displayed before keeps its client key, see {@link #setItems(List)}.
	 * </p>
	 *
	 * @param items
	 *        The items to display in display order, each object at most once.
	 */
	public void showItems(List<Item> items) {
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

		replaceChildren(contents);
		putState(ITEM_KEYS, keys);
		if (_selectable) {
			putState(SELECTED, selection.selectedKeys());
		}
		publishActions();
	}

	/**
	 * Whether the children are displayed together with their objects, see {@link #setItems(List)}
	 * and {@link #showItems(List)}.
	 */
	public boolean hasItems() {
		return _selection != null;
	}

	/**
	 * Whether the items of this container are selected by a click, see {@link #setItems(List)}.
	 */
	public boolean isSelectable() {
		return _selectable;
	}

	/**
	 * Makes the items of this container activatable: the user opens an item, and the given
	 * activation runs with its object.
	 *
	 * <p>
	 * The children are displayed together with their objects, by {@link #setItems(List)} for a
	 * selectable container and by {@link #showItems(List)} otherwise.
	 * </p>
	 *
	 * @param activation
	 *        What activating an item runs, deciding per object whether it can be activated.
	 * @param button
	 *        Whether each item of a {@link #isSelectable() selectable} container shows an open
	 *        button: there, a click selects the item, and the gestures opening it - a double-click,
	 *        {@code Enter} on the focused item - are neither visible nor available on a touch
	 *        device. In a container that is not selectable, the click opens the item, and no item
	 *        shows a button.
	 */
	public void setActivation(ItemActivation activation, boolean button) {
		_activation = activation;
		putState(ACTIVATION_BUTTON, Boolean.valueOf(button));
		publishActions();
	}

	/**
	 * Whether the items of this container are activatable, see
	 * {@link #setActivation(ItemActivation, boolean)}.
	 */
	public boolean isActivatable() {
		return _activation != null;
	}

	/**
	 * Publishes again what activating each item offers, after the
	 * {@link #setActivation(ItemActivation, boolean) activation} reported a change.
	 */
	public void updateActivation() {
		publishActions();
	}

	private void publishActions() {
		if (_activation == null || _selection == null) {
			return;
		}
		List<Object> values = _selection.getItems();
		List<Map<String, Object>> actions = new ArrayList<>(values.size());
		for (Object value : values) {
			ItemAction action = _activation.action(value);
			actions.add(action == null ? null : action.toState());
		}
		putState(ITEM_ACTIONS, actions);
	}

	private ItemSelection selection() {
		if (_selection == null) {
			_selection = new ItemSelection(getReactContext()::allocateId, keys -> putState(SELECTED, keys));
		}
		return _selection;
	}

	private ItemSelection requireSelection() {
		if (!_selectable) {
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
		return _selectable ? _selection.getMode() : SelectionMode.SINGLE;
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
		return _selectable ? _selection.getSelection() : Set.of();
	}

	/**
	 * Whether the container displays an item for the given object, see {@link #setItems(List)}.
	 */
	@Override
	public boolean displays(Object item) {
		return _selectable && _selection.displays(item);
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
	void handleSelectItem(SelectItemArguments args) {
		if (_selectable) {
			_selection.select(args.getKey(), args.isToggle(), args.isRange());
		}
	}

	/**
	 * Activates the item with the given key, as the user opens it.
	 *
	 * <p>
	 * In a {@link #isSelectable() selectable} container, the item becomes the selection first, then
	 * the {@link #setActivation(ItemActivation, boolean) activation} runs with its object - so that
	 * whatever the activation reads from the selection is that object. A report the
	 * {@link #setSelectionListener(Consumer) selection listener} refuses propagates, and the
	 * activation does not run.
	 * </p>
	 *
	 * <p>
	 * An item the activation offers no action for is not activatable: its activation changes
	 * nothing, neither the selection. A key that names no displayed item - the item went away while
	 * the gesture was on its way - changes nothing, and so does an activation in a container that
	 * is not {@link #isActivatable() activatable}.
	 * </p>
	 *
	 * @see ItemActivation#activate(Object)
	 */
	@ReactCommandHandler(CMD_ACTIVATE_ITEM)
	void handleActivateItem(ActivateItemArguments args) {
		if (_activation == null || _selection == null) {
			return;
		}
		Object item = _selection.item(args.getKey());
		if (item == null || _activation.action(item) == null) {
			return;
		}
		if (_selectable) {
			_selection.select(item, false, false);
		}
		_activation.activate(item);
	}

	/**
	 * The identities of the displayed items: the client key {@link #CMD_SELECT_ITEM} and
	 * {@link #CMD_ACTIVATE_ITEM} take an item by; {@code null} while the children are displayed
	 * without their objects.
	 *
	 * <p>
	 * The keys of the items are the global business keys of their objects. A key resolves only to
	 * a displayed item.
	 * </p>
	 *
	 * @see DisplayedObjectId
	 */
	@Override
	public ObjectIdentities objectIdentities() {
		return _selection == null ? null : _selection.identities();
	}

	/**
	 * The selection command, while the container is not {@link #isSelectable() selectable}, and the
	 * activation command, while it is not {@link #isActivatable() activatable}.
	 */
	@Override
	protected Set<String> scriptingHiddenCommands() {
		if (isSelectable() && isActivatable()) {
			return super.scriptingHiddenCommands();
		}
		Set<String> result = new LinkedHashSet<>(super.scriptingHiddenCommands());
		if (!isSelectable()) {
			result.add(CMD_SELECT_ITEM);
		}
		if (!isActivatable()) {
			result.add(CMD_ACTIVATE_ITEM);
		}
		return result;
	}

	/**
	 * Wraps each child in an element of the given CSS class, carrying the 0-based position of the
	 * child as the CSS custom property {@code --item-index}.
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
		return presentationKeys(super.scriptingPresentationKeys(), ITEM_CLASS, MAX_WIDTH, ACTIVATION_BUTTON);
	}

	/**
	 * Projects the items of a container displaying them with their objects, and the selection of a
	 * {@link #isSelectable() selectable} container.
	 *
	 * <p>
	 * Each of the {@link #ITEM_KEYS} becomes an entry identifying its item: in the
	 * {@link ProjectionMode#AGENT agent projection} by the client key {@link #CMD_SELECT_ITEM} takes
	 * under {@link ScriptingModelKey#ID}, together with the label of the item's object under
	 * {@link ScriptingModelKey#LABEL}; in the {@link ProjectionMode#KEY key projection} by the
	 * business key of the object under {@link ScriptingModelKey#KEY}. The
	 * {@link #SELECTED} keys are the client keys in the agent projection and the business keys in
	 * the key projection.
	 * </p>
	 */
	@Override
	public Map<String, Object> scriptingScalarState(ProjectionMode mode) {
		Map<String, Object> result = super.scriptingScalarState(mode);
		if (_selection != null) {
			ObjectIdentities identities = objectIdentities();
			_selection.identifyItems(result, mode, identities, ITEM_KEYS);
			if (_selectable) {
				_selection.identifyKeys(result, mode, identities, SELECTED);
			}
		}
		return result;
	}

}
