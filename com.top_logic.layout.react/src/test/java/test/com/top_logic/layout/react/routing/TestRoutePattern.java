/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.routing;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;

/**
 * Tests for {@link RoutePattern}.
 */
public class TestRoutePattern extends TestCase {

	public void testStaticMatch() {
		RoutePattern pattern = RoutePattern.compile("/explore", "explore");
		RouteMatch match = pattern.match("explore");
		assertNotNull(match);
		assertEquals("explore", match.itemId());
		assertTrue(match.params().isEmpty());
	}

	public void testStaticNoMatch() {
		RoutePattern pattern = RoutePattern.compile("/explore", "explore");
		assertNull(pattern.match("listings"));
	}

	public void testParamMatch() {
		RoutePattern pattern = RoutePattern.compile("/property/:estateId", "property-detail");
		RouteMatch match = pattern.match("property/42");
		assertNotNull(match);
		assertEquals("property-detail", match.itemId());
		assertEquals("42", match.params().get("estateId"));
	}

	public void testParamNoMatch() {
		RoutePattern pattern = RoutePattern.compile("/property/:estateId", "property-detail");
		assertNull(pattern.match("listings"));
	}

	public void testWildcardMatch() {
		RoutePattern pattern = RoutePattern.compile("/*", "not-found");
		RouteMatch match = pattern.match("anything/here");
		assertNotNull(match);
		assertEquals("not-found", match.itemId());
	}

	public void testConsumedSegments() {
		RoutePattern pattern = RoutePattern.compile("/property/:id", "detail");
		RouteMatch match = pattern.match("property/42/overview");
		assertNotNull(match);
		assertEquals("42", match.params().get("id"));
		assertEquals("overview", match.remainingPath());
	}

	public void testSegmentProduction() {
		RoutePattern pattern = RoutePattern.compile("/property/:id", "detail");
		String segment = pattern.produce(Map.of("id", "42"));
		assertEquals("property/42", segment);
	}

}
