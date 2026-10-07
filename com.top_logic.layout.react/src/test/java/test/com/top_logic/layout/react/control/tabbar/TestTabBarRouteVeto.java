/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.tabbar;

import java.util.List;

import junit.framework.TestCase;

import test.com.top_logic.layout.react.dirty.UnsavedChanges;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.dirty.DirtyChannel;
import com.top_logic.layout.react.dirty.StateHandler;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a URL takes the user off a {@link ReactTabBarControl} tab holding unsaved changes no
 * more silently than a click on another tab does.
 *
 * <p>
 * The browser's back button and the address bar reach the tab bar as a URL, which the
 * {@link RouteManager} resolves into an activation of the tab the URL names. An activation that
 * simply switched would drop what the user typed into the tab being left, without anyone being
 * asked.
 * </p>
 */
public class TestTabBarRouteVeto extends TestCase {

	/** The tab that is displayed and whose content holds the unsaved changes. */
	private static final String OVERVIEW = "overview";

	/** The tab the URL names. */
	private static final String DETAILS = "details";

	/** The React module of the content a tab displays. */
	private static final String CONTENT_MODULE = "TestContent";

	/** The context the tab bar and its {@link RouteManager} belong to. */
	private ReactContext _context;

	/** The unsaved changes tracked for the tab displayed. */
	private DirtyChannel _dirtyChannel;

	private ReactTabBarControl _tabBar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_dirtyChannel = new DirtyChannel();

		List<TabDefinition> tabs = List.of(
			new TabDefinition(OVERVIEW, "Overview", this::content, _dirtyChannel).withRoute(OVERVIEW),
			new TabDefinition(DETAILS, "Details", this::content, null).withRoute(DETAILS));

		_tabBar = new ReactTabBarControl(_context, null, tabs, OVERVIEW);

		// Attaching registers the tab bar with the route manager of its context, which is the seam a
		// URL reaches it through.
		_tabBar.attach();
	}

	/** The content of a tab - nothing is displayed, the unsaved changes are tracked by the tab's
	 * {@link DirtyChannel} directly. */
	private ReactControl content() {
		return new ReactControl(_context, null, CONTENT_MODULE);
	}

	/**
	 * Tests that a URL naming another tab is refused while the tab displayed holds unsaved changes:
	 * the refusal names them, and the tab bar keeps showing the tab it showed.
	 */
	public void testUrlIsRefusedByUnsavedChanges() {
		StateHandler changes = new UnsavedChanges();
		_dirtyChannel.updateState(changes, true);

		RouteManager routeManager = _context.getRouteManager();
		try {
			routeManager.navigateToRoute(DETAILS);
			fail("A URL must not take the user off a tab holding unsaved changes.");
		} catch (ChannelVetoException ex) {
			assertEquals("The unsaved changes that refused the URL are reported.",
				List.of(changes), ex.getDirtyHandlers());
		}

		assertEquals("The refused URL must leave the tab bar showing the tab it displays.",
			OVERVIEW, _tabBar.getActiveTabId());
		assertEquals(OVERVIEW, _tabBar.activeRouteSegment().path());

		// What the caller of a refused URL does: the display stays as the refusal keeps it, and the
		// address the client is restored to is the one it composes.
		routeManager.cancelAdoption();
		assertEquals("The address the refusal restores is the one the display composes.",
			OVERVIEW, routeManager.currentUrl());
	}

	/**
	 * Tests that a URL naming the tab already displayed asks nothing, although that tab holds
	 * unsaved changes: a deeper segment, a query parameter, a step back between two addresses of the
	 * same page all reach the tab bar as the tab it shows, and nothing there is being left.
	 */
	public void testUrlOfTheTabDisplayedAsksNothing() {
		StateHandler changes = new UnsavedChanges();
		_dirtyChannel.updateState(changes, true);

		RouteManager routeManager = _context.getRouteManager();
		routeManager.navigateToRoute(OVERVIEW);
		routeManager.finishAdoption();

		assertEquals("The tab displayed stays displayed, without a question about input that is "
			+ "not being left.", OVERVIEW, _tabBar.getActiveTabId());
		assertEquals(OVERVIEW, routeManager.currentUrl());
	}

	/**
	 * Tests that the same holds for a click on the tab already displayed: what the user typed is not
	 * what a click landing where it already is asks about.
	 */
	public void testClickOnTheTabDisplayedAsksNothing() {
		_dirtyChannel.updateState(new UnsavedChanges(), true);

		_tabBar.revealChild(OVERVIEW);

		assertEquals(OVERVIEW, _tabBar.getActiveTabId());
	}

	/**
	 * Tests that the same URL switches the tab bar once the unsaved changes are resolved, and that
	 * the tab reached - which tracks no unsaved changes of its own - is left again without anyone
	 * being asked.
	 */
	public void testUrlSwitchesWhenChangesAreResolved() {
		StateHandler changes = new UnsavedChanges();
		_dirtyChannel.updateState(changes, true);

		changes.executeDiscard();
		_dirtyChannel.updateState(changes, false);

		RouteManager routeManager = _context.getRouteManager();
		routeManager.navigateToRoute(DETAILS);
		routeManager.finishAdoption();

		assertEquals("Nothing refuses the URL, so the tab bar shows the tab it names.",
			DETAILS, _tabBar.getActiveTabId());
		assertEquals(DETAILS, routeManager.currentUrl());

		routeManager.navigateToRoute(OVERVIEW);
		routeManager.finishAdoption();
		assertEquals("A tab without a dirty channel is left without a refusal.",
			OVERVIEW, _tabBar.getActiveTabId());
	}

}
