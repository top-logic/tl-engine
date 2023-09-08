/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map;
import java.util.Set;


/**
 * {@link Set} that automatically indexes its elements using a given {@link Mapping}.
 * 
 * <p>
 * Elements with the same key can only appear once in this list, but the order of elements is
 * preserved.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IndexedSet<K, V> extends ObservableSet<V> {
	private final Map<K, V> index;
	private final Mapping<? super V, ? extends K> keyMapping;

	/**
	 * Creates a {@link IndexedSet}.
	 * 
	 * @param indexMap
	 *        The {@link Map} that implements the index of this list storage.
	 * @param keyMapping
	 *        The mapping that computes the key for each element of this
	 *        collection.
	 */
	public IndexedSet(Map<K, V> indexMap, Mapping<? super V, ? extends K> keyMapping) {
		this.index = indexMap;
		this.keyMapping = keyMapping;
	}

	@Override
	protected void beforeAdd(V newElement) {
		K key = keyMapping.map(newElement);
		V clash = index.put(key, newElement);
		if (clash != null) {
			// Revert add to index.
			index.put(key, clash);
			
			throw new IllegalArgumentException("Set already contains element with key '" + key + "'.");
		}
	}
	
	@Override
	protected void afterRemove(V oldElement) {
		K key = keyMapping.map(oldElement);
		V indexedElement = index.remove(key);
		assert indexedElement == oldElement : "Inconsistent index: expected '" + oldElement + "', removed '" + indexedElement + "'";
	}
}