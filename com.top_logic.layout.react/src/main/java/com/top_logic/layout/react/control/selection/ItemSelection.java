/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.identity.DisplayedObjectId;
import com.top_logic.layout.react.control.identity.ObjectIdIndex;
import com.top_logic.layout.react.control.identity.ObjectIdentities;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.table.SelectionMode;

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
 * {@link #selectedKeys()} itself after {@link #setItems(Collection) displaying other items}.
 * </p>
 *
 * <p>
 * The client keys are the ids the commands of the owning control take the items by: the control
 * offers the {@link #identities() identities} of the items as its object identities and marks the
 * key argument of its selection command {@link DisplayedObjectId}, so a recorded selection names
 * the business key of the selected object and replays by the client key it has in the replaying
 * session.
 * </p>
 */
public class ItemSelection {

	private final Consumer<List<String>> _publisher;

	/** Client keys of the displayed objects. */
	private final ObjectIdIndex _keys;

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
		_keys = new ObjectIdIndex(keyAllocator);
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
		_keys.setObjects(items);
		_items = List.copyOf(items);
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
		return _keys.idOf(item);
	}

	/**
	 * The object displayed under the given client key, {@code null} if none is.
	 */
	public Object item(String key) {
		return _keys.objectOf(key);
	}

	/**
	 * The identities of the displayed objects: the client key each one is displayed under, and its
	 * global business key.
	 *
	 * <p>
	 * A key resolves only to a displayed object.
	 * </p>
	 */
	public ObjectIdentities identities() {
		return _keys;
	}

	/**
	 * Whether the given object is displayed.
	 */
	public boolean displays(Object item) {
		return _keys.contains(item);
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
			String key = _keys.idOf(item);
			if (key != null) {
				keys.add(key);
			}
		}
		return keys;
	}

	/**
	 * Replaces the client keys in the given lists of a headless projection by the business keys of
	 * their objects, unless the projection {@link ProjectionMode#projectsIds(ObjectIdentities) shows
	 * ids}.
	 *
	 * <p>
	 * A client key is allocated per session, so an assertion on a projection holding it would fail
	 * in every other session. An object without a business key keeps its client key.
	 * </p>
	 *
	 * @param projection
	 *        The projected state of the control owning this selection.
	 * @param mode
	 *        How the projection identifies objects.
	 * @param identities
	 *        The {@link com.top_logic.layout.react.control.ScriptingControl#objectIdentities()
	 *        object identities} of the control owning this selection, {@code null} if it offers
	 *        none.
	 * @param properties
	 *        The properties of the projection holding lists of client keys.
	 */
	@SuppressWarnings("unchecked")
	public void identifyKeys(Map<String, Object> projection, ProjectionMode mode, ObjectIdentities identities,
			String... properties) {
		if (mode.projectsIds(identities)) {
			return;
		}
		for (String property : properties) {
			if (projection.get(property) instanceof List<?> keys) {
				List<Object> result = new ArrayList<>(keys.size());
				for (String key : (List<String>) keys) {
					Object item = _keys.objectOf(key);
					result.add(item == null ? key : ScriptingModelKey.identity(identities().keyContext(), item, key));
				}
				projection.put(property, result);
			}
		}
	}

	/**
	 * Replaces the client keys in the given list of a headless projection by an entry for each
	 * displayed object, see {@link #identifyItem(String, ProjectionMode, ObjectIdentities)}.
	 *
	 * @param projection
	 *        The projected state of the control owning this selection.
	 * @param mode
	 *        How the projection identifies objects.
	 * @param identities
	 *        The {@link com.top_logic.layout.react.control.ScriptingControl#objectIdentities()
	 *        object identities} of the control owning this selection, {@code null} if it offers
	 *        none.
	 * @param property
	 *        The property of the projection holding the list of client keys.
	 */
	@SuppressWarnings("unchecked")
	public void identifyItems(Map<String, Object> projection, ProjectionMode mode, ObjectIdentities identities,
			String property) {
		if (projection.get(property) instanceof List<?> keys) {
			List<Object> result = new ArrayList<>(keys.size());
			for (String key : (List<String>) keys) {
				result.add(identifyItem(key, mode, identities));
			}
			projection.put(property, result);
		}
	}

	/**
	 * The entry of the headless projection identifying the object displayed under the given client
	 * key.
	 *
	 * <p>
	 * Where the projection {@link ProjectionMode#projectsIds(ObjectIdentities) shows ids}, the entry
	 * holds the client key under {@link ScriptingModelKey#ID} - the id the selection command takes -
	 * and the label of the object; otherwise the business key of the object, see
	 * {@link ScriptingModelKey#putIdentity(Map, ProjectionMode, ObjectIdentities, Object, Object, String, Object)}.
	 * </p>
	 *
	 * @param key
	 *        The client key of a displayed object.
	 * @param mode
	 *        How the projection identifies objects.
	 * @param identities
	 *        The {@link com.top_logic.layout.react.control.ScriptingControl#objectIdentities()
	 *        object identities} of the control owning this selection, {@code null} if it offers
	 *        none.
	 */
	public Map<String, Object> identifyItem(String key, ProjectionMode mode, ObjectIdentities identities) {
		Map<String, Object> entry = new LinkedHashMap<>();
		Object item = _keys.objectOf(key);
		if (item == null) {
			entry.put(ScriptingModelKey.ID, key);
		} else {
			ScriptingModelKey.putIdentity(entry, mode, identities, identities().keyContext(), item,
				ScriptingModelKey.ID, key);
		}
		return entry;
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
		Object item = _keys.objectOf(key);
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

}
