/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.table.SelectionMode;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The selection among the objects a control displays as items, each under a client key.
 *
 * <p>
 * The control hands the displayed objects over in display order ({@link #setItems(Collection)}); an
 * object keeps its {@link #keyOf(Object) client key} across updates for as long as it is displayed,
 * so the client reuses what it renders for it. The {@link #getSelection() selection} is a set of
 * objects, also of objects that are not displayed at the moment; the {@link #selectedKeys() keys}
 * name those that are.
 * </p>
 *
 * <p>
 * A user selection ({@link #select(String, boolean, boolean)}) is reported to the
 * {@link #setListener(Consumer) listener}; a programmatic one ({@link #setSelection(Collection)}) is
 * not. In the {@link #setMode(SelectionMode) selection mode} {@link SelectionMode#MULTI}, a
 * selection that toggles adds an item to the selection or takes it out again, and one that extends
 * adds the items from the one selected last up to the selected one, in display order.
 * </p>
 *
 * <p>
 * The control owning a selection publishes it: it passes a {@link #ItemSelection(Supplier, Consumer)
 * publisher} receiving the selected keys whenever the selection changes, and publishes
 * {@link #selectedKeys()} itself after {@link #setItems(Collection) displaying other items}. For the
 * recording of a selection by the business identity of the selected object, see
 * {@link #recordByName(Map, String, Class, String)} and {@link #selectByName(SelectByNameArguments,
 * ResKey1)}.
 * </p>
 */
public class ItemSelection {

	private final Supplier<String> _keyAllocator;

	private final Consumer<List<String>> _publisher;

	/** Client keys of the displayed objects. */
	private Map<Object, String> _keys = new HashMap<>();

	/** Displayed objects by their client key. */
	private final Map<String, Object> _itemsByKey = new HashMap<>();

	/** The displayed objects in display order. */
	private List<Object> _items = List.of();

	/** Whether one item or several may be selected. */
	private SelectionMode _mode = SelectionMode.SINGLE;

	/** The selected objects, also those that are not displayed. */
	private final Set<Object> _selection = new LinkedHashSet<>();

	/**
	 * The object selected last without extending the selection, also when the selection took it out
	 * of the selection: where a range starts.
	 */
	private Object _anchor;

	private Consumer<Set<Object>> _listener;

	/**
	 * Creates an {@link ItemSelection} displaying no items.
	 *
	 * @param keyAllocator
	 *        Allocates the client key of an object that is displayed and has none.
	 * @param publisher
	 *        Receives the {@link #selectedKeys() keys of the displayed selected objects} whenever the
	 *        selection changes.
	 */
	public ItemSelection(Supplier<String> keyAllocator, Consumer<List<String>> publisher) {
		_keyAllocator = keyAllocator;
		_publisher = publisher;
	}

	/**
	 * Sets the displayed objects.
	 *
	 * <p>
	 * An object that was displayed before keeps its client key, one that is no longer displayed
	 * loses it. The selection is kept. The caller publishes the {@link #selectedKeys()} afterwards,
	 * since they may have changed.
	 * </p>
	 *
	 * @param items
	 *        The displayed objects in display order.
	 */
	public void setItems(Collection<?> items) {
		Map<Object, String> keys = new HashMap<>();
		_itemsByKey.clear();
		for (Object item : items) {
			String key = keys.computeIfAbsent(item, this::keyFor);
			_itemsByKey.put(key, item);
		}
		_keys = keys;
		_items = List.copyOf(items);
	}

	private String keyFor(Object item) {
		String existing = _keys.get(item);
		return existing != null ? existing : _keyAllocator.get();
	}

	/**
	 * The displayed objects in display order.
	 */
	public List<Object> getItems() {
		return _items;
	}

	/**
	 * The client key of the given object, {@code null} if it is not displayed.
	 */
	public String keyOf(Object item) {
		return _keys.get(item);
	}

	/**
	 * The object displayed under the given client key, {@code null} if none is.
	 */
	public Object item(String key) {
		return _itemsByKey.get(key);
	}

	/**
	 * Whether the given object is displayed.
	 */
	public boolean displays(Object item) {
		return _keys.containsKey(item);
	}

	/**
	 * Whether one item or several may be selected.
	 */
	public SelectionMode getMode() {
		return _mode;
	}

	/**
	 * Sets whether one item or several may be selected.
	 */
	public void setMode(SelectionMode mode) {
		_mode = mode;
	}

	/**
	 * Whether the {@link #getMode() selection mode} is {@link SelectionMode#MULTI}.
	 */
	public boolean isMulti() {
		return _mode == SelectionMode.MULTI;
	}

	/**
	 * The selected objects, empty if there are none; also those that are not displayed.
	 */
	public Set<Object> getSelection() {
		return Collections.unmodifiableSet(_selection);
	}

	/**
	 * Sets the selected objects, without reporting them to the {@link #setListener(Consumer)
	 * listener}.
	 *
	 * @param items
	 *        The selected objects, empty for no selection. In the selection mode
	 *        {@link SelectionMode#SINGLE}, the caller passes at most one.
	 */
	public void setSelection(Collection<?> items) {
		_selection.clear();
		_selection.addAll(items);
		_publisher.accept(selectedKeys());
	}

	/**
	 * Sets the listener informed when the user changes the selection.
	 *
	 * @param listener
	 *        Receives the selected objects; {@code null} for no listener.
	 */
	public void setListener(Consumer<Set<Object>> listener) {
		_listener = listener;
	}

	/**
	 * The client keys of the displayed selected objects, in selection order.
	 */
	public List<String> selectedKeys() {
		List<String> keys = new ArrayList<>(_selection.size());
		for (Object item : _selection) {
			String key = _keys.get(item);
			if (key != null) {
				keys.add(key);
			}
		}
		return keys;
	}

	/**
	 * The displayed selected objects, in display order.
	 */
	public List<Object> displayedSelection() {
		List<Object> result = new ArrayList<>();
		for (Object item : _items) {
			if (_selection.contains(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * Selects the item with the given client key, as the user clicks it.
	 *
	 * <p>
	 * A key that names no displayed item - the item went away while the click was on its way -
	 * changes nothing.
	 * </p>
	 *
	 * @return Whether the key named a displayed item.
	 *
	 * @see #select(Object, boolean, boolean)
	 */
	public boolean select(String key, boolean toggle, boolean range) {
		Object item = _itemsByKey.get(key);
		if (item == null) {
			return false;
		}
		select(item, toggle, range);
		return true;
	}

	/**
	 * Selects the given object as the user does, and reports the new selection to the
	 * {@link #setListener(Consumer) listener}.
	 *
	 * <p>
	 * A report the listener refuses by an exception - unsaved changes the selection would replace,
	 * say - restores the selection before and propagates.
	 * </p>
	 *
	 * @param toggle
	 *        Whether the object is added to the selection, or taken out of it when it is selected,
	 *        instead of replacing the selection. In the selection mode {@link SelectionMode#SINGLE},
	 *        a toggle of the selected object gives the selection up.
	 * @param range
	 *        Whether the objects from the one selected last up to the given one are added to the
	 *        selection; only in the selection mode {@link SelectionMode#MULTI}.
	 */
	public void select(Object item, boolean toggle, boolean range) {
		Set<Object> next = new LinkedHashSet<>();
		Object anchor = _anchor;
		if (isMulti()) {
			if (range && anchor != null && displays(anchor)) {
				next.addAll(_selection);
				next.addAll(itemsBetween(anchor, item));
			} else if (toggle) {
				next.addAll(_selection);
				if (!next.remove(item)) {
					next.add(item);
				}
				anchor = item;
			} else {
				next.add(item);
				anchor = item;
			}
		} else {
			if (!(toggle && _selection.contains(item))) {
				next.add(item);
			}
			anchor = item;
		}

		List<Object> before = new ArrayList<>(_selection);
		setSelection(next);
		if (_listener != null) {
			try {
				_listener.accept(getSelection());
			} catch (RuntimeException ex) {
				setSelection(before);
				throw ex;
			}
		}
		_anchor = anchor;
	}

	/**
	 * The displayed objects from one of the given ones up to the other, both included, in display
	 * order.
	 */
	public List<Object> itemsBetween(Object first, Object last) {
		List<Object> result = new ArrayList<>();
		boolean inRange = false;
		for (Object item : _items) {
			boolean bound = item.equals(first) || item.equals(last);
			if (bound || inRange) {
				result.add(item);
			}
			if (bound) {
				if (inRange || first.equals(last)) {
					return result;
				}
				inRange = true;
			}
		}
		return result;
	}

	/**
	 * The replay-stable form of a selection by client key: the same selection naming the selected
	 * object by its {@link ScriptingModelKey business identity}, since client keys are allocated per
	 * session.
	 *
	 * <p>
	 * The client command carries the item's client key in the given argument, and the
	 * {@link SelectionModifiers modifiers} under their names.
	 * </p>
	 *
	 * @param arguments
	 *        The arguments of the client command.
	 * @param keyArgument
	 *        The argument carrying the client key of the selected item.
	 * @param recordedType
	 *        The arguments type of the recorded command.
	 * @param recordedCommand
	 *        The name of the recorded command, whose handler calls
	 *        {@link #selectByName(SelectByNameArguments, ResKey1)}.
	 * @return The command to record, {@code null} if the arguments name no displayed item with a
	 *         business identity.
	 */
	public RecordedCommand recordByName(Map<String, Object> arguments, String keyArgument,
			Class<? extends SelectByNameArguments> recordedType, String recordedCommand) {
		if (arguments == null || !(arguments.get(keyArgument) instanceof String key)) {
			return null;
		}
		Object item = _itemsByKey.get(key);
		ModelName name = item == null ? null : ScriptingModelKey.name(null, item);
		if (name == null) {
			return null;
		}
		SelectByNameArguments recorded = TypedConfiguration.newConfigItem(recordedType);
		recorded.setName(recordedCommand);
		recorded.setKey(name);
		recorded.setToggle(Boolean.TRUE.equals(arguments.get(SelectionModifiers.TOGGLE)));
		recorded.setRange(Boolean.TRUE.equals(arguments.get(SelectionModifiers.RANGE)));
		return new RecordedCommand(recorded);
	}

	/**
	 * Selects the item of the object named by the given {@link ModelName business identity} as the
	 * user does - the replay of a selection {@link #recordByName(Map, String, Class, String)
	 * recorded} by name.
	 *
	 * @param unresolved
	 *        The error reported, with the name, when it designates no displayed object: a recorded
	 *        selection that does not resolve is an explicit failure, never a silent no-op.
	 */
	public HandlerResult selectByName(SelectByNameArguments args, ResKey1 unresolved) {
		ModelName name = args.getKey();
		Object item = ScriptingModelKey.locate(null, name);
		if (item == null || !displays(item)) {
			return HandlerResult.error(unresolved.fill(name));
		}
		select(item, args.isToggle(), args.isRange());
		return HandlerResult.DEFAULT_RESULT;
	}

}
