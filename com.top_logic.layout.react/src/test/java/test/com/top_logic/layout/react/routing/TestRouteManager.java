/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;

/**
 * Tests for {@link RouteManager}.
 */
public class TestRouteManager extends TestCase {

	static class MockParticipant implements RoutingParticipant {

		private final List<RoutePattern> _routes;

		private RouteSegment _activeSegment;

		private RouteMatch _lastActivation;

		private final List<RouteChangeListener> _listeners = new ArrayList<>();

		MockParticipant(List<RoutePattern> routes) {
			_routes = routes;
		}

		@Override
		public List<RoutePattern> declaredRoutes() {
			return _routes;
		}

		@Override
		public void activateRoute(RouteMatch match) {
			_lastActivation = match;
			_activeSegment = new RouteSegment(
				match.pattern().produce(match.params()));
		}

		@Override
		public RouteSegment activeRouteSegment() {
			return _activeSegment;
		}

		@Override
		public void addRouteChangeListener(RouteChangeListener l) {
			_listeners.add(l);
		}

		@Override
		public void removeRouteChangeListener(RouteChangeListener l) {
			_listeners.remove(l);
		}

		void simulateNavigation(String itemId, Map<String, String> params) {
			RoutePattern pattern = _routes.stream()
				.filter(r -> r.itemId().equals(itemId)).findFirst().orElseThrow();
			_activeSegment = new RouteSegment(pattern.produce(params));
			for (RouteChangeListener l : new ArrayList<>(_listeners)) {
				l.onRouteChange(this, _activeSegment);
			}
		}

		RouteMatch lastActivation() {
			return _lastActivation;
		}
	}

	/**
	 * Tests that the composed URL reflects the active segment of a single participant.
	 */
	public void testUrlComposition() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore"),
			RoutePattern.compile("/listings", "listings")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());
		assertEquals("explore", rm.currentUrl());
	}

	/**
	 * Tests that a pending URL is resolved when a matching participant registers.
	 */
	public void testPendingUrlResolution() {
		RouteManager rm = new RouteManager();
		rm.setPendingUrl("explore");
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore"),
			RoutePattern.compile("/listings", "listings")));
		rm.register(sidebar);
		assertNotNull(sidebar.lastActivation());
		assertEquals("explore", sidebar.lastActivation().itemId());
	}

	/**
	 * Tests that path parameters are extracted during pending URL resolution.
	 */
	public void testParamResolution() {
		RouteManager rm = new RouteManager();
		rm.setPendingUrl("property/42");
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/property/:id", "detail")));
		rm.register(sidebar);
		assertNotNull(sidebar.lastActivation());
		assertEquals("42", sidebar.lastActivation().params().get("id"));
	}

	/**
	 * Tests that multiple participants compose a nested URL.
	 */
	public void testNestedComposition() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore")));
		MockParticipant tabs = new MockParticipant(List.of(
			RoutePattern.compile("/featured", "featured"),
			RoutePattern.compile("/nearby", "nearby")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());
		rm.register(tabs);
		tabs.simulateNavigation("featured", Map.of());
		assertEquals("explore/featured", rm.currentUrl());
	}

	/**
	 * Tests that URL change notifications are delivered to the handler.
	 */
	public void testUrlChangeNotification() {
		RouteManager rm = new RouteManager();
		List<String> urlChanges = new ArrayList<>();
		rm.setUrlChangeHandler((url, replace) -> urlChanges.add(url));

		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());
		assertEquals(1, urlChanges.size());
		assertEquals("explore", urlChanges.get(0));
	}

	/**
	 * Tests that {@code navigateToRoute} activates the matching route on a registered participant.
	 */
	public void testNavigateToRoute() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore"),
			RoutePattern.compile("/listings", "listings")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());

		rm.navigateToRoute("listings");
		assertEquals("listings", sidebar.lastActivation().itemId());
	}

	/**
	 * Tests that unregistering a participant removes its segment from the composed URL.
	 */
	public void testUnregister() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore")));
		rm.register(sidebar);
		rm.unregister(sidebar);
		assertEquals("", rm.currentUrl());
	}
}
