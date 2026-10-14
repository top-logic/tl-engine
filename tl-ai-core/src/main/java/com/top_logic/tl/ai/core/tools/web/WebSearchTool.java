/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;

/**
 * Tool that searches the web via the configured {@link WebSearchEngine}.
 *
 * <p>
 * A ranked result list is the cheapest prompt-injection channel there is: the attacker needs no
 * access to anything, only a page that ranks for a query the agent is likely to make. So what the
 * engine returned - its answer, and every title, URL and excerpt - comes back inside the
 * {@link UntrustedEnvelope#SEARCH_RESULTS untrusted-content envelope}.
 * </p>
 *
 * <p>
 * The tool's own two sentences stay <em>outside</em> it: the line naming the query, and the standing
 * instruction to cite sources as markdown links. That instruction is the application talking to the
 * model, and putting it inside a block the model is told to treat as data would tell the model to
 * ignore it.
 * </p>
 *
 * <p>
 * The {@code allowedDomains} / {@code blockedDomains} arguments are <b>model-supplied narrowing</b>
 * and nothing more. The ceiling is the acting role's {@link BrowsingPolicy}, configured on
 * {@link WebToolsService}: what an agent passes can only take origins away from what that policy
 * permits, never add one. The narrowing is applied twice - once as the engine's own domain filters,
 * which saves a pointless round trip, and once on what comes back, which is the half that actually
 * binds, because a search engine is a third party and its filters are a request, not a guarantee.
 * </p>
 *
 * <p>
 * {@link ToolMode#READ} is deliberate and says only that the search changes nothing in this
 * application. That it sends the query to a third party is the group's statement, not the mode's -
 * see {@link ToolGroups#EGRESS}, which is where an administrator gates outbound access.
 * </p>
 */
public class WebSearchTool {

	/** An ISO 639-1 code, optionally with a region or script, e.g. {@code de} or {@code zh-hant}. */
	private static final Pattern LANGUAGE = Pattern.compile("[a-z]{2}(-[a-z]{2,4})?");

	/**
	 * Creates a {@link WebSearchTool}.
	 */
	public WebSearchTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	/**
	 * Searches the web and returns ranked results.
	 *
	 * @param query
	 *        The search query.
	 * @param allowedDomains
	 *        Optional domain allow-list. May be {@code null}.
	 * @param blockedDomains
	 *        Optional domain block-list. May be {@code null}.
	 * @param count
	 *        How many results to return; {@code null} for the configured default.
	 * @param freshness
	 *        How recent the results must be ({@code day}, {@code week}, {@code month}, {@code year});
	 *        {@code null} for any time.
	 * @param language
	 *        The language of the results as an ISO 639-1 code; {@code null} for any language.
	 * @return Formatted results, or an error string.
	 */
	@ToolMethod(
		description = "Search the web and return ranked results (title, URL, snippet). "
			+ "Use this to find current information or sources on the public internet. "
			+ "A result is not read until you open it with webFetch; "
			+ "cite the pages you read as markdown links.",
		mode = ToolMode.READ, group = ToolGroups.EGRESS,
		marking = AnswerMarking.TOOL, unavailable = "noEngine")
	public String webSearch(
			@P("The search query.") String query,
			@P(value = "Optional: restrict results to these domains, e.g. [\"docs.oracle.com\"]. "
				+ "This can only NARROW what the administrator's browsing policy already permits - "
				+ "naming a domain that policy forbids does not make it reachable.",
				required = false) List<String> allowedDomains,
			@P(value = "Optional: exclude results from these domains. Do not combine with allowedDomains.",
				required = false) List<String> blockedDomains,
			@P(value = "Optional: how many results to return, 1 to " + WebSearchQuery.MAX_COUNT
				+ ". Leave it out for the default.", required = false) Integer count,
			@P(value = "Optional: only results published in the last day, week, month or year. "
				+ "One of: day, week, month, year. Leave it out for any time.",
				required = false) String freshness,
			@P(value = "Optional: only results in this language, as a two-letter ISO 639-1 code, "
				+ "e.g. \"de\" or \"en\". Leave it out for any language.",
				required = false) String language) {
		String unavailable = noEngine(null);
		if (unavailable != null) {
			return "Error: " + unavailable;
		}
		WebToolsService service = WebToolsService.getInstance();
		return search(service.getSearchEngine(), service.getMaxResults(), WebToolsService.currentPolicy(), query,
			allowedDomains, blockedDomains, count, freshness, language);
	}

