/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MapIterator;

/**
 * Empty {@link BidiMap} implementation.
 * 
 * <p>
 * Access the {@link EmptyBidiMap} using {@link MapUtil#emptyBidiMap()}.
 * </p>
 * 
 * @see MapUtil#emptyBidiMap()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class EmptyBidiMap<K, V> implements BidiMap<K, V> {

	@SuppressWarnings("rawtypes")
	static final BidiMap INSTANCE = new EmptyBidiMap<>();

	@Override
	public MapIterator<K, V> mapIterator() {
		return IteratorUtils.emptyMapIterator();
	}

	@Override
	public K getKey(Object value) {
		return null;
	}

	@Override
	public K removeValue(Object value) {
		return null;
	}

	@Override
	public BidiMap<V, K> inverseBidiMap() {
		return MapUtil.emptyBidiMap();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return Collections.emptySet();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean containsKey(Object key) {
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public V get(Object key) {
		return null;
	}

	@Override
	public V remove(Object key) {
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		// Nothing to do here
	}

	@Override
	public Set<K> keySet() {
		return Collections.emptySet();
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<V> values() {
		return Collections.emptySet();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Map) && ((Map<?, ?>) o).isEmpty();
	}

	@Override
	public int hashCode() {
		return 0;
	}

	// Preserves singleton property
	private Object readResolve() {
		return MapUtil.emptyBidiMap();
	}

}

