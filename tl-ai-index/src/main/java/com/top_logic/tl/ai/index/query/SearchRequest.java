/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

import java.util.Map;
import java.util.Set;

/**
 * What to search for.
 *
 * @param query
 *        The words of the search, in the reader's language; ignored by a similarity search.
 * @param types
 *        Qualified type names one of which a hit must have; empty for every indexed type.
 * @param scopes
 *        Identifiers of scope objects to search in, narrowing the scopes the reader may read; empty
 *        for all of those.
 * @param filters
 *        Keyword values per keyword name, one of which a hit must carry.
 * @param mode
 *        Whether words, meaning or both are searched.
 * @param limit
 *        How many objects the page holds at most.
 * @param minRevision
 *        A revision the index must have reached before it is searched, e.g. the caller's own last
 *        commit; 0 for none.
 * @param waitMillis
 *        How long to wait for {@code minRevision}.
 */
public record SearchRequest(String query, Set<String> types, Set<String> scopes, Map<String, Set<String>> filters,
		SearchMode mode, int limit, long minRevision, long waitMillis) {

	/** The page size when none is given. */
	public static final int DEFAULT_LIMIT = 10;

	/** The largest page. */
	public static final int MAX_LIMIT = 50;

	/**
	 * Creates a {@link SearchRequest}, with the limit kept between 1 and {@link #MAX_LIMIT}.
	 */
	public SearchRequest {
		types = types == null ? Set.of() : Set.copyOf(types);
		scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
		filters = filters == null ? Map.of() : Map.copyOf(filters);
		mode = mode == null ? SearchMode.HYBRID : mode;
		limit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
	}

	/**
	 * A hybrid search for the given words over every indexed type.
	 */
	public static SearchRequest of(String query) {
		return new SearchRequest(query, Set.of(), Set.of(), Map.of(), SearchMode.HYBRID, DEFAULT_LIMIT, 0, 0);
	}

	/**
	 * This request, for the given types.
	 */
	public SearchRequest withTypes(Set<String> newTypes) {
		return new SearchRequest(query, newTypes, scopes, filters, mode, limit, minRevision, waitMillis);
	}

	/**
	 * This request, in the given scopes.
	 */
	public SearchRequest withScopes(Set<String> newScopes) {
		return new SearchRequest(query, types, newScopes, filters, mode, limit, minRevision, waitMillis);
	}

	/**
	 * This request, with the given keyword filters.
	 */
	public SearchRequest withFilters(Map<String, Set<String>> newFilters) {
		return new SearchRequest(query, types, scopes, newFilters, mode, limit, minRevision, waitMillis);
	}

	/**
	 * This request, in the given mode.
	 */
	public SearchRequest withMode(SearchMode newMode) {
		return new SearchRequest(query, types, scopes, filters, newMode, limit, minRevision, waitMillis);
	}

	/**
	 * This request, with the given page size.
	 */
	public SearchRequest withLimit(int newLimit) {
		return new SearchRequest(query, types, scopes, filters, mode, newLimit, minRevision, waitMillis);
	}

	/**
	 * This request, waiting up to {@code millis} for the index to reach the given revision.
	 */
	public SearchRequest awaiting(long revision, long millis) {
		return new SearchRequest(query, types, scopes, filters, mode, limit, revision, millis);
	}

}
