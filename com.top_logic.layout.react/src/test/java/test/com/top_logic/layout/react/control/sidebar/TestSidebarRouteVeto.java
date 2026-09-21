/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.sidebar;

import java.util.List;

import junit.framework.TestCase;

import test.com.top_logic.layout.react.dirty.UnsavedChanges;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.sidebar.NavigationItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.dirty.DirtyChannel;
import com.top_logic.layout.react.dirty.StateHandler;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a URL takes the user off a {@link ReactSidebarControl} item holding unsaved changes no
 * more silently than a click on another item does.
 *
 * <p>
 * The browser's back button and the address bar reach the sidebar as a URL, which the
 * {@link RouteManager} resolves into an activation of the item the URL names. An activation that
 * simply switched would drop what the user typed into the page being left, without anyone being
 * asked.
 * </p>
 */
public class TestSidebarRouteVeto extends TestCase {

	/** The item that is displayed and whose content holds the unsaved changes. */
	private static final String FIRST = "first";

	/** The item the URL names. */
	private static final String SECOND = "second";

	/** The React module of the content a navigation item displays. */
	private static final String CONTENT_MODULE = "TestContent";

	/** The context the sidebar and its {@link RouteManager} belong to. */
	private ReactContext _context;

	/** The unsaved changes tracked for the item displayed. */
	private DirtyChannel _dirtyChannel;

	private ReactSidebarControl _sidebar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_dirtyChannel = new DirtyChannel();

		List<SidebarItem> items = List.of(
			new NavigationItem(FIRST, "First", null, this::content, _dirtyChannel).withRoute(FIRST),
			new NavigationItem(SECOND, "Second", null, this::content).withRoute(SECOND));

		_sidebar = new ReactSidebarControl(_context, items, FIRST, false, null, null);

		// Attaching registers the sidebar with the route manager of its context, which is the seam a
		// URL reaches it through.
		_sidebar.attach();
	}

	/** The content of a navigation item - nothing is displayed, the unsaved changes are tracked
	 * by the item's {@link DirtyChannel} directly. */
	private ReactControl content() {
		return new ReactControl(_context, null, CONTENT_MODULE);
	}

	/** The route segment the sidebar contributes to the URL. */
	private String activeSegment() {
		return _sidebar.activeRouteSegment().path();
	}

	/**
	 * Tests that a URL naming another item is refused while the item displayed holds unsaved
	 * changes: the refusal names them, and the sidebar keeps showing the item it showed.
	 */
	public void testUrlIsRefusedByUnsavedChanges() {
		StateHandler changes = new UnsavedChanges();
		_dirtyChannel.updateState(changes, true);

		RouteManager routeManager = _context.getRouteManager();
		try {
			routeManager.navigateToRoute(SECOND);
			fail("A URL must not take the user off a page holding unsaved changes.");
		} catch (ChannelVetoException ex) {
			assertEquals("The unsaved changes that refused the URL are reported.",
				List.of(changes), ex.getDirtyHandlers());
		}

		assertEquals("The refused URL must leave the sidebar showing the item it displays.",
			FIRST, activeSegment());

		// What the caller of a refused URL does: the display stays as the refusal keeps it, and the
		// address the client is restored to is the one it composes.
		routeManager.cancelAdoption();
		assertEquals("The address the refusal restores is the one the display composes.",
			FIRST, routeManager.currentUrl());
	}

	/**
	 * Tests that the same URL switches the sidebar once the unsaved changes are resolved, which is
	 * what the retry after a save or a discard does.
	 */
	public void testUrlSwitchesWhenChangesAreResolved() {
		StateHandler changes = new UnsavedChanges();
		_dirtyChannel.updateState(changes, true);

		changes.executeDiscard();
		_dirtyChannel.updateState(changes, false);

		RouteManager routeManager = _context.getRouteManager();
		routeManager.navigateToRoute(SECOND);
		routeManager.finishAdoption();

		assertEquals("Nothing refuses the URL, so the sidebar shows the item it names.",
			SECOND, activeSegment());
		assertEquals(SECOND, routeManager.currentUrl());
	}

	/**
	 * Tests that a URL naming the item already displayed asks nothing, although that item holds
	 * unsaved changes: a deeper segment, a query parameter, a step back between two addresses of the
	 * same page all reach the sidebar as the item it shows, and nothing there is being left.
	 */
	public void testUrlOfTheItemDisplayedAsksNothing() {
		StateHandler changes = new UnsavedChanges();
		_dirtyChannel.updateState(changes, true);

		RouteManager routeManager = _context.getRouteManager();
		routeManager.navigateToRoute(FIRST);
		routeManager.finishAdoption();

		assertEquals("The item displayed stays displayed, without a question about input that is "
			+ "not being left.", FIRST, activeSegment());
		assertEquals(FIRST, routeManager.currentUrl());
	}

	/**
	 * Tests that the same holds for a click on the item already displayed: what the user typed is
	 * not what a click landing where it already is asks about.
	 */
	public void testClickOnTheItemDisplayedAsksNothing() {
		_dirtyChannel.updateState(new UnsavedChanges(), true);

		_sidebar.revealChild(FIRST);

		assertEquals(FIRST, activeSegment());
	}

	/**
	 * Tests that an item without a {@link DirtyChannel} of its own is left by a URL without anyone
	 * being asked.
	 */
	public void testItemWithoutDirtyChannelIsLeft() {
		StateHandler changes = new UnsavedChanges();
		_dirtyChannel.updateState(changes, true);

		RouteManager routeManager = _context.getRouteManager();
		try {
			routeManager.navigateToRoute(SECOND);
			fail("A URL must not take the user off a page holding unsaved changes.");
		} catch (ChannelVetoException ex) {
			// Expected - the item displayed tracks the unsaved changes.
		}
		routeManager.cancelAdoption();

		// The unsaved changes are resolved, so the item the URL named is reached; it tracks no
		// unsaved changes of its own, and leaving it again asks nobody.
		_dirtyChannel.updateState(changes, false);
		routeManager.navigateToRoute(SECOND);
		routeManager.finishAdoption();
		assertEquals(SECOND, activeSegment());

		routeManager.navigateToRoute(FIRST);
		routeManager.finishAdoption();
		assertEquals(FIRST, activeSegment());
	}

}
