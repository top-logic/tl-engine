/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.NamedConstant;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.RevisionQuery;

/**
 * Cache for {@link CompiledQuery}.
 * 
 * <p>
 * This cache can be retrieved from a {@link KnowledgeBase} to store pre-compiled
 * {@link RevisionQuery} for multiple usage.
 * </p>
 * 
 * <p>
 * Typical usage:
 * 
 * <pre>
 *  CompiledQueyCache cache = kb.getQueryCache();
 *  CompileQuery query = cache.getQuery(key);
 *  if (query == null) {
 *  	query = cache.storeQuery(key, createQuery(...));
 *  }
 *  query.search(...)
 * </pre>
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CompiledQueryCache {

	/**
	 * Returns the {@link CompiledQuery} formerly stored under the given key in
	 * {@link #storeQuery(Object, CompiledQuery)}.
	 * 
	 * <p>
	 * It is strongly recommend to use a {@link NamedConstant} as key or something which has a
	 * {@link NamedConstant} as part, to ensure a unique key.
	 * </p>
	 * 
	 * @param key
	 *        The key used to store the requested query.
	 * 
	 * @return The formerly stored {@link CompiledQuery} or <code>null</code> when none yet stored.
	 */
	<E> CompiledQuery<E> getQuery(Object key);

	/**
	 * Stores the given {@link CompiledQuery} under the given key in this cache, if not already a
	 * different query is stored. It is <b>not</b> possible to override an existing cache entry. It
	 * needed the former entry must be {@link #removeQuery(Object) removed}.
	 * 
	 * <p>
	 * As this cache is used concurrently it may be that a different thread already has stored a
	 * query for the same key. Therefore the returned query should be used.
	 * </p>
	 * 
	 * <p>
	 * It is strongly recommend to use a {@link NamedConstant} as key or something which has a
	 * {@link NamedConstant} as part, to ensure a unique key.
	 * </p>
	 * 
	 * @param key
	 *        The key to store the requested query.
	 * @param query
	 *        The {@link CompiledQuery} to store. Must not be <code>null</code>.
	 * 
	 * @return The query which can now be accessed using {@link #getQuery(Object)}. It is not
	 *         necessarily the given query.
	 */
	<E> CompiledQuery<E> storeQuery(Object key, CompiledQuery<E> query);

	/**
	 * Removes the entry for the given key.
	 * 
	 * @param key
	 *        The key to identifying the query to remove.
	 * 
	 * @return The formerly stored query, or <code>null</code> when none was formerly stored.
	 */
	<E> CompiledQuery<E> removeQuery(Object key);

}
