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
		rm.adoptUrl("explore");
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
		rm.adoptUrl("property/42");
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

	/**
	 * Tests that the URL is composed from the displayed participants, not from the sequence in which
	 * the participants registered.
	 */
	public void testCompositionFollowsDisplay() {
		RouteManager rm = new RouteManager();
		MockParticipant tabs = new MockParticipant(List.of(
			RoutePattern.compile("/featured", "featured")));
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore")));

		// A tab rendered lazily registers after the sidebar it is displayed in.
		rm.register(tabs);
		tabs.simulateNavigation("featured", Map.of());
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());

		rm.setDisplayedParticipants(() -> List.of(sidebar, tabs));
		assertEquals("explore/featured", rm.currentUrl());
	}

	/**
	 * Tests that a participant that has left the display contributes no segment, even while it is
	 * still registered.
	 */
	public void testUndisplayedParticipantIsNotComposed() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore")));
		MockParticipant tabs = new MockParticipant(List.of(
			RoutePattern.compile("/featured", "featured")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());
		rm.register(tabs);
		tabs.simulateNavigation("featured", Map.of());

		rm.setDisplayedParticipants(() -> List.of(sidebar));
		assertEquals("explore", rm.currentUrl());
	}

	/**
	 * Tests that adopting a URL the browser already displays creates no history entry, neither while
	 * the URL is resolved nor for the display the resolution materializes afterwards.
	 */
	public void testNavigationCreatesNoHistoryEntry() {
		RouteManager rm = new RouteManager();
		List<Boolean> replaceFlags = new ArrayList<>();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore"),
			RoutePattern.compile("/listings", "listings")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());

		rm.setUrlChangeHandler((url, replace) -> replaceFlags.add(replace));
		rm.navigateToRoute("listings");
		assertEquals("listings", rm.currentUrl());

		// The navigated-to view materializes its display only when it is rendered, so a tab of it
		// enters the composition after the navigation has been resolved.
		MockParticipant tabs = new MockParticipant(List.of(
			RoutePattern.compile("/featured", "featured")));
		tabs.simulateNavigation("featured", Map.of());
		rm.register(tabs);

		assertEquals("listings/featured", rm.currentUrl());
		assertFalse("A browser navigation must not push a history entry.", replaceFlags.contains(Boolean.FALSE));
	}

	/**
	 * Tests that a pending URL is adopted by a display that already exists, i.e. whose participants
	 * are registered before the URL to adopt is known.
	 */
	public void testPendingUrlAdoptedByExistingDisplay() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore"),
			RoutePattern.compile("/listings", "listings")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());

		rm.adoptUrl("listings");
		rm.resolvePending();

		assertEquals("listings", sidebar.lastActivation().itemId());
		assertEquals("listings", rm.currentUrl());
	}

	/**
	 * Tests that a URL the display cannot reproduce is corrected without a history entry, and only
	 * once the adoption is through with the segments it can resolve.
	 */
	public void testUnresolvableUrlIsCorrected() {
		RouteManager rm = new RouteManager();
		List<String> urls = new ArrayList<>();
		List<Boolean> replaceFlags = new ArrayList<>();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());

		rm.setUrlChangeHandler((url, replace) -> {
			urls.add(url);
			replaceFlags.add(replace);
		});
		rm.adoptUrl("nowhere");
		rm.resolvePending();
		assertEquals("Nothing is reported while the display may still materialize.", List.of(), urls);

		rm.finishAdoption();

		assertEquals(List.of("explore"), urls);
		assertEquals(List.of(Boolean.TRUE), replaceFlags);
	}

	/**
	 * Tests that a freshly loaded page is told the URL its display composes, even though an earlier
	 * page of the same window was already shown that URL.
	 */
	public void testFreshPageIsToldTheComposedUrl() {
		RouteManager rm = new RouteManager();
		List<String> urls = new ArrayList<>();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());

		rm.setUrlChangeHandler((url, replace) -> urls.add(url));

		// The page displays no route at all, whatever the page before it displayed.
		rm.adoptUrl("");
		rm.resolvePending();
		rm.finishAdoption();

		assertEquals(List.of("explore"), urls);
	}

	/**
	 * Tests that a value the display establishes for a segment the adopted URL left unspecified
	 * reaches the address bar without a history entry.
	 */
	public void testDefaultValueOfAdoptedUrlCreatesNoHistoryEntry() {
		RouteManager rm = new RouteManager();
		List<String> urls = new ArrayList<>();
		List<Boolean> replaceFlags = new ArrayList<>();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/listings", "listings")));
		MockParticipant selection = new MockParticipant(List.of(
			RoutePattern.compile("/:id", "id")));
		rm.register(sidebar);

		rm.setUrlChangeHandler((url, replace) -> {
			urls.add(url);
			replaceFlags.add(replace);
		});

		// The URL names the view but not the object within it.
		rm.adoptUrl("listings");
		rm.resolvePending();

		// Rendering the view registers its route parameter and selects a default.
		rm.register(selection);
		selection.simulateNavigation("id", Map.of("id", "42"));
		rm.finishAdoption();

		assertEquals("listings/42", rm.currentUrl());
		assertFalse("The default of an adopted URL must not push a history entry.",
			replaceFlags.contains(Boolean.FALSE));
		assertEquals("listings/42", urls.get(urls.size() - 1));
	}

	/**
	 * Tests that a route change reported by a displayed participant pushes a history entry, so that
	 * the user can undo the navigation with the back button.
	 */
	public void testUserNavigationCreatesHistoryEntry() {
		RouteManager rm = new RouteManager();
		List<Boolean> replaceFlags = new ArrayList<>();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore"),
			RoutePattern.compile("/listings", "listings")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());

		rm.setUrlChangeHandler((url, replace) -> replaceFlags.add(replace));
		sidebar.simulateNavigation("listings", Map.of());

		assertEquals(List.of(Boolean.FALSE), replaceFlags);
	}
}
