/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.sidebar.NavigationItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests that the URL a {@link ReactSidebarControl} composes names the page it displays, and never
 * one the user has left.
 *
 * <p>
 * A page contributes to the URL through the {@link RoutingParticipant}s of the controls it shows -
 * the tab bar naming the tab in view, above all. Those belong to their page: while the sidebar
 * already reports the item just picked, a participant of the previous page is read as belonging to
 * the new one, and the composed URL then carries a segment of a page that is not displayed. Such a
 * URL is written to the address bar, where it is copied and followed, and it does not lead back to
 * what was on screen.
 * </p>
 */
public class TestSidebarRouting extends TestCase {

	/**
	 * A control that contributes one fixed segment to the URL, standing in for the tab bar a page
	 * shows.
	 */
	private static final class RoutedContent extends ReactControl implements RoutingParticipant {

		private final RouteSegment _segment;

		private final List<RouteChangeListener> _listeners = new ArrayList<>();

		private final Supplier<String> _sidebarSegment;

		/** What the sidebar reported at the moment this content was detached. */
		String _segmentAtDetach;

		RoutedContent(ReactContext context, String segment, Supplier<String> sidebarSegment) {
			super(context, null, "TestContent");
			_segment = new RouteSegment(segment);
			_sidebarSegment = sidebarSegment;
		}

		@Override
		protected void onDetach() {
			_segmentAtDetach = _sidebarSegment.get();
			super.onDetach();
		}

		@Override
		public List<RoutePattern> declaredRoutes() {
			return List.of(RoutePattern.compile(_segment.path(), _segment.path()));
		}

		@Override
		public void activateRoute(RouteMatch match) {
			// Always active: this stands for a tab bar that shows one fixed tab.
		}

		@Override
		public RouteSegment activeRouteSegment() {
			return _segment;
		}

		@Override
		public void addRouteChangeListener(RouteChangeListener listener) {
			_listeners.add(listener);
		}

		@Override
		public void removeRouteChangeListener(RouteChangeListener listener) {
			_listeners.remove(listener);
		}
	}

	private static void render(ReactControl control) {
		try {
			control.write(new TagWriter());
		} catch (IOException ex) {
			throw new AssertionError("Rendering the sidebar failed.", ex);
		}
	}

	/**
	 * The page being left is retired while the sidebar still names it, so that nothing can compose
	 * the two pages into one URL.
	 *
	 * <p>
	 * Asserted on the order of the two events rather than on a composed URL: what puts a stale
	 * segment into the address bar is the incoming page registering while the outgoing one is still
	 * registered, and which of the several things the sidebar does happens to trigger a composition
	 * is not the point - that the pages never overlap is.
	 * </p>
	 */
	public void testThePageLeftIsRetiredBeforeTheNewOneIsNamed() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue());

		ReactSidebarControl[] holder = new ReactSidebarControl[1];
		Supplier<String> sidebarSegment = () -> {
			RouteSegment segment = holder[0].activeRouteSegment();
			return segment == null ? null : segment.path();
		};
		List<RoutedContent> created = new ArrayList<>();
		Supplier<ReactControl> first = () -> {
			RoutedContent content = new RoutedContent(context, "one", sidebarSegment);
			created.add(content);
			return content;
		};

		List<SidebarItem> items = List.of(
			new NavigationItem("first", "First", null, first).withRoute("first"),
			new NavigationItem("second", "Second", null,
				() -> new RoutedContent(context, "two", sidebarSegment)).withRoute("second"));

		ReactSidebarControl sidebar =
			new ReactSidebarControl(context, items, "first", false, null, null);
		holder[0] = sidebar;

		sidebar.attach();
		// The content of an item is created when the sidebar is rendered, not when it is attached,
		// so there is nothing to leave behind until it has been.
		render(sidebar);
		assertFalse("The first item's content was never created, so nothing is being left.",
			created.isEmpty());

		sidebar.selectItem("second");

		RoutedContent left = created.get(0);
		assertNotNull("The content of the page left behind was never detached, so its routing "
			+ "participant stays registered for a page that is no longer displayed.",
			left._segmentAtDetach);
		assertEquals("The page left behind was retired only after the sidebar already named the "
			+ "page moved to; while both are registered, the composed URL carries a segment of a "
			+ "page that is not displayed.", "first", left._segmentAtDetach);
	}

}
