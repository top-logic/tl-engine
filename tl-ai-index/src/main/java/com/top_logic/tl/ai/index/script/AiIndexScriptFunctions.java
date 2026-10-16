/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.index.AiIndexService;
import com.top_logic.tl.ai.index.config.IndexedType;
import com.top_logic.tl.ai.index.query.Hit;
import com.top_logic.tl.ai.index.query.Hits;
import com.top_logic.tl.ai.index.query.SearchMode;
import com.top_logic.tl.ai.index.query.SearchRequest;
import com.top_logic.tl.ai.index.sync.IndexStatus;
import com.top_logic.util.TLContext;

/**
 * TL-Script functions of the AI index.
 *
 * <p>
 * A search runs with the rights of the session's user and is refused in a system context.
 * </p>
 */
@ScriptPrefix("aiIndex")
public class AiIndexScriptFunctions extends TLScriptFunctions {

	/**
	 * Searches the application's index by words and meaning.
	 *
	 * <p>
	 * Answers the objects the current user may read, best first. A German query finds an English
	 * text. Example: <code>aiIndexSearch("Grundplatte", {"types": ["my:Statement"], "limit": 5})</code>.
	 * </p>
	 *
	 * @param query
	 *        What to look for.
	 * @param options
	 *        Optional: {@code types} (qualified type names), {@code scopes} (scope objects),
	 *        {@code filters} (keyword name to value or list of values), {@code mode} ({@code "hybrid"},
	 *        {@code "words"} or {@code "meaning"}), {@code limit} (at most 50).
	 * @return The objects found, best first.
	 */
	public static List<TLObject> search(@Mandatory String query, Map<String, Object> options) {
		Hits hits = AiIndexService.getInstance().index().search(request(query, options), security());
		return objects(hits);
	}

	/**
	 * Finds objects whose content is close to the given object's.
	 *
	 * <p>
	 * Example: <code>aiIndexSimilar($statement, {"types": ["my:Statement"], "limit": 10})</code>.
	 * </p>
	 *
	 * @param object
	 *        The object to start from.
	 * @param options
	 *        Optional, as for {@code aiIndexSearch}, without {@code mode}.
	 * @return The objects found, best first, without the given one.
	 */
	public static List<TLObject> similar(@Mandatory TLObject object, Map<String, Object> options) {
		Hits hits = AiIndexService.getInstance().index().similar(object, request(null, options), security());
		return objects(hits);
	}

	/**
	 * Where the index stands.
	 *
	 * @return A dictionary with {@code ready}, {@code indexedRevision}, {@code headRevision},
	 *         {@code pendingRevisions}, {@code rebuilding}, {@code buildTotal}, {@code buildDone},
	 *         {@code meaning}, {@code lastError} and {@code embedderProblem}.
	 */
	public static Map<String, Object> status() {
		AiIndexService service = AiIndexService.getInstance();
		IndexStatus status = service.index().status();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("ready", status.ready());
		result.put("indexedRevision", status.indexedRevision());
		result.put("headRevision", status.headRevision());
		result.put("pendingRevisions", status.pendingRevisions());
		result.put("rebuilding", status.rebuilding());
		result.put("buildTotal", status.buildTotal());
		result.put("buildDone", status.buildDone());
		result.put("meaning", status.meaning());
		result.put("lastError", status.lastError());
		result.put("embedderProblem", service.embedderProblem());
		return result;
	}

	private static ModelAccessRightsSecurity security() {
		Person user = TLContext.currentUser();
		return new ModelAccessRightsSecurity(user);
	}

	private static List<TLObject> objects(Hits hits) {
		List<TLObject> result = new ArrayList<>(hits.hits().size());
		for (Hit hit : hits.hits()) {
			result.add(hit.object());
		}
		return result;
	}

	static SearchRequest request(String query, Map<String, Object> options) {
		Map<String, Object> given = options == null ? Map.of() : options;
		Set<String> types = strings(given.get("types"));
		Set<String> scopes = new LinkedHashSet<>();
		for (Object scope : values(given.get("scopes"))) {
			scopes.add(scope instanceof TLObject object ? ReadFilter.idOf(object) : String.valueOf(scope));
		}
		Map<String, Set<String>> filters = new LinkedHashMap<>();
		if (given.get("filters") instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				// As the index stores keywords: an enumeration value by its name.
				filters.put(String.valueOf(entry.getKey()), new LinkedHashSet<>(IndexedType.keywordValues(entry.getValue())));
			}
		}
		Object mode = given.get("mode");
		SearchMode searchMode =
			mode == null ? SearchMode.HYBRID : SearchMode.valueOf(String.valueOf(mode).toUpperCase(Locale.ROOT));
		Object limit = given.get("limit");
		int pageSize = limit instanceof Number number ? number.intValue() : SearchRequest.DEFAULT_LIMIT;
		return new SearchRequest(query, types, scopes, filters, searchMode, pageSize, 0, 0);
	}

	private static Set<String> strings(Object value) {
		Set<String> result = new LinkedHashSet<>();
		for (Object element : values(value)) {
			result.add(element instanceof TLObject object ? ReadFilter.idOf(object) : String.valueOf(element));
		}
		return result;
	}

	private static Collection<?> values(Object value) {
		if (value == null) {
			return List.of();
		}
		return value instanceof Collection<?> many ? many : List.of(value);
	}

}
