/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.react.window.WindowOptions;

/**
 * Tests for {@link ReactWindowRegistry}.
 */
public class TestReactWindowRegistry extends TestCase {

	/** Session ID for the registries under test; they are never looked up by it here. */
	private static final String SESSION_ID = "testSession";

	public void testOpenWindowCreatesEntry() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext openerCtx = new DefaultReactContext("", "vOpener", openerQueue, registry);
		WindowOptions options = new WindowOptions().setWidth(1024).setTitle("Test");

		String windowId = registry.openWindow(openerCtx, options);

		assertNotNull(windowId);
		assertTrue("Window ID must start with 'v'", windowId.startsWith("v"));

		WindowEntry entry = registry.getWindow(windowId);
		assertNotNull(entry);
		assertEquals(windowId, entry.getWindowId());
		assertEquals("vOpener", entry.getOpenerWindowId());
		assertEquals(1024, entry.getOptions().getWidth());
		assertFalse(entry.isConnected());
	}

	public void testWindowIdsAreUnique() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext ctx = new DefaultReactContext("", "vOpener", openerQueue, registry);

		String id1 = registry.openWindow(ctx, new WindowOptions());
		String id2 = registry.openWindow(ctx, new WindowOptions());

		assertNotSame(id1, id2);
		assertFalse(id1.equals(id2));
	}

	public void testWindowClosed() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext ctx = new DefaultReactContext("", "vOpener", openerQueue, registry);

		String windowId = registry.openWindow(ctx, new WindowOptions());
		assertNotNull(registry.getWindow(windowId));

		registry.windowClosed(windowId);
		assertNull(registry.getWindow(windowId));
	}

	public void testGetNonexistentWindow() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		assertNull(registry.getWindow("vDoesNotExist"));
	}

	public void testOpenWindowWithProvider() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext openerCtx = new DefaultReactContext("", "vOpener", openerQueue, registry);

		WindowOptions options = new WindowOptions().setWidth(800).setTitle("Provider Test");

		String windowId = registry.openWindow(openerCtx,
			(ctx, model) -> new ReactControl(ctx, model, "test-module"),
			"testModel", options);

		assertNotNull(windowId);
		WindowEntry entry = registry.getWindow(windowId);
		assertNotNull(entry);
		assertNotNull("Provider must be stored", entry.getControlProvider());
		assertEquals("testModel", entry.getModel());
		assertNull("Root control must be null before ViewServlet builds it",
			entry.getRootControl());

		// Simulate what ViewServlet does: build the tree with the child's context.
		SSEUpdateQueue childQueue = registry.getOrCreateQueue(windowId);
		ReactContext childCtx = new DefaultReactContext("", windowId, childQueue, registry);
		ReactControl builtControl = entry.getControlProvider().createControl(
			childCtx, entry.getModel());
		entry.setRootControl(builtControl);

		assertNotNull("Root control must be set after building", entry.getRootControl());
		// Verify the control is registered on the child's queue, not the opener's.
		assertNotNull("Control must be findable on child queue",
			childQueue.getControl(builtControl.getID()));
		assertNull("Control must NOT be on opener queue",
			openerQueue.getControl(builtControl.getID()));
	}

	/**
	 * A rebuild marks every window of the session and tells each of them to reload.
	 */
	public void testRebuildWindows() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		// The tab the user is working in, and a window opened from it.
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		WindowEntry opener = registry.getWindow("vOpener");
		ReactContext openerCtx = new DefaultReactContext("", "vOpener", openerQueue, registry);
		String openedId = registry.openWindow(openerCtx, new WindowOptions());
		WindowEntry opened = registry.getWindow(openedId);
		SSEUpdateQueue openedQueue = opened.getQueue();

		// The tab displays a tree; the opened window is rendered only once its browser asks.
		opener.setRootControl(new ReactControl(openerCtx, null, "Demo"));
		int openerBefore = openerQueue.pendingEventCount();
		int openedBefore = openedQueue.pendingEventCount();
		assertFalse(opener.isRebuildRequested());
		assertFalse(opened.isRebuildRequested());

		registry.rebuildWindows();

		assertTrue("The tab must be rebuilt.", opener.isRebuildRequested());
		assertTrue("The opened window must be rebuilt.", opened.isRebuildRequested());
		assertEquals("One reload for the tab.", openerBefore + 1, openerQueue.pendingEventCount());
		assertEquals("One reload for the opened window.", openedBefore + 1,
			openedQueue.pendingEventCount());
		assertNotNull("The tree is disposed by the render the reload brings, not here.",
			opener.getRootControl());
	}

	/**
	 * A window that displays nothing yet is marked like any other, and a session without windows has
	 * nothing to rebuild.
	 */
	public void testRebuildWindowsWithoutTree() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		// Nothing registered at all: must not fail.
		registry.rebuildWindows();

		WindowEntry entry = registry.getOrCreateWindow("vTab");
		assertNull(entry.getRootControl());

		registry.rebuildWindows();

		assertTrue(entry.isRebuildRequested());
		assertEquals(1, entry.getQueue().pendingEventCount());
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactWindowRegistry.class,
			TypeIndex.Module.INSTANCE);
	}
}
