/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility for creating maps with multiple entries in a single expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MapBuilder<K,V> {
	
	/**
	 * Whether a reference to this builder's {@link #buffer} has already
	 * {@link #toMap() handed out}.
	 */
	private boolean shared = false;
	
	/**
	 * The buffer, where map entries are accumulated.
	 */
	private Map<K,V> buffer;


    /**
     * Creates a new MapBuilder with a new map.
     */
    public MapBuilder() {
		this(false);
	}

	/**
	 * Creates a new MapBuilder with a new map.
	 * 
	 * @param linked
	 *        Whether the order of {@link Map#keySet()} of the result map must be the same as the
	 *        insertion order.
	 * 
	 * @see LinkedHashMap
	 * @see HashMap
	 */
	public MapBuilder(boolean linked) {
		buffer = linked ? new LinkedHashMap<>() : new HashMap<>();
    }

    /**
     * Creates a new MapBuilder with a new map of the given size.
     * 
     * @param size the size of this map builder to avoid resizing operations.
     */
    public MapBuilder(int size) {
        buffer = MapUtil.newMap(size);
    }

	/**
	 * Adds a new entry to this map builder.
	 * 
	 * @return This {@link MapBuilder} object for call chaining.
	 */
	public MapBuilder<K,V> put(K key, V value) {
		checkIfDuplicateKey(key);
		useNewBufferIfShared();
		
		buffer.put(key, value);
		return this;
	}

	public MapBuilder<K,V> putAll(Map<K,V> source) {
		checkIfDuplicateKeys(source.keySet());
		useNewBufferIfShared();
		
		buffer.putAll(source);
		return this;
	}

	private void checkIfDuplicateKey(K key) {
		if (buffer.containsKey(key)) {
			throw new IllegalArgumentException("Duplicate key '" + key + "'.");
		}
	}

	private void checkIfDuplicateKeys(Iterable<K> keys) {
		for (K key : keys) {
			checkIfDuplicateKey(key);
		}
	}

	private void useNewBufferIfShared() {
		if (shared) {
			// Copy on write
			buffer = copyBuffer();
			shared = false;
		}
	}

	private Map<K, V> copyBuffer() {
		if (buffer instanceof LinkedHashMap<?, ?>) {
			return new LinkedHashMap<>(buffer);
		} else {
			return new HashMap<>(buffer);
		}
	}

	/**
	 * Creates a non-shared map instance that contains all key-value pairs added
	 * to this map builder.
	 * 
	 * <p>
	 * Later calls to {@link #put(Object, Object)} will not modify the returned
	 * map.
	 * </p>
	 */
	public Map<K,V> toMap() {
		Map<K,V> result;
		if (shared) {
			// Prevent returning two reference to the same map object.
			result = copyBuffer();
		} else {
			result = buffer;
		}
		shared = true;
		return result;
	}
	
	/**
	 * Creates a non-shared unmodifiable map instance that contains all key-value pairs added
	 * to this map builder.
	 * 
	 */
	public Map<K,V> toUnmodifiableMap() {
		return Collections.unmodifiableMap(toMap());
	}

}