	/**
	 * Runs one search on {@code engine}, after checking the arguments the model wrote. Package-visible
	 * for testing.
	 *
	 * @param engine
	 *        The configured engine.
	 * @param defaultCount
	 *        The number of results when the model gave none.
	 * @param browsing
	 *        The policy of the acting role.
	 * @param query
	 *        See {@link #webSearch(String, List, List, Integer, String, String)}.
	 * @param allowedDomains
	 *        See {@link #webSearch(String, List, List, Integer, String, String)}.
	 * @param blockedDomains
	 *        See {@link #webSearch(String, List, List, Integer, String, String)}.
	 * @param count
	 *        See {@link #webSearch(String, List, List, Integer, String, String)}.
	 * @param freshness
	 *        See {@link #webSearch(String, List, List, Integer, String, String)}.
	 * @param language
	 *        See {@link #webSearch(String, List, List, Integer, String, String)}.
	 * @return The tool's answer.
	 */
	static String search(WebSearchEngine engine, int defaultCount, BrowsingPolicy browsing, String query,
			List<String> allowedDomains, List<String> blockedDomains, Integer count, String freshness,
			String language) {
		if (query == null || query.isBlank()) {
			return "Error: query is required.";
		}
		boolean hasAllow = allowedDomains != null && !allowedDomains.isEmpty();
		boolean hasBlock = blockedDomains != null && !blockedDomains.isEmpty();
		if (hasAllow && hasBlock) {
			return "Error: provide either allowedDomains or blockedDomains, not both.";
		}
		if (count != null && (count.intValue() < 1 || count.intValue() > WebSearchQuery.MAX_COUNT)) {
			return "Error: count must be between 1 and " + WebSearchQuery.MAX_COUNT + ".";
		}
		Freshness age = null;
		if (freshness != null && !freshness.isBlank()) {
			age = Freshness.of(freshness);
			if (age == null) {
				return "Error: freshness must be one of day, week, month, year.";
			}
		}
		String lang = null;
		if (language != null && !language.isBlank()) {
			lang = language.strip().toLowerCase(Locale.ROOT);
			if (!LANGUAGE.matcher(lang).matches()) {
				return "Error: language must be a two-letter ISO 639-1 code, e.g. \"de\".";
			}
		}

		SearchScope scope = narrow(browsing, allowedDomains, blockedDomains);
		if (scope.refusal() != null) {
			return "Error: " + BrowsingPolicies.audit(browsing, String.join(", ", allowedDomains),
				scope.refusal());
		}

		List<String> notes = new ArrayList<>();
		if (age != null && !engine.supportsFreshness()) {
			notes.add("The configured search engine cannot restrict results by age; "
				+ "the results are of any age.");
			age = null;
		}
		if (lang != null && !engine.supportsLanguage()) {
			notes.add("The configured search engine cannot restrict results by language; "
				+ "the results are in any language.");
			lang = null;
		}
		int wanted = count != null ? count.intValue()
			: Math.max(1, Math.min(defaultCount, WebSearchQuery.MAX_COUNT));
		WebSearchQuery search = new WebSearchQuery(query, scope.include(), scope.exclude(), wanted, age, lang);
		try {
			WebSearchResults results = onPolicyOnly(browsing, engine.search(search));
			// The result addresses are addresses this run was given; the query it wrote is not.
			for (WebSearchHit hit : results.hits()) {
				UrlProvenance.record(hit.url());
			}
			String answer = format(query, results);
			return notes.isEmpty() ? answer : answer + "\n\n" + String.join("\n", notes);
		} catch (CancellationException ex) {
			return "Error: web search was cancelled.";
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return "Error: web search was interrupted.";
		} catch (Exception ex) {
			Logger.error("Web search failed: " + ex.getMessage(), ex, WebSearchTool.class);
			return "Error: web search failed: " + ex.getMessage();
		}
	}

