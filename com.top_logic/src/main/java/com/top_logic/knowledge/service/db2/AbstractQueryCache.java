/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;

import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * Factory class to create {@link QueryCache}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractQueryCache<E> extends AbstractKBCache<List<E>> implements QueryCache<E> {

	/**
	 * Creates a new {@link QueryCache} for the given {@link SimpleQuery} with the standard
	 * arguments.
	 * 
	 * @see AbstractQueryCache#newQueryCache(KnowledgeBase, SimpleQuery, RevisionQueryArguments)
	 */
	public static <E> QueryCache<E> newQueryCache(KnowledgeBase kb, SimpleQuery<E> query) {
		return newQueryCache(kb, query, revisionArgs());
	}

	/**
	 * Creates a new {@link QueryCache} for the given {@link SimpleQuery} and the given arguments.
	 * 
	 * <p>
	 * The {@link QueryCache#getValue()} of the result cache also contains local uncommitted
	 * changes.
	 * </p>
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to execute the query in.
	 * @param query
	 *        The simple query, whose result must be cached.
	 * 
	 * @see KnowledgeBase#compileSimpleQuery(SimpleQuery)
	 */
	public static <E> SimpleQueryCache<E> newQueryCache(KnowledgeBase kb, SimpleQuery<E> query,
			RevisionQueryArguments args) {
		return newQueryCache(kb, query, args, false);
	}

	/**
	 * Creates a new {@link QueryCache} for the given {@link SimpleQuery} and the given arguments.
	 * 
	 * <p>
	 * The {@link QueryCache#getValue()} of the result cache also contains local uncommitted
	 * changes.
	 * </p>
	 * 
	 * <p>
	 * In contrast to
	 * {@link AbstractQueryCache#newQueryCache(KnowledgeBase, SimpleQuery, RevisionQueryArguments)}
	 * not the search result is actually stored, but only the identifiers.
	 * </p>
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to execute the query in.
	 * @param query
	 *        The simple query, whose result must be cached.
	 * 
	 * @see KnowledgeBase#compileSimpleQuery(SimpleQuery)
	 */
	public static <E> SimpleQueryCache<E> newQueryCacheByID(KnowledgeBase kb, SimpleQuery<E> query,
			RevisionQueryArguments args) {
		return newQueryCache(kb, query, args, true);
	}

	private static <E> SimpleQueryCache<E> newQueryCache(KnowledgeBase kb, SimpleQuery<E> query,
			RevisionQueryArguments args, boolean cacheByID) {
		if (!(kb instanceof DBKnowledgeBase)) {
			throw new IllegalArgumentException(kb + " must be a " + DBKnowledgeBase.class);
		}
		if (args.getRequestedRevision() != Revision.CURRENT_REV) {
			throw new IllegalArgumentException("Query must be a current query");
		}
		if (cacheByID) {
			return new IDQueryCache<>((DBKnowledgeBase) kb, query, args);
		} else {
			return new ValueQueryCache<>((DBKnowledgeBase) kb, query, args);
		}
	}

}

