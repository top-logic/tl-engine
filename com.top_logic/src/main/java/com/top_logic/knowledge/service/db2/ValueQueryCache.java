/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.knowledge.search.RevisionQueryArguments;

/**
 * {@link SimpleQueryCache} that stores the result itself.
 * 
 * @see IDQueryCache
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValueQueryCache<E> extends SimpleQueryCache<E> {

	/**
	 * Creates a new {@link ValueQueryCache}.
	 */
	public ValueQueryCache(DBKnowledgeBase kb, SimpleQuery<E> query, RevisionQueryArguments args) {
		super(kb, query, args);
	}

	@Override
	protected KBCacheValue<List<E>> newCache(long minValidity, List<E> cacheValue) {
		return new SimpleKBCacheValue<>(minValidity, cacheValue);
	}

	@Override
	protected boolean cacheResultModifiable() {
		return false;
	}

}

