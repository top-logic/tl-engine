/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.tools;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.tl.ai.index.AiIndex;
import com.top_logic.tl.ai.index.AiIndexService;
import com.top_logic.tl.ai.index.query.Hit;
import com.top_logic.tl.ai.index.query.Hits;
import com.top_logic.tl.ai.index.query.SearchMode;
import com.top_logic.tl.ai.index.query.SearchRefusedException;
import com.top_logic.tl.ai.index.query.SearchRequest;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * The agent tools of the index: {@code index.search} and {@code index.similar}.
 *
 * <p>
 * Both read under the run's rights - the same {@link ModelSecurity} every other read tool uses - and
 * put every hit on the run's reading list, so the answer may cite it. Their answers are business
 * data and are marked as such.
 * </p>
 */
public class IndexTools {

	private final Supplier<AiIndex> _index;

	private final boolean _ofService;

	/**
	 * The tools of the application's {@link AiIndexService}.
	 */
	public IndexTools() {
		_index = () -> AiIndexService.getInstance().index();
		_ofService = true;
	}

	/**
	 * The tools of the given index.
	 */
	public IndexTools(Supplier<AiIndex> index) {
		_index = index;
		_ofService = false;
	}

	/**
	 * Searches the index.
	 */
	@ToolMethod(description = "Search the application's objects by words and by meaning in one query. It finds an"
		+ " object by what its text is about, also when the text uses other words or another language: a German"
		+ " question finds an English text. Answers the best objects you may read, best first, each with its id,"
		+ " the passage that matched and where that passage is (the page of a document). Use it to find objects by"
		+ " their content; use a query on attributes for exact conditions.",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ, unavailable = "unavailable")
	public String search(
			@P("What to look for, in your own words or the user's; any language") String query,
			@P(value = "Qualified type names to search, e.g. [\"my:Statement\"]; omit for every indexed type",
				required = false) List<String> types,
			@P(value = "Ids of scope objects to search in, e.g. a project's id; omit for every scope you may read",
				required = false) List<String> scopes,
			@P(value = "Keyword filters as \"name=value\", e.g. [\"kind=RISK\"]; several values of one name are"
				+ " alternatives", required = false) List<String> filters,
			@P(value = "\"hybrid\" (default) for words and meaning, \"words\" for exact terms and codes, \"meaning\""
				+ " for paraphrases", required = false) String mode,
			@P(value = "How many objects to answer, default 10, at most 50", required = false) Integer limit,
			ToolExecutionContext context) {
		if (query == null || query.isBlank()) {
			return "error: Give the words to search for in 'query'.";
		}
		SearchMode searchMode = mode(mode);
		if (searchMode == null) {
			return "error: Unknown mode '" + mode + "'. Use hybrid, words or meaning.";
		}
		SearchRequest request = request(query, types, scopes, filters, searchMode, limit);
		if (request == null) {
			return "error: A filter must be written as name=value, e.g. kind=RISK.";
		}
		try {
			Hits hits = _index.get().search(request, ReadFilter.of(context).security());
			return render("Search for '" + query.strip() + "'", hits);
		} catch (SearchRefusedException ex) {
			return "error: " + ex.getMessage();
		}
	}

	/**
	 * Finds objects near a given one.
	 */
	@ToolMethod(description = "Find the objects whose content is closest to a given object's - in meaning and in"
		+ " their characteristic words - without a query: duplicates, related statements, the documents about"
		+ " the same thing. Answers the objects you may read, best first, each with its id and the passage that"
		+ " matched. The given object itself is not in the answer.",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ, unavailable = "unavailable")
	public String similar(
			@P("The id of the object to start from, e.g. \"GenericObject:4711\"") String id,
			@P(value = "Qualified type names to search, e.g. [\"my:Statement\"]; omit for every indexed type",
				required = false) List<String> types,
			@P(value = "Ids of scope objects to search in; omit for every scope you may read", required = false)
			List<String> scopes,
			@P(value = "Keyword filters as \"name=value\"", required = false) List<String> filters,
			@P(value = "How many objects to answer, default 10, at most 50", required = false) Integer limit,
			ToolExecutionContext context) {
		ModelSecurity security = ReadFilter.of(context).security();
		TLObject object;
		try {
			object = TidResolver.resolve(id, security);
		} catch (IllegalArgumentException ex) {
			return "error: " + ex.getMessage();
		}
		SearchRequest request = request(null, types, scopes, filters, SearchMode.HYBRID, limit);
		if (request == null) {
			return "error: A filter must be written as name=value, e.g. kind=RISK.";
		}
		try {
			Hits hits = _index.get().similar(object, request, security);
			ReadFilter.shown(object);
			return render("Objects similar to " + ReadFilter.labelled(object), hits);
		} catch (SearchRefusedException ex) {
			return "error: " + ex.getMessage();
		}
	}

