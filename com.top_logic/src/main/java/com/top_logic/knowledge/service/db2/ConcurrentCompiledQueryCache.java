/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.service.CompiledQueryCache;

/**
 * Implementation of {@link CompiledQueryCache} based on a {@link ConcurrentHashMap}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConcurrentCompiledQueryCache extends ConcurrentHashMap<Object, CompiledQuery<?>>
		implements CompiledQueryCache {

	@Override
	public <E> CompiledQuery<E> getQuery(Object key) {
		return cast(get(key));
	}

	@Override
	public <E> CompiledQuery<E> storeQuery(Object key, CompiledQuery<E> query) {
		if (query == null) {
			throw new IllegalArgumentException("Query to cache must not be null.");
		}
		return cast(MapUtil.putIfAbsent(this, key, query));
	}

	@Override
	public <E> CompiledQuery<E> removeQuery(Object key) {
		return cast(remove(key));
	}

	@SuppressWarnings("unchecked")
	private <E> CompiledQuery<E> cast(CompiledQuery<?> query) {
		return (CompiledQuery<E>) query;
	}

}

