/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import static com.top_logic.tl.ai.core.tools.web.TestBrowsingPolicy.origin;
import static com.top_logic.tl.ai.core.tools.web.TestBrowsingPolicy.policy;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.tools.web.BrowsingPolicyConfig.Access;
import com.top_logic.tl.ai.core.tools.web.WebSearchTool.SearchScope;

/**
 * Tests that the browsing governance of issue #347 is actually <b>enforced</b> on a web search.
 *
 * <p>
 * Asserted as a pure function, because what has to hold there is an algebraic property - a
 * model-supplied domain list can only remove origins, never add one.
 * </p>
 *
 * @see TestBrowsingPolicy
 */
public class TestBrowsingPolicyEnforcement extends TestCase {

	/** The configuration items need the module system for their nested type resolution. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestBrowsingPolicyEnforcement.class)));
	}

	// --- Web search: model-supplied narrowing under a configured ceiling. ---

	/** A domain the model asks for that the policy forbids does not make it reachable. */
	public void testAModelSuppliedAllowListCannotWiden() {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.oracle.com", Access.ALLOW)));

		SearchScope widened = WebSearchTool.narrow(browsing, List.of("evil.example.com"), null);
		assertNotNull("the search is refused rather than run on the whole web", widened.refusal());
		assertTrue(widened.refusal(), widened.refusal().contains("docs.oracle.com"));

		SearchScope mixed =
			WebSearchTool.narrow(browsing, List.of("evil.example.com", "docs.oracle.com"), null);
		assertNull(mixed.refusal());
		assertEquals("only the permitted half survives", List.of("docs.oracle.com"), mixed.include());
	}

	/** Naming nothing gets the policy's own ceiling, so an unrestricted search cannot happen either. */
	public void testAnUnfilteredSearchInheritsTheCeiling() {
		BrowsingPolicy browsing = BrowsingPolicy.of(policy("research", Access.DENY,
			origin("docs.oracle.com", Access.ALLOW), origin("*.top-logic.com", Access.ALLOW)));

		SearchScope scope = WebSearchTool.narrow(browsing, null, null);
		assertNull(scope.refusal());
		assertEquals(List.of("docs.oracle.com", "top-logic.com"), scope.include());
	}

	/** A model-supplied domain that is narrower than the ceiling is kept as asked. */
	public void testAModelSuppliedAllowListMayNarrow() {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("*.top-logic.com", Access.ALLOW)));

		SearchScope scope = WebSearchTool.narrow(browsing, List.of("dev.top-logic.com"), null);
		assertNull(scope.refusal());
		assertEquals(List.of("dev.top-logic.com"), scope.include());
	}

	/** What the policy refuses is added to the engine's exclude list beside what the model asked. */
	public void testDeniedOriginsReachTheEngineAsExclusions() {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("open", Access.ALLOW, origin("pastebin.com", Access.DENY)));

		SearchScope scope = WebSearchTool.narrow(browsing, null, List.of("spam.example.com"));
		assertNull(scope.refusal());
		assertEquals(List.of(), scope.include());
		assertEquals(List.of("spam.example.com", "pastebin.com"), scope.exclude());
	}

	/**
	 * An engine that ignores the domain filter does not get past the policy: the hits are filtered
	 * again on the way back, which is the half that actually binds.
	 */
	public void testOffPolicyHitsAreDroppedFromWhatTheEngineAnswered() {
		BrowsingPolicy browsing =
			BrowsingPolicy.of(policy("research", Access.DENY, origin("docs.oracle.com", Access.ALLOW)));
		WebSearchResults answered = new WebSearchResults("an answer", List.of(
			hit("https://docs.oracle.com/a"),
			hit("https://evil.example.com/b"),
			hit("not a url at all")));

		WebSearchResults filtered = WebSearchTool.onPolicyOnly(browsing, answered);

		assertEquals(1, filtered.hits().size());
		assertEquals("https://docs.oracle.com/a", filtered.hits().get(0).url());
		assertEquals("the engine's answer text is kept", "an answer", filtered.answer());
	}

	/** With nothing configured the results come back exactly as the engine answered them. */
	public void testNothingConfiguredFiltersNothing() {
		WebSearchResults answered =
			new WebSearchResults(null, List.of(hit("https://example.com/a")));

		assertSame(answered, WebSearchTool.onPolicyOnly(BrowsingPolicy.unrestricted(), answered));

		SearchScope scope = WebSearchTool.narrow(BrowsingPolicy.unrestricted(), null, null);
		assertEquals(List.of(), scope.include());
		assertEquals(List.of(), scope.exclude());
		assertNull(scope.refusal());
	}

	// --- Fixture. ---

	private static WebSearchHit hit(String url) {
		return new WebSearchHit("title", url, "snippet", null);
	}
}