	/**
	 * Why {@code webSearch} can do nothing in this application: no search engine is configured.
	 *
	 * <p>
	 * Nothing is configured by default, because a search engine is a paid third-party service that
	 * an administrator chooses. The tool is then not offered at all, and a call that names it anyway
	 * is answered with this sentence.
	 * </p>
	 *
	 * @param context
	 *        The run; not used, because the answer is the same for every run.
	 * @return The reason, or {@code null} when an engine is configured.
	 */
	String noEngine(ToolExecutionContext context) {
		WebToolsService service = WebToolsService.getInstance();
		if (service != null && service.getSearchEngine() != null) {
			return null;
		}
		return "web search is not configured in this application: no search engine is set on the web "
			+ "tools service. Work without web search, and tell the user that an administrator can "
			+ "configure a search engine (Brave or Tavily) on the web tools service.";
	}

	/**
	 * The domain filters one search is actually run with.
	 *
	 * @param include
	 *        The domains results are restricted to, empty for no restriction.
	 * @param exclude
	 *        The domains results are kept out of, empty for none.
	 * @param refusal
	 *        Why the search cannot be run at all, or {@code null} when it can.
	 */
	record SearchScope(List<String> include, List<String> exclude, String refusal) {
		// Value type only.
	}

	/**
	 * Narrows what the model asked for to what the browsing policy permits. Package-visible for
	 * testing.
	 *
	 * <p>
	 * The rule is one-directional: a domain the model names is kept only if the policy already
	 * permits it, and a policy that reaches only the origins it names contributes those as the
	 * include list when the model named none. A model that names nothing but forbidden domains is
	 * told so rather than silently searched for on the whole web, which is the one case where
	 * dropping its list would <em>widen</em> the search instead of narrowing it.
	 * </p>
	 *
	 * @param browsing
	 *        The policy of the acting role.
	 * @param allowedDomains
	 *        What the model asked to be restricted to; may be {@code null}.
	 * @param blockedDomains
	 *        What the model asked to be kept out; may be {@code null}.
	 * @return The scope; never {@code null}.
	 */
	static SearchScope narrow(BrowsingPolicy browsing, List<String> allowedDomains,
			List<String> blockedDomains) {
		List<String> ceiling = browsing.allowedSearchDomains();
		Set<String> exclude = new LinkedHashSet<>();
		if (blockedDomains != null) {
			exclude.addAll(blockedDomains);
		}
		exclude.addAll(browsing.blockedSearchDomains());

		List<String> requested = nonBlank(allowedDomains);
		if (requested.isEmpty()) {
			// Nothing to narrow: the policy's own ceiling is the include list, if it has one.
			return new SearchScope(ceiling, List.copyOf(exclude), null);
		}
		List<String> permitted = new ArrayList<>();
		for (String domain : requested) {
			if (permits(browsing, ceiling, domain)) {
				permitted.add(domain);
			}
		}
		if (permitted.isEmpty()) {
			return new SearchScope(List.of(), List.of(),
				"none of the domains you asked for is allowed for your role; allowed: "
					+ browsing.describeAllowed() + ".");
		}
		return new SearchScope(List.copyOf(permitted), List.copyOf(exclude), null);
	}

