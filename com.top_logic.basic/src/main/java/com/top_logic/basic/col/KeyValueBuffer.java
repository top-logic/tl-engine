/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Space-optimized one-time {@link Iterable} buffer of {@link Entry}s that contain key/value pairs.
 * 
 * <p>
 * This key/value buffer must first be built using {@link #put(Object, Object)} and can then be
 * iterated exactly once using {@link #iterator()}. After iteration has started, the buffer can
 * neither be modified, nor iterated again. However, reusing this buffer by restarting the built
 * process is possible using {@link #clear()}.
 * </p>
 * 
 * @param <K>
 *        The key type, see {@link #getKey()}.
 * @param <V>
 *        The value type, see {@link #getValue()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KeyValueBuffer<K, V> implements Iterable<Entry<K, V>>, Iterator<Entry<K, V>>,
		Entry<K, V> {

	private static final int BEFORE_FIRST_ITERATE_POS = -2;

	private static final int BUILDING_STATE = Integer.MAX_VALUE;

	private final ArrayList<Object> _namesAndValues;

	private int _readPos;

	/**
	 * Creates an empty {@link KeyValueBuffer} buffer.
	 */
	public KeyValueBuffer() {
		this(4);
	}

	/**
	 * Creates an empty {@link KeyValueBuffer} buffer that has capacity to hold at least the given number
	 * of key/value pair entries.
	 * 
	 * @param initialSize
	 *        The buffer capacity.
	 */
	public KeyValueBuffer(int initialSize) {
		_namesAndValues = new ArrayList<>(initialSize * 2);
		clear();
	}

	/**
	 * Builder interface for {@link KeyValueBuffer}.
	 * 
	 * @param name
	 *        The attribute name.
	 * @param value
	 *        The attribute value.
	 * @return This instance for call chaining.
	 */
	public KeyValueBuffer<K, V> put(K name, V value) {
		checkBuildingState();
		_namesAndValues.add(name);
		_namesAndValues.add(value);
		return this;
	}

	/**
	 * Prepares this buffer for reuse.
	 */
	public void clear() {
		_namesAndValues.clear();
		_readPos = BUILDING_STATE;
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		checkBuildingState();
		_readPos = BEFORE_FIRST_ITERATE_POS;
		return this;
	}

	private void checkBuildingState() {
		if (_readPos != BUILDING_STATE) {
			throw new IllegalStateException("Already iterated.");
		}
	}

	@Override
	public boolean hasNext() {
		checkIteratingState();
		return _readPos - BEFORE_FIRST_ITERATE_POS < _namesAndValues.size();
	}

	@Override
	public Entry<K, V> next() {
		checkIteratingState();
		_readPos += 2;
		return this;
	}

	@Override
	public void remove() {
		checkIteratingState();
		throw errorUnmodifiable();
	}

	private void checkIteratingState() {
		if (_readPos == BUILDING_STATE) {
			throw new IllegalStateException("Iteration not yet started.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public K getKey() {
		checkValueState();
		return (K) _namesAndValues.get(_readPos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V getValue() {
		checkValueState();
		return (V) _namesAndValues.get(_readPos + 1);
	}

	private void checkValueState() {
		if (!inValueState()) {
			throw new IllegalStateException("No entry has yet been retrieved.");
		}
	}

	private boolean inValueState() {
		return _readPos != BUILDING_STATE && _readPos > BEFORE_FIRST_ITERATE_POS;
	}

	@Override
	public V setValue(V arg0) {
		throw errorUnmodifiable();
	}

	private UnsupportedOperationException errorUnmodifiable() {
		return new UnsupportedOperationException("Cannot modify initialization values.");
	}

	@Override
	public int hashCode() {
		if (inValueState()) {
			return entryHashCode(this);
		} else {
			return super.hashCode();
		}
	}

	private static <K, V> int entryHashCode(Entry<K, V> e) {
		return (e.getKey() == null ? 0 : e.getKey().hashCode()) ^
			(e.getValue() == null ? 0 : e.getValue().hashCode());
	}

	@Override
	public boolean equals(Object e2) {
		if (e2 == this) {
			return true;
		}
		if (e2 instanceof Entry<?, ?> && inValueState()) {
			return entryEquals(this, (Entry<?, ?>) e2);
		}
		return false;
	}

	private static <K, V> boolean entryEquals(Entry<K, V> e1, Entry<?, ?> e2) {
		return (e1.getKey() == null ?
			e2.getKey() == null : e1.getKey().equals(e2.getKey())) &&
			(e1.getValue() == null ?
				e2.getValue() == null : e1.getValue().equals(e2.getValue()));
	}

}
