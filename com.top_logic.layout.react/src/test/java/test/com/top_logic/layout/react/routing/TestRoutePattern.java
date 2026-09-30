/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.routing;

import java.util.List;
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

	/**
	 * Tests that a produced value keeps the single segment it fills, whatever characters it
	 * contains.
	 */
	public void testValueEncoding() {
		RoutePattern pattern = RoutePattern.compile("/property/:id", "detail");

		assertEquals("property/a%2Fb", pattern.produce(Map.of("id", "a/b")));
		assertEquals("property/a%20b", pattern.produce(Map.of("id", "a b")));
		assertEquals("property/100%25", pattern.produce(Map.of("id", "100%")));
		assertEquals("property/a%3Fb%23c", pattern.produce(Map.of("id", "a?b#c")));
		assertEquals("property/Gr%C3%B6%C3%9Fe", pattern.produce(Map.of("id", "Größe")));
		assertEquals("property/a-b.c_d~e", pattern.produce(Map.of("id", "a-b.c_d~e")));
	}

	/**
	 * Tests that a value is delivered as it was produced, so that a link naming it selects what it
	 * names.
	 */
	public void testValueRoundTrip() {
		RoutePattern pattern = RoutePattern.compile("/property/:id", "detail");

		for (String value : List.of("42", "a/b", "a b", "100%", "a?b#c", "Größe", "🙂", "a-b.c_d~e")) {
			String path = pattern.produce(Map.of("id", value));
			RouteMatch match = pattern.match(path);
			assertNotNull("Path '" + path + "' does not match the pattern that produced it.", match);
			assertEquals(value, match.params().get("id"));
			assertEquals("", match.remainingPath());
		}
	}

	/**
	 * Tests that an encoded slash belongs to the value it was encoded into instead of separating two
	 * segments.
	 */
	public void testEncodedSlashIsNoSeparator() {
		RoutePattern pattern = RoutePattern.compile("/property/:id", "detail");

		RouteMatch match = pattern.match("property/a%2Fb");
		assertNotNull(match);
		assertEquals("a/b", match.params().get("id"));
		assertEquals("", match.remainingPath());

		// An unencoded slash, in contrast, ends the value and leaves what follows to the next
		// pattern.
		RouteMatch split = pattern.match("property/a/b");
		assertNotNull(split);
		assertEquals("a", split.params().get("id"));
		assertEquals("b", split.remainingPath());
	}

	/**
	 * Tests that the remaining path stays encoded, for the pattern that matches it next.
	 */
	public void testRemainingPathStaysEncoded() {
		RoutePattern first = RoutePattern.compile("/property/:id", "detail");
		RouteMatch match = first.match("property/42/a%2Fb");
		assertNotNull(match);
		assertEquals("a%2Fb", match.remainingPath());

		RoutePattern second = RoutePattern.compile(":name", "name");
		RouteMatch rest = second.match(match.remainingPath());
		assertNotNull(rest);
		assertEquals("a/b", rest.params().get("name"));
	}

	/**
	 * Tests that a static segment is matched and produced as the pattern writes it, escapes
	 * included.
	 */
	public void testStaticSegmentIsVerbatim() {
		RoutePattern pattern = RoutePattern.compile("/a%2Fb/:id", "static");

		assertEquals("a%2Fb/42", pattern.produce(Map.of("id", "42")));

		RouteMatch match = pattern.match("a%2Fb/42");
		assertNotNull(match);
		assertEquals("42", match.params().get("id"));
		assertNull("The decoded form of the static segment is not what the pattern writes.",
			pattern.match("a/b/42"));
	}

	/**
	 * Tests that a segment nobody encoded is still readable as a value, so that a hand-written link
	 * works.
	 */
	public void testUnencodedValueIsReadable() {
		RoutePattern pattern = RoutePattern.compile("/property/:id", "detail");

		RouteMatch match = pattern.match("property/50%-100%");
		assertNotNull(match);
		assertEquals("50%-100%", match.params().get("id"));
	}

}
