/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.browser;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.RequestPolicy;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy;
import com.top_logic.tl.ai.core.tools.web.UrlSafety;

/**
 * Tests the domain policy {@link BrowserToolsService} applies to every connection of a browser
 * session, as the {@link FilteringProxy} decides it: the installation's allowlist and blocklist on
 * top of the SSRF check, which no setting relaxes.
 *
 * @see com.top_logic.tl.ai.core.tools.web.TestFilteringProxy
 */
public class TestBrowserRequestPolicy extends TestCase {

	/**
	 * SSRF cannot be relaxed by configuration: with {@code allow-any-domains} on - the mode in which
	 * every public domain is reachable - the metadata endpoint, loopback and the private ranges stay
	 * refused.
	 */
	public void testAllowAnyDomainsDoesNotRelaxSsrf() {
		RequestPolicy allowAny = newService(List.of(), List.of(), true, false);
		for (String host : List.of("169.254.169.254", "127.0.0.1", "10.1.2.3", "192.168.0.5")) {
			assertFalse("allow-any must not reach " + host,
				FilteringProxy.decide(allowAny, host, 80).permitted());
		}
	}

	/** The blocklist wins over everything: it refuses even in allow-any mode, and for a public host. */
	public void testBlocklistWinsInEveryMode() {
		RequestPolicy allowAnyButBlocked =
			newService(List.of(), List.of("8.8.8.8"), true, false);
		Target refused = FilteringProxy.decide(allowAnyButBlocked, "8.8.8.8", 80);
		assertFalse("the blocklist refuses a public host even in allow-any mode", refused.permitted());
		assertTrue("names the blocklist: " + refused.refusal(), refused.refusal().contains("blocklist"));

		RequestPolicy allowlistedButBlocked =
			newService(List.of("8.8.8.8"), List.of("8.8.8.8"), false, true);
		assertFalse("the blocklist wins over the allowlist",
			FilteringProxy.decide(allowlistedButBlocked, "8.8.8.8", 80).permitted());
	}

	/**
	 * With the allowlist extended to every request, a host that is not on it is refused fail-closed -
	 * an empty allowlist refuses everything, not nothing.
	 */
	public void testAllowlistIsFailClosedWhenItGovernsEveryRequest() {
		RequestPolicy emptyAllowlist = newService(List.of(), List.of(), false, true);
		assertFalse("an empty allowlist permits nothing",
			FilteringProxy.decide(emptyAllowlist, "8.8.8.8", 80).permitted());

		RequestPolicy allowlisted = newService(List.of("8.8.8.8"), List.of(), false, true);
		assertTrue("an allowlisted host is permitted",
			FilteringProxy.decide(allowlisted, "8.8.8.8", 80).permitted());
		assertFalse("a host that is not on the allowlist is refused",
			FilteringProxy.decide(allowlisted, "1.1.1.1", 80).permitted());
	}

	/**
	 * By default the allowlist stays a <em>top-level-origin</em> control: the proxy applies SSRF and the
	 * blocklist to every connection, but does not refuse a sub-resource host merely for being
	 * off-allowlist - which is what keeps an allowlisted page rendering with its third-party assets.
	 */
	public void testAllowlistDoesNotGovernEveryRequestByDefault() {
		RequestPolicy allowlisted = newService(List.of("8.8.8.8"), List.of(), false, false);
		assertTrue("an off-allowlist sub-resource host is not refused at the proxy by default",
			FilteringProxy.decide(allowlisted, "1.1.1.1", 80).permitted());
		assertFalse("SSRF still applies to every connection",
			FilteringProxy.decide(allowlisted, "127.0.0.1", 80).permitted());
		RequestPolicy blocked = newService(List.of("8.8.8.8"), List.of("1.1.1.1"), false, false);
		assertFalse("the blocklist still applies to every connection",
			FilteringProxy.decide(blocked, "1.1.1.1", 80).permitted());
	}

	/**
	 * <b>The trailing-dot bypass, at the proxy.</b> {@code blocked.example.} is the fully qualified
	 * spelling of a blocklisted host: it resolves to the same server, but it matches neither the
	 * blocklist entry nor a sub-domain of it unless the host is normalised first. The proxy normalises
	 * through the same {@link UrlSafety#normalizeHost(String)} the tool's pre-flight check uses, so
	 * the two layers cannot disagree about which host this is.
	 */
	public void testTrailingDotSpellingIsRefusedByTheDomainPolicy() {
		RequestPolicy blocking = newService(List.of(), List.of("blocked.example"), true, false);

		Target refused = FilteringProxy.decide(blocking, "blocked.example.", 80);
		assertFalse("the fully qualified spelling is the same blocked host", refused.permitted());
		assertTrue("named as a blocklist refusal: " + refused.refusal(),
			refused.refusal().contains("blocklist"));
		assertFalse("a trailing-dot sub-domain of a blocked domain too",
			FilteringProxy.decide(blocking, "www.blocked.example.", 80).permitted());
		assertFalse("upper case plus the root label",
			FilteringProxy.decide(blocking, "BLOCKED.Example.", 443).permitted());
	}

	private static BrowserToolsService newService(List<String> allowedDomains, List<String> blockedDomains,
			boolean allowAny, boolean applyAllowlistToAllRequests) {
		BrowserToolsService.Config<?> config =
			TypedConfiguration.newConfigItem(BrowserToolsService.Config.class);
		config.update(config.descriptor().getProperty(BrowserToolsService.Config.ALLOWED_DOMAINS),
			allowedDomains);
		config.update(config.descriptor().getProperty(BrowserToolsService.Config.BLOCKED_DOMAINS),
			blockedDomains);
		config.update(config.descriptor().getProperty(BrowserToolsService.Config.ALLOW_ANY_DOMAINS),
			allowAny);
		config.update(
			config.descriptor().getProperty(BrowserToolsService.Config.APPLY_ALLOWLIST_TO_ALL_REQUESTS),
			applyAllowlistToAllRequests);
		return new BrowserToolsService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}
}
