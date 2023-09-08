/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 * {@link Collection} that automatically indexes its elements using a given
 * {@link Mapping}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IndexedCollection<K, V> extends AbstractCollection<V> {
	private final Map<K, V> base;
	private final Mapping<? super V, ? extends K> keyMapping;

	/**
	 * Creates a {@link IndexedCollection}.
	 * 
	 * @param map
	 *        The {@link Map} that implements the indexed storage of this
	 *        collection.
	 * @param keyMapping
	 *        The mapping that computes the key for each element of this
	 *        collection.
	 */
	public IndexedCollection(Map<K, V> map, Mapping<? super V, ? extends K> keyMapping) {
		this.base = map;
		this.keyMapping = keyMapping;
	}

	@Override
	public boolean add(V newEntry) {
		final K key = getKey(newEntry);
		if (base.containsKey(key)) {
			throw new IllegalArgumentException("Collection already contains element with key '" + key + "'.");
		}
		V clash = base.put(key, newEntry);
		return newEntry != clash;
	}

	private K getKey(V entry) {
		return keyMapping.map(entry);
	}

	@Override
	public Iterator<V> iterator() {
		return base.values().iterator();
	}

	@Override
	public int size() {
		return base.size();
	}
}