/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map;

import com.top_logic.basic.StringServices;


/**
 * {@link Map} that has an algorithm to compute the key from a value.
 * 
 * <p>
 * Values can only be put into this map with a key that matches the given key computation algorithm.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IndexMap<K, V> extends ObservableMap<K, V> {

	private final Mapping<V, K> _keyMapping;

	/**
	 * Creates a {@link IndexMap}.
	 * 
	 * @param keyMapping
	 *        The algorithm to create a suitable key for a value.
	 */
	public IndexMap(Mapping<V, K> keyMapping) {
		_keyMapping = keyMapping;
	}

	@Override
	protected void beforeAdd(K key, V newValue) {
		final K expectedKey = _keyMapping.map(newValue);
		if (!StringServices.equals(key, expectedKey)) {
			throw new IllegalArgumentException("In an index map, the key '" + key
				+ "' must match the value of the index property ('" + expectedKey + "').");
		}
	}

}