	/**
	 * Whether the policy permits a domain the model named.
	 *
	 * <p>
	 * A ceiling entry is compared literally as well as through the policy, because a wildcard rule
	 * ({@code *.example.com}) contributes {@code example.com} to the ceiling although the policy
	 * itself refuses that apex host - a search restricted to the domain is exactly what the rule
	 * means, and what comes back is filtered host by host afterwards anyway.
	 * </p>
	 */
	private static boolean permits(BrowsingPolicy browsing, List<String> ceiling, String domain) {
		if (ceiling.contains(UrlSafety.normalizeHost(domain))) {
			return true;
		}
		return browsing.refuseAccess(domain) == null;
	}

	private static List<String> nonBlank(List<String> domains) {
		List<String> out = new ArrayList<>();
		if (domains == null) {
			return out;
		}
		for (String domain : domains) {
			if (domain != null && !domain.isBlank()) {
				out.add(domain.trim());
			}
		}
		return out;
	}

	/**
	 * Drops the hits the browsing policy refuses. Package-visible for testing.
	 *
	 * <p>
	 * This is the half of the narrowing that binds. The engine was <em>asked</em> for the same
	 * restriction, but it is a third party: it may widen a domain filter, ignore it, or answer with a
	 * redirector URL, and the policy has to hold whatever it does. A hit whose URL cannot be parsed
	 * into a host is dropped too, because a URL this process cannot read is one it cannot clear.
	 * </p>
	 *
	 * @param browsing
	 *        The policy of the acting role.
	 * @param results
	 *        What the engine answered.
	 * @return The results with every off-policy hit removed.
	 */
	static WebSearchResults onPolicyOnly(BrowsingPolicy browsing, WebSearchResults results) {
		if (browsing.isUnrestricted()) {
			return results;
		}
		List<WebSearchHit> kept = new ArrayList<>();
		for (WebSearchHit hit : results.hits()) {
			String host = hostOf(hit.url());
			if (host != null && browsing.refuseAccess(host) == null) {
				kept.add(hit);
			}
		}
		if (kept.size() == results.hits().size()) {
			return results;
		}
		return new WebSearchResults(results.answer(), kept);
	}

	/** The host of a result URL, or {@code null} when it has none this process can read. */
	private static String hostOf(String url) {
		if (url == null || url.isBlank()) {
			return null;
		}
		try {
			return URI.create(url.trim()).getHost();
		} catch (RuntimeException ex) {
			return null;
		}
	}

	/**
	 * Renders what the engine returned, with everything that came from the engine inside the
	 * untrusted-content envelope. Package-visible for testing.
	 *
	 * @see UntrustedEnvelope#SEARCH_RESULTS
	 */
	static String format(String query, WebSearchResults results) {
		boolean noAnswer = results.answer() == null || results.answer().isBlank();
		if (results.hits().isEmpty() && noAnswer) {
			// Nothing came back, so there is nothing to mark - and an empty envelope would only tell
			// the model to distrust a sentence this application wrote.
			return "No results found for query: " + query;
		}
		StringBuilder body = new StringBuilder();
		if (!noAnswer) {
			body.append("Answer: ").append(results.answer().strip()).append("\n");
		}
		int index = 1;
		for (WebSearchHit hit : results.hits()) {
			body.append("\n").append(index++).append(". ").append(nullToEmpty(hit.title())).append("\n");
			body.append("   URL: ").append(nullToEmpty(hit.url())).append("\n");
			String text = hit.content() != null ? hit.content() : hit.snippet();
			if (text != null && !text.isBlank()) {
				body.append("   ").append(text.strip()).append("\n");
			}
		}

		StringBuilder out = new StringBuilder();
		out.append("Web search results for: \"").append(query).append("\"\n\n");
		out.append(UntrustedEnvelope.SEARCH_RESULTS.wrap(query, body.toString().strip()));
		out.append("\n\nOpen a result with webFetch before you rely on it, and cite the pages you read"
			+ " as markdown links.");
		return out.toString();
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
