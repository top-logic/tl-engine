/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.Access;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.DownloadAccess;
import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.OriginRule;

/**
 * One resolved browsing policy: the immutable answer to "may this run reach that origin, and may a
 * page there hand it a file".
 *
 * <p>
 * Built once from the configuration (see {@link BrowsingPolicies}) and then a pure function of a
 * host. A user holding several roles is bound by all of their policies at once, which is why this
 * holds a <em>list</em> of sections: an origin is permitted only when every section permits it, so
 * holding one more role can never widen what a run reaches.
 * </p>
 *
 * <p>
 * This is the governance layer and says which origins an installation has agreed its agents may
 * browse. It is not the SSRF check: {@link UrlSafety} decides whether an address may be connected to
 * at all, is enforced independently, and nothing here can relax it.
 * </p>
 */
public final class BrowsingPolicy {

	/** The policy of an installation that has configured none: every public origin, downloads and all. */
	private static final BrowsingPolicy UNRESTRICTED = new BrowsingPolicy(List.of());

	/**
	 * What one configured policy says about one host.
	 *
	 * @param access
	 *        Whether the host may be reached.
	 * @param downloads
	 *        Whether a page at the host may start a download. Never {@link Access#ALLOW} when
	 *        {@code access} refuses.
	 */
	private record Verdict(Access access, Access downloads) {
		// Nothing but the two axes.
	}

	/**
	 * One origin rule, with its pattern pre-parsed.
	 *
	 * @param pattern
	 *        The pattern as the administrator wrote it, for the refusal message and the listings.
	 * @param suffix
	 *        The dot-prefixed domain a wildcard pattern matches the end of ({@code .example.com}),
	 *        or {@code null} for an exact-host rule.
	 * @param host
	 *        The host an exact rule matches, or {@code null} for a wildcard rule.
	 */
	private record Rule(String pattern, String suffix, String host, Access access, Access downloads) {

		boolean matches(String normalizedHost) {
			if (suffix != null) {
				return normalizedHost.endsWith(suffix);
			}
			return normalizedHost.equals(host);
		}
	}

	/**
	 * One configured policy, as this object applies it.
	 *
	 * @param name
	 *        The configured name, so a refusal can say which policy refused.
	 */
	private record Section(String name, Access access, Access downloads, List<Rule> rules) {

		/**
		 * The verdict for {@code normalizedHost}.
		 *
		 * <p>
		 * Deny beats allow, whatever order the rules are written in: a matching rule that refuses
		 * settles it, and only when none does can a matching rule that permits. That makes the
		 * outcome independent of rule order, at the price of not being able to carve an allowed host
		 * out of a denied wildcard - which is expressed the other way round, as a deny-by-default
		 * policy with the host allowed.
		 * </p>
		 */
		Verdict verdict(String normalizedHost) {
			boolean matched = false;
			boolean downloadsRefused = false;
			for (Rule rule : rules) {
				if (!rule.matches(normalizedHost)) {
					continue;
				}
				if (rule.access() == Access.DENY) {
					return new Verdict(Access.DENY, Access.DENY);
				}
				matched = true;
				downloadsRefused |= rule.downloads() == Access.DENY;
			}
			if (matched) {
				return new Verdict(Access.ALLOW, downloadsRefused ? Access.DENY : Access.ALLOW);
			}
			if (access == Access.DENY) {
				return new Verdict(Access.DENY, Access.DENY);
			}
			return new Verdict(Access.ALLOW, downloads);
		}

		/** Whether this section reaches only the origins its rules name. */
		boolean bounded() {
			return access == Access.DENY;
		}

		/** The patterns this section permits, for the message that tells the model where it may go. */
		List<String> allowPatterns() {
			List<String> patterns = new ArrayList<>();
			for (Rule rule : rules) {
				if (rule.access() == Access.ALLOW) {
					patterns.add(rule.pattern());
				}
			}
			return patterns;
		}

