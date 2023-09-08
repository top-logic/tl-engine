/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * {@link AbstractQueryCache} for a {@link SimpleQuery}.
 * 
 * <p>
 * The result of the cache is equal to the result of
 * {@link KnowledgeBase#compileSimpleQuery(SimpleQuery)}.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SimpleQueryCache<E> extends AbstractQueryCache<E> {

	/** The {@link SimpleQuery}, this cache bases on. */
	protected final SimpleQuery<E> _query;

	/** Arguments for the simple query. */
	protected final RevisionQueryArguments _args;

	/** The {@link RevisionQuery} created from the given {@link SimpleQuery}. */
	protected final RevisionQuery<E> _revisionQuery;

	private final DBKnowledgeBase _kb;

	/**
	 * Creates a new {@link SimpleQueryCache}.
	 */
	protected SimpleQueryCache(DBKnowledgeBase kb, SimpleQuery<E> query, RevisionQueryArguments args) {
		_kb = kb;
		_args = args;
		_query = query;
		_revisionQuery = query.toRevisionQuery(kb());
	}

	@Override
	protected DBKnowledgeBase kb() {
		return _kb;
	}

	@Override
	protected List<E> newLocalCacheValue() {
		return kb().compileSimpleQuery(_query).search();
	}

	@Override
	protected List<E> newGlobalCacheValue() {
		CompiledQuery<E> compiledQuery = kb().compileQuery(_revisionQuery);
		List<E> searchResult = compiledQuery.search(_args);
		return searchResult;
	}

	@Override
	protected List<E> adaptToTransaction(List<E> cacheValue) {
		boolean adaptResultInline = cacheResultModifiable();
		List<E> adaptionResult =
			kb().adaptToTransaction(_query, _revisionQuery, _args, cacheValue, adaptResultInline);
		if (adaptionResult == null) {
			return cacheValue;
		}
		return adaptionResult;
	}

	@Override
	protected List<E> newGlobalCacheValue(List<E> globalCacheValue, UpdateEvent event) {
		boolean adaptResultInline = cacheResultModifiable();
		List<E> adaptedResult =
			kb().adaptToUpdate(event, _query, _revisionQuery, _args, globalCacheValue, adaptResultInline);
		return adaptedResult;
	}

	/**
	 * Whether the return value of the concrete implementation of
	 * {@link AbstractKBCache.KBCacheValue} is modifiable.
	 */
	protected abstract boolean cacheResultModifiable();

}
