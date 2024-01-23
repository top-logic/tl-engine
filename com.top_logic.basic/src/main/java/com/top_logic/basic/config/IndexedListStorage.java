/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.NamedConstant;

/**
 * {@link AbstractListStorage} which expects that there is a key property. It
 * realizes its storage via a "linked map" whose values have references to the
 * keys of the previous and the next "entries" in the list.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class IndexedListStorage<K, V extends ConfigurationItem> extends AbstractListStorage<K, V> {

	/** Object to identify whether the end of the list was reached */
	static final Object END = new NamedConstant("END");

	/**
	 * Value of the internal storage which links to the key of the previous
	 * entry and the key of the next entry.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class LinkedValue<T> {

		Object _previousKey = END;
		Object _nextKey = END;

		T _value;

		LinkedValue(T value) {
			this._value = value;
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append('[');
			buffer.append("previous:").append(_previousKey);
			buffer.append(',');
			buffer.append("value:").append(_value);
			buffer.append(',');
			buffer.append("next:").append(_nextKey);
			buffer.append(']');
			return buffer.toString();
		}

	}

	/** Key of the first entry in the represented list */
	private Object _firstKey = END;
	/** Key of the last entry in the represented list */
	private Object _lastKey = END;

	private HashMap<K, LinkedValue<V>> _storage;

	private final PropertyDescriptor _keyProperty;

	public IndexedListStorage(Log protocol, PropertyDescriptor keyProperty, List<? extends V> baseList) {
		super(protocol);
		_keyProperty = keyProperty;
		this._storage = new HashMap<>();
		if (baseList != null) {
			for (V item : baseList) {
				append(item);
			}
		}
	}

	@Override
	void append(V item) {
		K key = getKey(item);
		if (_storage.isEmpty()) {
			insertFirstElement(key, item);
		} else {
			addLast(key, item);
		}
	}

	/**
	 * Adds the given item as last element to the represented list.
	 */
	private void addLast(K key, V item) {
		checkNotContained(key, item);
		
		// adopts the old last value to point to the new key
		assert _lastKey != END : "Non empty map so a last element";
		LinkedValue<V> oldLastValue = _storage.get(_lastKey);
		assert oldLastValue._nextKey == END : "Last entry must not have a next entry";
		oldLastValue._nextKey = key;

		// inserts the value for the given key as last element
		LinkedValue<V> newLastValue = new LinkedValue<>(item);
		newLastValue._previousKey = _lastKey;
		_lastKey = key;
		LinkedValue<V> oldValue = _storage.put(key, newLastValue);
		assert oldValue == null;
	}

	/**
	 * Inserts the given {@link ConfigurationItem} into the currently empty
	 * list.
	 */
	private void insertFirstElement(K key, V item) {
		assert _firstKey == END : "Empty map so no first element";
		assert _lastKey == END : "Empty map so no last element";
		_firstKey = _lastKey = key;
		LinkedValue<V> v = new LinkedValue<>(item);
		_storage.put(key, v);
	}

	@Override
	void prepend(V item) {
		K key = getKey(item);
		if (_storage.isEmpty()) {
			insertFirstElement(key, item);
		} else {
			addFirst(key, item);
		}
	}

	private void addFirst(K key, V item) {
		checkNotContained(key, item);

		// Updates the value of the first values that it things the given key is
		// the first
		assert _firstKey != END : "Non empty map so a last element";
		LinkedValue<V> oldFirstValue = _storage.get(_firstKey);
		assert oldFirstValue._previousKey == END : "First entry must not have a previous entry";
		oldFirstValue._previousKey = key;

		// Inserts the item under the key as first element in the storage
		LinkedValue<V> newFirstValue = new LinkedValue<>(item);
		newFirstValue._nextKey = _firstKey;
		_firstKey = key;
		_storage.put(key, newFirstValue);
	}

	/**
	 * checks whether the given keys are equal
	 */
	private boolean keysEquals(Object key1, Object key2) {
		if (key1 == null) {
			return key2 == null;
		}
		return key1.equals(key2);
	}

	@Override
	void insertBefore(K reference, V item) {
		K key = getKey(item);
		if (keysEquals(reference, _firstKey)) {
			addFirst(key, item);
			return;
		}

		// Not the first element
		final LinkedValue<V> afterValue = _storage.get(reference);
		if (afterValue == null) {
			_protocol.error("No corresponding ConfigurationItem found for reference '" + reference
				+ "'. References: " + _storage.keySet());
			return;
		}

		assert afterValue._previousKey != END : "referenced Object is not the first.";
		final LinkedValue<V> beforeValue = _storage.get(afterValue._previousKey);
		assert beforeValue != null : "Previous is always a key";

		insertBetween(beforeValue, key, item, afterValue);
	}

	/**
	 * Inserts the given {@link ConfigurationItem} into the list between the
	 * previousValue and the nextValue.
	 */
	private void insertBetween(LinkedValue<V> previousValue, K key, V item,
			LinkedValue<V> nextValue) {
		checkNotContained(key, item);

		LinkedValue<V> newValue = new LinkedValue<>(item);
		_storage.put(key, newValue);
		linkBetween(previousValue, key, newValue, nextValue);
	}

	private void checkNotContained(Object key, V item) {
		if (_storage.get(key) != null) {
			throw new IllegalArgumentException(
				"Duplicate key '" + key + "' in property '" + _keyProperty + "' at " + item.location());
		}
	}

	private void linkBetween(LinkedValue<V> previousValue, Object key, LinkedValue<V> newValue, LinkedValue<V> nextValue) {
		Object previousKey = nextValue._previousKey;
		Object nextKey = previousValue._nextKey;

		newValue._nextKey = nextKey;
		newValue._previousKey = previousKey;

		nextValue._previousKey = key;
		previousValue._nextKey = key;
	}

	@Override
	void insertAfter(K reference, V item) {
		K key = getKey(item);
		if (keysEquals(reference, _lastKey)) {
			addLast(key, item);
			return;
		}

		// Not the last element
		final LinkedValue<V> beforeValue = _storage.get(reference);
		if (beforeValue == null) {
			_protocol.error(
				"Reference '" + reference + "' not found, possible values are: "
					+ _storage.keySet().stream().map(Object::toString).sorted().collect(Collectors.joining(", ")));
			return;
		}

		assert beforeValue._nextKey != END : "referenced Object is not the last.";
		final LinkedValue<V> afterValue = _storage.get(beforeValue._nextKey);
		assert afterValue != null : "Next is always a key";

		insertBetween(beforeValue, key, item, afterValue);
	}

	@Override
	void remove(K reference) {
		final LinkedValue<V> value = _storage.remove(reference);
		if (value == null) {
			_protocol.error("No corresponding ConfigurationItem found for reference '" + reference
				+ "'. References: " + _storage.keySet());
			return;
		}
		if (_storage.isEmpty()) {
			// no more content
			_firstKey = _lastKey = END;
			return;
		}

		// Update entry after the removed one
		final boolean isLast = value._nextKey == END;
		if (!isLast) {
			final LinkedValue<V> nextValue = _storage.get(value._nextKey);
			nextValue._previousKey = value._previousKey;
		} else {
			_lastKey = value._previousKey;
			// is not both first and last
			_storage.get(_lastKey)._nextKey = END;
		}

		// Update entry before the removed one
		final boolean isFirst = value._previousKey == END;
		if (!isFirst) {
			final LinkedValue<V> previousValue = _storage.get(value._previousKey);
			previousValue._nextKey = value._nextKey;
		} else {
			_firstKey = value._nextKey;
			// is not both first and last
			_storage.get(_firstKey)._previousKey = END;
		}
	}

	@Override
	void update(V item) {
		K key = getKey(item);
		updateValue(key, item);
	}

	/**
	 * Updates the value of the key by setting the given item as {@link LinkedValue#_value}.
	 */
	private void updateValue(Object key, V item) {
		final LinkedValue<V> value = _storage.get(key);
		if (value == null) {
			_protocol.error("No corresponding ConfigurationItem found for key '" + key + "'.");
			return;
		}
		value._value = item;
	}

	@Override
	void moveAfter(V item, K reference) {
		K key = getKey(item);
		updateValue(key, item);
		internalMoveAfter(key, reference);
	}

	@Override
	void moveBefore(V item, K reference) {
		K key = getKey(item);
		updateValue(key, item);
		internalMoveBefore(key, reference);
	}

	@Override
	void moveToEnd(V item) {
		K key = getKey(item);
		updateValue(key, item);
		if (!key.equals(_lastKey)) {
			internalMoveAfter(key, _lastKey);
		} else {
			/* Move to the end of the list, whereas the entry is already the last entry. Ignore. */
		}
	}

	@Override
	void moveToStart(V item) {
		K key = getKey(item);
		updateValue(key, item);
		if (!key.equals(_firstKey)) {
			internalMoveBefore(key, _firstKey);
		} else {
			/* Move to the start of the list, whereas the entry is already the first entry. Ignore. */
		}
	}

	private K getKey(V item) {
		return key(item);
	}

	/**
	 * Moves the given key after the given reference
	 */
	private void internalMoveAfter(K key, Object reference) {
		assert reference != END;
		assert !key.equals(reference);
		V value = _storage.get(key)._value;
		remove(key);
		insertAfter((K) reference, value);
	}

	/**
	 * Moves the given key before the given reference
	 */
	private void internalMoveBefore(K key, Object reference) {
		assert reference != END;
		assert !key.equals(reference);
		V value = _storage.get(key)._value;
		remove(key);
		insertBefore((K) reference, value);
	}

	@Override
	V resolveReference(K reference) {
		return getConfig(reference, false);
	}

	private V getConfig(Object reference, boolean mayBeNull) {
		final LinkedValue<V> value = _storage.get(reference);
		if (value == null) {
			if (!mayBeNull) {
				_protocol.error("No corresponding ConfigurationItem found for reference '" + reference
					+ "'. References: " + _storage.keySet());
			}
			return null;
		}
		return value._value;
	}

	@Override
	V resolveReferenceOrNull(K reference) {
		return getConfig(reference, true);
	}

	@Override
	List<V> toList() {
		if (_storage.isEmpty()) {
			return Collections.emptyList();
		}
		ArrayList<V> result = new ArrayList<>(_storage.size());
		Object key = _firstKey;
		while (key != END) {
			final LinkedValue<V> value = _storage.get(key);
			assert value != null : "Key has a non null value";
			assert value._value != null;
			result.add(value._value);
			key = value._nextKey;
		}
		return result;
	}

	/**
	 * For debugging: Call this method at the begin and end of every method that might corrupt the
	 * data structure.
	 */
	@SuppressWarnings("unused")
	private void assertConsistent() {
		if (_storage.isEmpty()) {
			assert _firstKey == END;
			assert _lastKey == END;
			return;
		}
		assert _firstKey != END;
		assert _lastKey != END;
		assert _storage.get(_firstKey)._previousKey == END;
		assert _storage.get(_lastKey)._nextKey == END;

		Set<K> set = new HashSet<>();
		List<V> list = toList();
		for (V entry : list) {
			set.add(key(entry));
		}
		assert set.size() == list.size();

		for (Entry<K, LinkedValue<V>> entry : _storage.entrySet()) {
			Object expectedKey = key(entry.getValue()._value);
			assert entry.getKey().equals(expectedKey);
			if (!entry.getKey().equals(_firstKey)) {
				Object previousKey = entry.getValue()._previousKey;
				assert _storage.get(previousKey)._nextKey.equals(entry.getKey());
			}
			if (!entry.getKey().equals(_lastKey)) {
				Object nextKey = entry.getValue()._nextKey;
				assert _storage.get(nextKey)._previousKey.equals(entry.getKey());
			}
			assert entry.getValue()._value != null;
		}
	}

	private K key(V value) {
		if (value == null) {
			_protocol.error("Null must not be stored in an indexed list.");
			return null;
		}
		ConfigurationItem config = (ConfigurationItem) value;
		ConfigurationDescriptor expectedDescriptor = _keyProperty.getDescriptor();
		Class<?> expectedType = expectedDescriptor.getConfigurationInterface();
		ConfigurationDescriptor concreteDescriptor = config.descriptor();
		Class<?> concreteType = concreteDescriptor.getConfigurationInterface();
		if (!expectedType.isAssignableFrom(concreteType)) {
			_protocol.error("List element is expected to be of type '" + expectedType.getName() + "' but found '"
				+ concreteType.getName() + "'.");
			return null;
		}
		@SuppressWarnings("unchecked")
		K result = (K) config.value(_keyProperty);
		return result;
	}

}
