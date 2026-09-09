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

		RouteSegment _activeSegment;

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
			notifyRouteChange(_activeSegment);
		}

		void notifyRouteChange(RouteSegment segment) {
			for (RouteChangeListener l : new ArrayList<>(_listeners)) {
				l.onRouteChange(this, segment);
			}
		}

		RouteMatch lastActivation() {
			return _lastActivation;
		}
	}

	/**
	 * A {@link MockParticipant} that has a state without a route to return to.
	 */
	static class ResettableParticipant extends MockParticipant {

		private boolean _reset;

		ResettableParticipant(List<RoutePattern> routes) {
			super(routes);
		}

		@Override
		public void resetRoute() {
			_reset = true;
			_activeSegment = null;
		}

		boolean wasReset() {
			return _reset;
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
	 * Tests that a registered participant the display does not contain is passed over when a pending
	 * URL is resolved, and that a participant the display shows only after an earlier activation
	 * takes its turn: the frames of a tile stack each carry a tab bar declaring the same routes, and
	 * the segment following the stack's route belongs to the tab bar of the frame that route puts on
	 * top, not to the one of a frame it covers.
	 */
	public void testPendingUrlIsResolvedByDisplayedParticipantsOnly() {
		RouteManager rm = new RouteManager();
		List<RoutePattern> tabRoutes = List.of(
			RoutePattern.compile("/contact", "contact"),
			RoutePattern.compile("/accounts", "accounts"));
		MockParticipant lowerTabs = new MockParticipant(tabRoutes);
		MockParticipant upperTabs = new MockParticipant(tabRoutes);
		List<MockParticipant> displayed = new ArrayList<>();
		MockParticipant stack = new MockParticipant(List.of(
			RoutePattern.compile("/person/:person", "person"))) {
			@Override
			public void activateRoute(RouteMatch match) {
				super.activateRoute(match);
				// The route puts the upper frame on top, which uncovers nothing and covers the lower one.
				displayed.clear();
				displayed.addAll(List.of(this, upperTabs));
			}
		};
		// Registration order: the stack, then the tab bars frame by frame - the tab bar of the covered
		// frame registered before the one of the frame on top.
		rm.register(stack);
		rm.register(lowerTabs);
		rm.register(upperTabs);
		displayed.addAll(List.of(stack, lowerTabs));
		rm.setDisplayedParticipants(() -> new ArrayList<>(displayed));

		rm.adoptUrl("person/1/accounts");
		rm.resolvePending();
		rm.finishAdoption();

		assertNotNull(stack.lastActivation());
		assertNull("Tab bar of a covered frame must not take up the route.", lowerTabs.lastActivation());
		assertNotNull("Tab bar of the frame on top takes up the route.", upperTabs.lastActivation());
		assertEquals("accounts", upperTabs.lastActivation().pattern().itemId());
		assertEquals("person/1/accounts", rm.currentUrl());
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
	 * Tests that a display change carried out as a navigation reports one history entry, however many
	 * participants appear and disappear while it is applied.
	 */
	public void testNavigationReportsOneHistoryEntry() {
		RouteManager rm = new RouteManager();
		List<String> urls = new ArrayList<>();
		List<Boolean> replaceFlags = new ArrayList<>();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore"),
			RoutePattern.compile("/listings", "listings")));
		MockParticipant leavingTab = new MockParticipant(List.of(
			RoutePattern.compile("/featured", "featured")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());
		leavingTab.simulateNavigation("featured", Map.of());
		rm.register(leavingTab);

		rm.setUrlChangeHandler((url, replace) -> {
			urls.add(url);
			replaceFlags.add(replace);
		});

		MockParticipant enteringTab = new MockParticipant(List.of(
			RoutePattern.compile("/nearby", "nearby")));
		rm.navigate(() -> {
			// What the item left behind displayed goes away, what the selected one displays appears.
			rm.unregister(leavingTab);
			enteringTab.simulateNavigation("nearby", Map.of());
			rm.register(enteringTab);
			sidebar.simulateNavigation("listings", Map.of());
		});

		assertEquals("listings/nearby", rm.currentUrl());
		assertEquals(List.of("listings/nearby"), urls);
		assertEquals(List.of(Boolean.FALSE), replaceFlags);
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

	/**
	 * Tests that a display change applied while a URL is adopted reports nothing: it is the display
	 * settling into the URL the client already shows, not a navigation away from it, and the end of
	 * the adoption reports what the display composes then - here the URL the client shows already.
	 */
	public void testDisplayChangeWhileAdoptingIsNoHistoryEntry() {
		RouteManager rm = new RouteManager();
		List<Boolean> replaceFlags = new ArrayList<>();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/explore", "explore"),
			RoutePattern.compile("/listings", "listings")));
		rm.register(sidebar);
		sidebar.simulateNavigation("explore", Map.of());

		rm.setUrlChangeHandler((url, replace) -> replaceFlags.add(replace));
		rm.adoptUrl("listings");
		rm.navigate(() -> sidebar.simulateNavigation("listings", Map.of()));
		rm.finishAdoption();

		assertEquals("listings", rm.currentUrl());
		assertEquals("The display materializing into the adopted URL reports nothing of its own.",
			List.of(), replaceFlags);
	}

	/**
	 * Tests that a URL naming fewer segments than the display shows returns the participant which
	 * displayed the segment it leaves out to its state without a route.
	 */
	public void testShorterUrlResetsUnnamedParticipant() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		ResettableParticipant detail = new ResettableParticipant(List.of(
			RoutePattern.compile("/detail/:id", "id")));
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());
		rm.register(detail);
		detail.simulateNavigation("id", Map.of("id", "x"));
		assertEquals("a/detail/x", rm.currentUrl());

		// The way back out of the detail.
		rm.navigateToRoute("a");
		rm.finishAdoption();

		assertTrue("What the URL leaves out is what the user navigated away from.", detail.wasReset());
		assertEquals("a", rm.currentUrl());
	}

	/**
	 * Tests that a URL naming a segment for a participant of the display activates it instead of
	 * resetting it.
	 */
	public void testNamedParticipantIsActivatedNotReset() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		ResettableParticipant detail = new ResettableParticipant(List.of(
			RoutePattern.compile("/detail/:id", "id")));
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());
		rm.register(detail);

		rm.adoptUrl("a/detail/x");
		rm.resolvePending();
		rm.finishAdoption();

		assertFalse("A participant the URL names is not reset.", detail.wasReset());
		assertEquals("x", detail.lastActivation().params().get("id"));
		assertEquals("a/detail/x", rm.currentUrl());
	}

	/**
	 * Tests that a back navigation is reported only with the URL the display settles on - and not at
	 * all where that is the URL the browser already shows.
	 */
	public void testBackNavigationReportsTheSettledUrlOnly() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		ResettableParticipant detail = new ResettableParticipant(List.of(
			RoutePattern.compile("/detail/:id", "id")));
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());
		rm.register(detail);
		detail.simulateNavigation("id", Map.of("id", "x"));

		List<String> urls = new ArrayList<>();
		rm.setUrlChangeHandler((url, replace) -> urls.add(url));

		// The way back out of the detail, which the display reaches by resetting what the URL leaves
		// out - a state the address bar describes before the display arrives at it.
		rm.navigateToRoute("a");
		rm.finishAdoption();

		assertEquals("a", rm.currentUrl());
		assertEquals("Nothing is reported halfway through the adoption, and the URL it settles on is "
			+ "the one the browser already shows.", List.of(), urls);
	}

	/**
	 * Tests that a refused URL leaves the display as it is and is not reported - its caller tells the
	 * client which address it is left with.
	 */
	public void testCancelledAdoptionLeavesDisplay() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a"),
			RoutePattern.compile("/b", "b")));
		ResettableParticipant detail = new ResettableParticipant(List.of(
			RoutePattern.compile("/detail/:id", "id")));
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());
		rm.register(detail);
		detail.simulateNavigation("id", Map.of("id", "x"));

		List<String> urls = new ArrayList<>();
		rm.setUrlChangeHandler((url, replace) -> urls.add(url));

		// A form with unsaved input vetoes leaving it.
		rm.adoptUrl("b");
		rm.cancelAdoption();

		assertEquals("What the refusal keeps is what the display shows.", "a/detail/x", rm.currentUrl());
		assertFalse("A URL that is not taken up resets nothing.", detail.wasReset());
		assertEquals("The refusal is reported by its caller, together with the address it restores.",
			List.of(), urls);
	}

	/**
	 * Tests that a navigation after a refused URL is a history entry again, rather than a replacement
	 * of the address the refusal restored.
	 */
	public void testNavigationAfterCancelIsHistoryEntry() {
		RouteManager rm = new RouteManager();
		List<Boolean> replaceFlags = new ArrayList<>();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a"),
			RoutePattern.compile("/b", "b")));
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());

		rm.adoptUrl("b");
		rm.cancelAdoption();

		rm.setUrlChangeHandler((url, replace) -> replaceFlags.add(replace));
		sidebar.simulateNavigation("b", Map.of());

		assertEquals("b", rm.currentUrl());
		assertEquals(List.of(Boolean.FALSE), replaceFlags);
	}

	/**
	 * Tests that a participant an activation of the URL brought into the display keeps the segment it
	 * establishes itself, even though the URL named none for it.
	 */
	public void testParticipantMountedByActivationIsNotReset() {
		RouteManager rm = new RouteManager();
		ResettableParticipant selection = new ResettableParticipant(List.of(
			RoutePattern.compile("/:id", "id")));
		// Activating the sidebar item materializes the view it names, which registers its selection.
		MountingParticipant sidebar = new MountingParticipant(List.of(
			RoutePattern.compile("/a", "a")), rm, selection);
		rm.register(sidebar);

		// The URL names the view but not the object within it.
		rm.adoptUrl("a");
		rm.resolvePending();

		// The view that appeared picks a default of its own.
		selection.simulateNavigation("id", Map.of("id", "42"));
		rm.finishAdoption();

		assertFalse("What an activation of the URL brought into the display is part of what it asked for.",
			selection.wasReset());
		assertEquals("a/42", rm.currentUrl());
	}

	/**
	 * Tests that a display re-registering while a URL is adopted - a page loaded into the control
	 * tree its window still holds, which attaches that tree - is reset where the URL does not name
	 * what it shows.
	 */
	public void testReRegisteredDisplayIsResetByShorterUrl() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		ResettableParticipant detail = new ResettableParticipant(List.of(
			RoutePattern.compile("/detail/:id", "id")));
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());
		rm.register(detail);
		detail.simulateNavigation("id", Map.of("id", "x"));
		assertEquals("a/detail/x", rm.currentUrl());

		// The bare URL entered in the address bar: it is adopted before the tree the window holds is
		// attached again, so every participant of that tree registers while the adoption is running.
		rm.adoptUrl("a");
		rm.unregister(sidebar);
		rm.unregister(detail);
		rm.register(sidebar);
		rm.register(detail);
		rm.resolvePending();
		rm.finishAdoption();

		assertTrue("Re-registering is not being brought into the display by the URL.",
			detail.wasReset());
		assertEquals("a", rm.currentUrl());
	}

	/**
	 * Tests that a participant which always displays something keeps its segment where the URL names
	 * none, because it has no state without a route to return to.
	 */
	public void testParticipantWithoutResetKeepsSegment() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		MockParticipant tabs = new MockParticipant(List.of(
			RoutePattern.compile("/featured", "featured")));
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());
		rm.register(tabs);
		tabs.simulateNavigation("featured", Map.of());

		rm.navigateToRoute("a");
		rm.finishAdoption();

		assertEquals("A tab bar shows one of its tabs whatever the URL says.", "a/featured",
			rm.currentUrl());
	}

	/**
	 * Tests that a participant displaying a chain of routes is offered the rest of the URL again
	 * after each route it takes up, while one displaying a single thing is offered one route.
	 */
	public void testSequenceOfRoutesReachesOneParticipant() {
		RouteManager rm = new RouteManager();
		SequenceParticipant path = new SequenceParticipant(List.of(
			RoutePattern.compile("/person/:person", "person"),
			RoutePattern.compile("/order/:order", "order")));
		rm.register(path);

		rm.adoptUrl("person/1/order/7");
		rm.resolvePending();

		assertEquals("A drill-down path takes up one route per frame.",
			List.of("person=1", "order=7"), path.activations());

		MockParticipant single = new MockParticipant(List.of(
			RoutePattern.compile("/:id", "id")));
		RouteManager other = new RouteManager();
		other.register(single);
		other.adoptUrl("1/7");
		other.resolvePending();

		assertEquals("A participant displaying one thing takes up one route.", "1",
			single.lastActivation().params().get("id"));
	}

	/**
	 * Tests that the query parameters of the displayed participants form the query string of the
	 * composed URL, behind its path.
	 */
	public void testQueryComposition() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/listings", "listings")));
		QueryParticipant filter = new QueryParticipant("q");
		QueryParticipant sort = new QueryParticipant("sort");
		rm.register(sidebar);
		sidebar.simulateNavigation("listings", Map.of());
		rm.register(filter);
		rm.register(sort);

		assertEquals("A participant with nothing to say contributes no parameter.", "listings",
			rm.currentUrl());

		filter.setValue("a b");
		sort.setValue("name");
		assertEquals("listings?q=a+b&sort=name", rm.currentUrl());

		filter.setValue(null);
		assertEquals("listings?sort=name", rm.currentUrl());
	}

	/**
	 * Tests that the query of an adopted URL is offered to the participants, decoded, without
	 * changing how its path is resolved.
	 */
	public void testAdoptedQueryReachesParticipants() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a/:id", "detail")));
		QueryParticipant filter = new QueryParticipant("q");
		rm.register(sidebar);
		rm.register(filter);

		rm.adoptUrl("a/b?q=x%20y");
		rm.resolvePending();
		rm.finishAdoption();

		assertEquals("The query is no part of the path.", "b", sidebar.lastActivation().params().get("id"));
		assertEquals(Map.of("q", "x y"), filter.activatedQuery());
		assertEquals("a/b?q=x+y", rm.currentUrl());
	}

	/**
	 * Tests that a participant appearing while a URL is adopted is offered the query of that URL,
	 * because a parameter belongs to whichever participant declares it, wherever in the display that
	 * is.
	 */
	public void testAdoptedQueryReachesLateParticipant() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		rm.register(sidebar);

		rm.adoptUrl("a?q=late");
		rm.resolvePending();

		// The view the URL named materializes and brings its filter into the display.
		QueryParticipant filter = new QueryParticipant("q");
		rm.register(filter);
		rm.finishAdoption();

		assertEquals(Map.of("q", "late"), filter.activatedQuery());
		assertEquals("a?q=late", rm.currentUrl());
	}

	/**
	 * Tests that a URL without the parameter says nothing about the value: what the view establishes
	 * stands, and the address bar is completed with it as a replacement.
	 */
	public void testUrlWithoutParameterKeepsValue() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		QueryParticipant filter = new QueryParticipant("q");
		rm.register(sidebar);
		rm.register(filter);
		filter.setValue("established");

		List<Boolean> replaceFlags = new ArrayList<>();
		rm.setUrlChangeHandler((url, replace) -> replaceFlags.add(replace));

		rm.adoptUrl("a");
		rm.resolvePending();
		rm.finishAdoption();

		assertNull("A URL without the parameter activates nothing.", filter.activatedQuery());
		assertEquals("a?q=established", rm.currentUrl());
		assertEquals(List.of(Boolean.TRUE), replaceFlags);
	}

	/**
	 * Tests that a change of the query alone is no history entry, although the participant reported it
	 * as a navigation: refining what the page shows stays on that page.
	 */
	public void testQueryChangeIsNoHistoryEntry() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a"),
			RoutePattern.compile("/b", "b")));
		QueryParticipant filter = new QueryParticipant("q");
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());
		rm.register(filter);

		List<String> urls = new ArrayList<>();
		List<Boolean> replaceFlags = new ArrayList<>();
		rm.setUrlChangeHandler((url, replace) -> {
			urls.add(url);
			replaceFlags.add(replace);
		});

		filter.setValue("x");
		filter.setValue("xy");
		assertEquals(List.of("a?q=x", "a?q=xy"), urls);
		assertEquals("A filter the user narrows is not a page to come back from.",
			List.of(Boolean.TRUE, Boolean.TRUE), replaceFlags);

		// A change of the path is a navigation, whatever the query does.
		sidebar.simulateNavigation("b", Map.of());
		assertEquals("b?q=xy", urls.get(urls.size() - 1));
		assertEquals(Boolean.FALSE, replaceFlags.get(replaceFlags.size() - 1));
	}

	/**
	 * Tests that the display being built up for an adopted URL reports nothing while it exchanges what
	 * it contains, and that the URL it arrives at is reported once.
	 */
	public void testDisplayRebuildWhileAdoptingReportsOnce() {
		RouteManager rm = new RouteManager();
		MockParticipant leaving = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		MockParticipant entering = new MockParticipant(List.of(
			RoutePattern.compile("/b", "b")));
		rm.register(leaving);
		leaving.simulateNavigation("a", Map.of());

		List<String> urls = new ArrayList<>();
		List<Boolean> replaceFlags = new ArrayList<>();
		rm.setUrlChangeHandler((url, replace) -> {
			urls.add(url);
			replaceFlags.add(replace);
		});

		// The page the client shows is loaded into the control tree the window still holds: attaching
		// that tree unregisters what leaves the display and registers what enters it, and neither is
		// an address of its own.
		rm.adoptUrl("b");
		rm.unregister(leaving);
		entering.simulateNavigation("b", Map.of());
		rm.register(entering);
		assertEquals("Nothing is reported while the display is being built up.", List.of(), urls);

		rm.finishAdoption();

		assertEquals("b", rm.currentUrl());
		assertEquals("The URL the display arrives at is the one the client already shows.", List.of(),
			urls);
	}

	/**
	 * Tests that the one URL an adoption reports is the one the display arrives at, where that differs
	 * from the URL the client shows.
	 */
	public void testCorrectedUrlOfRebuiltDisplayIsReportedOnce() {
		RouteManager rm = new RouteManager();
		MockParticipant sidebar = new MockParticipant(List.of(
			RoutePattern.compile("/a", "a")));
		MockParticipant tabs = new MockParticipant(List.of(
			RoutePattern.compile("/featured", "featured")));
		rm.register(sidebar);
		sidebar.simulateNavigation("a", Map.of());

		List<String> urls = new ArrayList<>();
		List<Boolean> replaceFlags = new ArrayList<>();
		rm.setUrlChangeHandler((url, replace) -> {
			urls.add(url);
			replaceFlags.add(replace);
		});

		// A URL naming only the view, whose display then re-registers a tab of its own.
		rm.adoptUrl("a");
		rm.unregister(tabs);
		tabs.simulateNavigation("featured", Map.of());
		rm.register(tabs);
		rm.resolvePending();
		rm.finishAdoption();

		assertEquals(List.of("a/featured"), urls);
		assertEquals(List.of(Boolean.TRUE), replaceFlags);
	}

	/**
	 * A participant whose activation brings another one into the display, as materializing the view a
	 * sidebar item names does.
	 */
	static class MountingParticipant extends MockParticipant {

		private final RouteManager _manager;

		private final RoutingParticipant _mounted;

		MountingParticipant(List<RoutePattern> routes, RouteManager manager, RoutingParticipant mounted) {
			super(routes);
			_manager = manager;
			_mounted = mounted;
		}

		@Override
		public void activateRoute(RouteMatch match) {
			super.activateRoute(match);
			_manager.register(_mounted);
		}
	}

	/**
	 * A participant whose state travels in a query parameter, as a filter term does.
	 */
	static class QueryParticipant extends MockParticipant {

		private final String _name;

		private String _value;

		private Map<String, String> _activatedQuery;

		QueryParticipant(String name) {
			super(List.of());
			_name = name;
		}

		@Override
		public void activateQuery(Map<String, String> query) {
			_activatedQuery = query;
			String value = query.get(_name);
			if (value != null) {
				setValue(value);
			}
		}

		@Override
		public RouteSegment activeRouteSegment() {
			return _value == null ? null : new RouteSegment("", Map.of(_name, _value));
		}

		/**
		 * The query this participant was offered, or {@code null} while it was offered none.
		 */
		Map<String, String> activatedQuery() {
			return _activatedQuery;
		}

		/**
		 * Sets the value the participant contributes and reports the change.
		 */
		void setValue(String value) {
			_value = value;
			RouteSegment segment = activeRouteSegment();
			notifyRouteChange(segment != null ? segment : new RouteSegment(""));
		}
	}

	/**
	 * A participant that takes up route after route of one URL, as a drill-down path does.
	 */
	static class SequenceParticipant extends MockParticipant {

		private final List<String> _activations = new ArrayList<>();

		SequenceParticipant(List<RoutePattern> routes) {
			super(routes);
		}

		@Override
		public boolean acceptsRouteSequence() {
			return true;
		}

		@Override
		public void activateRoute(RouteMatch match) {
			super.activateRoute(match);
			match.params().forEach((name, value) -> _activations.add(name + "=" + value));
		}

		List<String> activations() {
			return _activations;
		}
	}
}