		/** The patterns this section refuses. */
		List<String> denyPatterns() {
			List<String> patterns = new ArrayList<>();
			for (Rule rule : rules) {
				if (rule.access() == Access.DENY) {
					patterns.add(rule.pattern());
				}
			}
			return patterns;
		}

		/** What this section permits, in one phrase an agent can act on. */
		String describe() {
			if (bounded()) {
				List<String> allowed = allowPatterns();
				return allowed.isEmpty() ? "nothing" : String.join(", ", allowed);
			}
			List<String> denied = denyPatterns();
			return denied.isEmpty() ? "every public origin"
				: "every public origin except " + String.join(", ", denied);
		}
	}

	private final List<Section> _sections;

	private BrowsingPolicy(List<Section> sections) {
		_sections = sections;
	}

	/**
	 * The policy of an installation that has configured none: every origin the SSRF check permits.
	 */
	public static BrowsingPolicy unrestricted() {
		return UNRESTRICTED;
	}

	/**
	 * The policy one configured entry describes.
	 *
	 * @param config
	 *        The configured policy.
	 * @return The resolved policy; never {@code null}.
	 */
	public static BrowsingPolicy of(BrowsingPolicyConfig.Policy config) {
		List<Rule> rules = new ArrayList<>();
		for (OriginRule origin : config.getOrigins()) {
			Rule rule = ruleOf(origin);
			if (rule != null) {
				rules.add(rule);
			}
		}
		Access downloads = resolve(config.getDownloads(), config.getAccess());
		return new BrowsingPolicy(
			List.of(new Section(config.getName(), config.getAccess(), downloads, List.copyOf(rules))));
	}

	/**
	 * The policy binding a run to all of {@code parts} at once.
	 *
	 * @param parts
	 *        The policies that apply; an empty list yields {@link #unrestricted()}.
	 * @return The combined policy; never {@code null}.
	 */
	public static BrowsingPolicy all(List<BrowsingPolicy> parts) {
		List<Section> sections = new ArrayList<>();
		for (BrowsingPolicy part : parts) {
			sections.addAll(part._sections);
		}
		if (sections.isEmpty()) {
			return UNRESTRICTED;
		}
		return new BrowsingPolicy(List.copyOf(sections));
	}

	/** The parsed form of one configured rule, or {@code null} when it names no host. */
	private static Rule ruleOf(OriginRule origin) {
		String pattern = UrlSafety.normalizeHost(origin.getHost());
		if (pattern == null || pattern.isEmpty()) {
			return null;
		}
		Access access = origin.getAccess();
		Access downloads = resolve(origin.getDownloads(), access);
		if (pattern.startsWith("*.")) {
			String domain = pattern.substring(1);
			if (domain.length() < 2) {
				return null;
			}
			return new Rule(pattern, domain, null, access, downloads);
		}
		return new Rule(pattern, null, pattern, access, downloads);
	}

	/** The download axis of an entry, with "as the access axis says" resolved to what that says. */
	private static Access resolve(DownloadAccess downloads, Access access) {
		return switch (downloads) {
			case ALLOW -> Access.ALLOW;
			case DENY -> Access.DENY;
			case AS_ACCESS -> access;
		};
	}

	/** Whether this policy refuses nothing, so no origin check has to be made at all. */
	public boolean isUnrestricted() {
		return _sections.isEmpty();
	}

	/**
	 * The name of this policy, for a log line and for the identity of the browser proxy it gets.
	 *
	 * @return The configured name, the names joined by {@code +} when several apply, or
	 *         {@code "(unrestricted)"}.
	 */
	public String name() {
		if (_sections.isEmpty()) {
			return "(unrestricted)";
		}
		List<String> names = new ArrayList<>();
		for (Section section : _sections) {
			names.add(section.name());
		}
		return String.join("+", names);
	}

