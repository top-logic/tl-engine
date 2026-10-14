/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.util.BytesRef;

/**
 * Which chunks a query may find at all, applied inside Lucene to both the word and the meaning side.
 *
 * <p>
 * Because the filter is part of the query, a reader who may read one scope of a thousand gets a full
 * page from that scope: both rankings contain only that scope's chunks. Lucene runs the vector search
 * exactly when the filter passes few documents.
 * </p>
 *
 * @param types
 *        Qualified type names, one of which a chunk's object must have; empty for any type.
 * @param scopes
 *        Scope identifiers, one of which a chunk must carry; <code>null</code> for no scope
 *        restriction. An empty set finds nothing: a reader with no readable scope reads no scoped
 *        chunk.
 * @param keywords
 *        Per keyword name the values one of which a chunk must carry.
 * @param excluded
 *        Objects whose chunks must not be found, e.g. the object a similarity search starts from.
 * @param unscoped
 *        Types whose chunks pass the scope restriction without a scope: types without a scope rule,
 *        whose objects are checked one by one after the search.
 */
public record IndexFilter(Set<String> types, Set<String> scopes, Map<String, Set<String>> keywords,
		Set<String> excluded, Set<String> unscoped) {

	/** A filter that lets every chunk pass. */
	public static final IndexFilter NONE = new IndexFilter(Set.of(), null, Map.of(), Set.of(), Set.of());

	/**
	 * A filter without types exempt from the scope restriction.
	 */
	public IndexFilter(Set<String> types, Set<String> scopes, Map<String, Set<String>> keywords, Set<String> excluded) {
		this(types, scopes, keywords, excluded, Set.of());
	}

	/**
	 * This filter, restricted to the given scopes as well.
	 */
	public IndexFilter withScopes(Set<String> newScopes) {
		return new IndexFilter(types, newScopes, keywords, excluded, unscoped);
	}

	/**
	 * This filter, restricted to the given scopes, except for chunks of the given types.
	 */
	public IndexFilter withScopes(Set<String> newScopes, Set<String> unscopedTypes) {
		return new IndexFilter(types, newScopes, keywords, excluded, unscopedTypes);
	}

	/**
	 * This filter, excluding the given objects as well.
	 */
	public IndexFilter excluding(Set<String> objects) {
		return new IndexFilter(types, scopes, keywords, objects, unscoped);
	}

	/**
	 * The filter as a Lucene query, or <code>null</code> when it lets every chunk pass.
	 */
	public Query toQuery() {
		if (scopes != null && scopes.isEmpty() && unscoped.isEmpty()) {
			return new MatchNoDocsQuery("No readable scope.");
		}
		List<Query> required = new ArrayList<>();
		if (!types.isEmpty()) {
			required.add(anyOf(IndexSchema.TYPE, types));
		}
		if (scopes != null) {
			if (unscoped.isEmpty()) {
				required.add(anyOf(IndexSchema.SCOPE, scopes));
			} else {
				BooleanQuery.Builder scoped = new BooleanQuery.Builder();
				if (!scopes.isEmpty()) {
					scoped.add(anyOf(IndexSchema.SCOPE, scopes), Occur.SHOULD);
				}
				scoped.add(anyOf(IndexSchema.TYPE, unscoped), Occur.SHOULD);
				scoped.setMinimumNumberShouldMatch(1);
				required.add(scoped.build());
			}
		}
		for (Map.Entry<String, Set<String>> keyword : keywords.entrySet()) {
			if (keyword.getValue().isEmpty()) {
				return new MatchNoDocsQuery("No value of keyword " + keyword.getKey() + ".");
			}
			required.add(anyOf(IndexSchema.keywordField(keyword.getKey()), keyword.getValue()));
		}
		if (required.isEmpty() && excluded.isEmpty()) {
			return null;
		}
		BooleanQuery.Builder result = new BooleanQuery.Builder();
		if (required.isEmpty()) {
			result.add(new MatchAllDocsQuery(), Occur.FILTER);
		}
		for (Query query : required) {
			result.add(query, Occur.FILTER);
		}
		if (!excluded.isEmpty()) {
			result.add(anyOf(IndexSchema.OBJECT, excluded), Occur.MUST_NOT);
		}
		return result.build();
	}

	private static Query anyOf(String field, Collection<String> values) {
		List<BytesRef> terms = new ArrayList<>(values.size());
		for (String value : values) {
			terms.add(new BytesRef(value));
		}
		return new TermInSetQuery(field, terms);
	}

}