	/**
	 * Why the tools can do nothing in this run, or <code>null</code>.
	 */
	public String unavailable(ToolExecutionContext context) {
		if (_ofService && !AiIndexService.Module.INSTANCE.isActive()) {
			return "This application has no search index.";
		}
		return null;
	}

	private static SearchMode mode(String mode) {
		if (mode == null || mode.isBlank()) {
			return SearchMode.HYBRID;
		}
		try {
			return SearchMode.valueOf(mode.strip().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static SearchRequest request(String query, List<String> types, List<String> scopes, List<String> filters,
			SearchMode mode, Integer limit) {
		Map<String, Set<String>> keywords = new HashMap<>();
		if (filters != null) {
			for (String filter : filters) {
				int equals = filter.indexOf('=');
				if (equals <= 0) {
					return null;
				}
				keywords.computeIfAbsent(filter.substring(0, equals).strip(), x -> new LinkedHashSet<>())
					.add(filter.substring(equals + 1).strip());
			}
		}
		Set<String> scopeIds = new LinkedHashSet<>();
		if (scopes != null) {
			for (String scope : scopes) {
				scopeIds.add(TidResolver.objectId(scope));
			}
		}
		return new SearchRequest(query, types == null ? Set.of() : Set.copyOf(types), scopeIds, keywords, mode,
			limit == null ? SearchRequest.DEFAULT_LIMIT : limit, 0, 0);
	}

	/**
	 * The answer: one entry per hit with its id, where the passage is and the passage itself, then
	 * what the search could not see.
	 */
	static String render(String title, Hits hits) {
		StringBuilder out = new StringBuilder();
		out.append(title).append(": ");
		if (hits.hits().isEmpty()) {
			out.append("nothing found that you may read.");
		} else {
			out.append(hits.hits().size()).append(hits.hits().size() == 1 ? " object" : " objects")
				.append(", best first (").append(describe(hits.mode())).append(").");
		}
		int number = 0;
		for (Hit hit : hits.hits()) {
			out.append("\n\n").append(++number).append(". ").append(ReadFilter.withId(hit.label(), hit.id()))
				.append(" - ").append(hit.type()).append(", text '").append(hit.part()).append('\'');
			if (hit.locator().page() > 0) {
				out.append(", page ").append(hit.locator().page());
			}
			out.append(", characters ").append(hit.locator().start()).append('-').append(hit.locator().end());
			if (hit.stale()) {
				out.append(" (changed since it was indexed; the new state is on its way)");
			}
			out.append('\n').append(hit.text().strip());
		}
		if (hits.moreAvailable()) {
			out.append("\n\nMore objects match: narrow the search or ask for a larger limit.");
		}
		if (hits.pendingRevisions() > 0) {
			out.append("\n\nThe index does not hold the newest ").append(hits.pendingRevisions())
				.append(hits.pendingRevisions() == 1 ? " commit" : " commits")
				.append(" yet; an object changed seconds ago may be missing or out of date.");
		}
		if (hits.notice() != null) {
			out.append("\n\n").append(hits.notice());
		}
		return out.toString();
	}

	private static String describe(SearchMode mode) {
		return switch (mode) {
			case HYBRID -> "by words and meaning";
			case WORDS -> "by words";
			case MEANING -> "by meaning";
		};
	}

}
