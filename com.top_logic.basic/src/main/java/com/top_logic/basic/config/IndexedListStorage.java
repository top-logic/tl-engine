/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;

/**
 * {@link AbstractListStorage} which expects that there is a key property.
 *
 * <p>
 * The items are stored in a doubly linked list of nodes. A node whose item has a key value is
 * indexed by that key, so that references resolve in constant time. A node whose item has no key
 * value (an anonymous item, see {@link ConfigUtil#isAnonymousKey(Object)}) is not indexed by key; it is reachable only by its position or, while
 * stored, by the item itself.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class IndexedListStorage<K, V extends ConfigurationItem> extends AbstractListStorage<K, V> {

	/**
	 * Entry of the linked list.
	 */
	private static final class Node<T> {

		Node<T> _previous;

		Node<T> _next;

		T _value;

		/**
		 * The key value of {@link #_value}, <code>null</code> for an anonymous item.
		 */
		Object _key;

		Node(T value, Object key) {
			_value = value;
			_key = key;
		}

		@Override
		public String toString() {
			return "[key:" + _key + ", value:" + _value + "]";
		}

	}

	private Node<V> _first;

	private Node<V> _last;

	private int _size;

	/** The nodes of all items with a key value, indexed by that key. */
	private final Map<Object, Node<V>> _nodeByKey = new HashMap<>();

	/** The nodes of all items, indexed by the item identity. */
	private final Map<V, Node<V>> _nodeByValue = new IdentityHashMap<>();

	private final PropertyDescriptor _keyProperty;

	public IndexedListStorage(Log protocol, PropertyDescriptor keyProperty, List<? extends V> baseList) {
		super(protocol);
		_keyProperty = keyProperty;
		if (baseList != null) {
			for (V item : baseList) {
				append(item);
			}
		}
	}

	@Override
	void append(V item) {
		Node<V> node = createNode(item);
		linkBefore(null, node);
	}

	@Override
	void prepend(V item) {
		Node<V> node = createNode(item);
		linkBefore(_first, node);
	}

	@Override
	void insertBefore(K reference, V item) {
		Node<V> target = referencedNode(reference);
		if (target == null) {
			return;
		}
		Node<V> node = createNode(item);
		linkBefore(target, node);
	}

	@Override
	void insertAfter(K reference, V item) {
		Node<V> target = referencedNode(reference);
		if (target == null) {
			return;
		}
		Node<V> node = createNode(item);
		linkBefore(target._next, node);
	}

	@Override
	void remove(K reference) {
		if (!checkNamedKey(reference)) {
			return;
		}
		Node<V> node = _nodeByKey.get(reference);
		if (node == null) {
			_protocol.info("Ignored removal of entry '" + reference + "' from property '" + _keyProperty
				+ "', the list contains no such entry, " + possibleValues(), Log.WARN);
			return;
		}
		unlink(node);
		unregister(node);
	}

	@Override
	void replace(V oldItem, V newItem) {
		Node<V> node = containedNode(oldItem);
		if (node == null) {
			return;
		}
		Object newKey = key(newItem);
		if (newKey != null && !newKey.equals(node._key) && _nodeByKey.containsKey(newKey)) {
			_protocol.error(
				"Duplicate key '" + newKey + "' in property '" + _keyProperty + "' at " + newItem.location());
			return;
		}
		unregister(node);
		node._value = newItem;
		node._key = newKey;
		register(node);
	}

	@Override
	void moveToStart(V item) {
		Node<V> node = containedNode(item);
		if (node == null) {
			return;
		}
		unlink(node);
		linkBefore(_first, node);
	}

	@Override
	void moveToEnd(V item) {
		Node<V> node = containedNode(item);
		if (node == null) {
			return;
		}
		unlink(node);
		linkBefore(null, node);
	}

	@Override
	void moveBefore(V item, K reference) {
		Node<V> node = containedNode(item);
		Node<V> target = referencedNode(reference);
		if (node == null || target == null || node == target) {
			return;
		}
		unlink(node);
		linkBefore(target, node);
	}

	@Override
	void moveAfter(V item, K reference) {
		Node<V> node = containedNode(item);
		Node<V> target = referencedNode(reference);
		if (node == null || target == null || node == target) {
			return;
		}
		unlink(node);
		linkBefore(target._next, node);
	}

	@Override
	V resolveReference(K reference) {
		Node<V> node = referencedNode(reference);
		return node == null ? null : node._value;
	}

	@Override
	V resolveReferenceOrNull(K reference) {
		if (ConfigUtil.isAnonymousKey(reference)) {
			return null;
		}
		Node<V> node = _nodeByKey.get(reference);
		return node == null ? null : node._value;
	}

	@Override
	V resolveIndexOrNull(int index) {
		if (index < 0 || index >= _size) {
			return null;
		}
		Node<V> node = _first;
		for (int n = 0; n < index; n++) {
			node = node._next;
		}
		return node._value;
	}

	@Override
	int size() {
		return _size;
	}

	@Override
	List<V> toList() {
		if (_size == 0) {
			return Collections.emptyList();
		}
		ArrayList<V> result = new ArrayList<>(_size);
		for (Node<V> node = _first; node != null; node = node._next) {
			result.add(node._value);
		}
		return result;
	}

	/**
	 * Creates a node for an item that is not yet contained in the list and registers it.
	 *
	 * @throws IllegalArgumentException
	 *         If the item has the key value of an item already in the list.
	 */
	private Node<V> createNode(V item) {
		Object key = key(item);
		if (key != null && _nodeByKey.containsKey(key)) {
			throw new IllegalArgumentException(
				"Duplicate key '" + key + "' in property '" + _keyProperty + "' at " + item.location());
		}
		Node<V> node = new Node<>(item, key);
		register(node);
		return node;
	}

	private void register(Node<V> node) {
		if (node._key != null) {
			_nodeByKey.put(node._key, node);
		}
		_nodeByValue.put(node._value, node);
	}

	private void unregister(Node<V> node) {
		if (node._key != null) {
			_nodeByKey.remove(node._key);
		}
		_nodeByValue.remove(node._value);
	}

	/**
	 * The node of the item with the given key value, or <code>null</code> after reporting an
	 * error.
	 */
	private Node<V> referencedNode(Object reference) {
		if (!checkNamedKey(reference)) {
			return null;
		}
		Node<V> node = _nodeByKey.get(reference);
		if (node == null) {
			_protocol.error(
				"No corresponding ConfigurationItem found for reference '" + reference + "', " + possibleValues());
		}
		return node;
	}

	/**
	 * Whether the given reference names a key, reports an error for an anonymous key.
	 */
	private boolean checkNamedKey(Object reference) {
		if (ConfigUtil.isAnonymousKey(reference)) {
			_protocol.error("A reference to an entry of property '" + _keyProperty
				+ "' must name a key, anonymous entries cannot be referenced.");
			return false;
		}
		return true;
	}

	/**
	 * Message part listing the key values of the entries in this list.
	 */
	private String possibleValues() {
		return "possible values are: "
			+ _nodeByKey.keySet().stream().map(Objects::toString).sorted().collect(Collectors.joining(", "));
	}

	/**
	 * The node of the given item, or <code>null</code> after reporting an error.
	 */
	private Node<V> containedNode(V item) {
		Node<V> node = _nodeByValue.get(item);
		if (node == null) {
			_protocol.error("Item '" + item + "' is not contained in the list of property '" + _keyProperty + "'.");
		}
		return node;
	}

	/**
	 * Links the given unlinked node before the given successor, <code>null</code> appends it.
	 */
	private void linkBefore(Node<V> successor, Node<V> node) {
		Node<V> predecessor = successor == null ? _last : successor._previous;
		node._previous = predecessor;
		node._next = successor;
		if (predecessor == null) {
			_first = node;
		} else {
			predecessor._next = node;
		}
		if (successor == null) {
			_last = node;
		} else {
			successor._previous = node;
		}
		_size++;
	}

	private void unlink(Node<V> node) {
		if (node._previous == null) {
			_first = node._next;
		} else {
			node._previous._next = node._next;
		}
		if (node._next == null) {
			_last = node._previous;
		} else {
			node._next._previous = node._previous;
		}
		node._previous = null;
		node._next = null;
		_size--;
	}

	private Object key(V value) {
		if (value == null) {
			_protocol.error("Null must not be stored in an indexed list.");
			return null;
		}
		ConfigurationItem config = value;
		ConfigurationDescriptor expectedDescriptor = _keyProperty.getDescriptor();
		Class<?> expectedType = expectedDescriptor.getConfigurationInterface();
		ConfigurationDescriptor concreteDescriptor = config.descriptor();
		Class<?> concreteType = concreteDescriptor.getConfigurationInterface();
		if (!expectedType.isAssignableFrom(concreteType)) {
			_protocol.error("List element is expected to be of type '" + expectedType.getName() + "' but found '"
				+ concreteType.getName() + "'.");
			return null;
		}
		Object key = config.value(_keyProperty);
		if (ConfigUtil.isAnonymousKey(key)) {
			return null;
		}
		return key;
	}

}
