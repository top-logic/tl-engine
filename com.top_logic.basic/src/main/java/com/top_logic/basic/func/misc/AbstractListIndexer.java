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
public abstract class AbstractListIndexer<K, T> extends Function1<Mapping<K, Integer>, List<T>> {

	@Override
	public Mapping<K, Integer> apply(List<T> arg) {
		final Map<K, Integer> map = new HashMap<>();
		if (arg != null) {
			for (int n = 0, cnt = arg.size(); n < cnt; n++) {
				map.put(key(arg.get(n)), Integer.valueOf(n));
			}
		}
		return Mappings.createMapBasedMapping(map, (Integer) null);
	}

	/**
	 * Transform the given entry into a {@link Mapping} key.
	 * 
	 * @param entry
	 *        The list entry.
	 * @return The key to be used in the mapping.
	 */
	protected abstract K key(T entry);

}
