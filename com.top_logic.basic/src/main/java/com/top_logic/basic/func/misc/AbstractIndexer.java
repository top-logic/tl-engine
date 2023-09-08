/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.func.Function1;

/**
 * {@link Function1} computing a {@link Mapping} for a {@link List} that provides for each list
 * entry its index in the list.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractIndexer<T, K, V> extends Function1<Mapping<K, V>, Iterable<T>> {

	@Override
	public Mapping<K, V> apply(Iterable<T> arg) {
		final Map<K, V> map = new HashMap<>();
		if (arg != null) {
			for (T entry : arg) {
				map.put(key(entry), value(entry));
			}
		}
		return Mappings.createMapBasedMapping(map, (V) null);
	}

	/**
	 * Transform the given entry into a {@link Mapping} key.
	 * 
	 * @param entry
	 *        The entry.
	 * @return The key to be used in the mapping.
	 */
	protected abstract K key(T entry);

	/**
	 * Transform the given entry into a {@link Mapping} value.
	 * 
	 * @param entry
	 *        The entry.
	 * @return The value to be returned from the mapping.
	 */
	protected abstract V value(T entry);

}