	/**
	 * Why this policy refuses {@code host}, or {@code null} when it permits it.
	 *
	 * <p>
	 * The message is written for the model: it names the origin, says the refusal is about the
	 * acting user's role rather than about the site, and lists where the run may go instead, so the
	 * agent can act on it rather than retry the same URL.
	 * </p>
	 *
	 * @param host
	 *        The destination host; any spelling, it is normalised here.
	 */
	public String refuseAccess(String host) {
		String normalized = normalized(host);
		if (normalized == null) {
			return _sections.isEmpty() ? null : "the request names no host.";
		}
		for (Section section : _sections) {
			if (section.verdict(normalized).access() == Access.DENY) {
				return refusal(normalized, section);
			}
		}
		return null;
	}

	/**
	 * Why this policy refuses a download from {@code host}, or {@code null} when it permits one.
	 *
	 * @param host
	 *        The host the download comes from; any spelling, it is normalised here.
	 */
	public String refuseDownload(String host) {
		String normalized = normalized(host);
		if (normalized == null) {
			return _sections.isEmpty() ? null : "the download names no host.";
		}
		for (Section section : _sections) {
			Verdict verdict = section.verdict(normalized);
			if (verdict.access() == Access.DENY) {
				return refusal(normalized, section);
			}
			if (verdict.downloads() == Access.DENY) {
				return "downloading from the origin '" + normalized + "' is not allowed for your role"
					+ " (browsing policy '" + section.name() + "'). The file was not saved.";
			}
		}
		return null;
	}

	private String refusal(String normalizedHost, Section section) {
		return "the origin '" + normalizedHost + "' is not allowed for your role (browsing policy '"
			+ section.name() + "'); allowed: " + section.describe() + ".";
	}

	/**
	 * What this policy permits, in one phrase, for a message to the model and for the service log.
	 */
	public String describeAllowed() {
		if (_sections.isEmpty()) {
			return "every public origin";
		}
		List<String> parts = new ArrayList<>();
		for (Section section : _sections) {
			parts.add(section.describe());
		}
		return String.join("; and ", parts);
	}

	/**
	 * Whether this policy reaches only origins it names, so a web search has a finite ceiling.
	 */
	public boolean bounded() {
		for (Section section : _sections) {
			if (section.bounded()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The domains a bounded policy permits, as a search engine's include list wants them.
	 *
	 * <p>
	 * A wildcard pattern becomes its bare domain, because a search engine matches a domain and its
	 * sub-domains and has no wildcard form. That is wider than the policy, which is why what comes
	 * back is filtered through {@link #refuseAccess(String)} afterwards rather than trusted.
	 * </p>
	 *
	 * @return The include list, empty when this policy is not {@link #bounded()}.
	 */
	public List<String> allowedSearchDomains() {
		Set<String> domains = new LinkedHashSet<>();
		for (Section section : _sections) {
			if (!section.bounded()) {
				continue;
			}
			for (String pattern : section.allowPatterns()) {
				domains.add(bareDomain(pattern));
			}
		}
		return List.copyOf(domains);
	}

	/**
	 * The domains this policy refuses, as a search engine's exclude list wants them.
	 *
	 * @return The exclude list; empty when no rule refuses anything.
	 */
	public List<String> blockedSearchDomains() {
		Set<String> domains = new LinkedHashSet<>();
		for (Section section : _sections) {
			for (String pattern : section.denyPatterns()) {
				domains.add(bareDomain(pattern));
			}
		}
		return List.copyOf(domains);
	}

	private static String bareDomain(String pattern) {
		return pattern.startsWith("*.") ? pattern.substring(2) : pattern;
	}

	/** The one spelling every layer compares, or {@code null} when no host was named. */
	private static String normalized(String host) {
		String normalized = UrlSafety.normalizeHost(host);
		if (normalized == null || normalized.isEmpty()) {
			return null;
		}
		if (normalized.length() > 1 && normalized.charAt(0) == '['
				&& normalized.charAt(normalized.length() - 1) == ']') {
			return normalized.substring(1, normalized.length() - 1);
		}
		return normalized;
	}
}
