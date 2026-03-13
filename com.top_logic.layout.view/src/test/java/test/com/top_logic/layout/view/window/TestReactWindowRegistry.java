/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.window;

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

public class TestReactWindowRegistry extends TestCase {

	public void testOpenWindowCreatesEntry() {
		ReactWindowRegistry registry = new ReactWindowRegistry();

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
		ReactWindowRegistry registry = new ReactWindowRegistry();
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext ctx = new DefaultReactContext("", "vOpener", openerQueue, registry);

		String id1 = registry.openWindow(ctx, new WindowOptions());
		String id2 = registry.openWindow(ctx, new WindowOptions());

		assertNotSame(id1, id2);
		assertFalse(id1.equals(id2));
	}

	public void testWindowClosed() {
		ReactWindowRegistry registry = new ReactWindowRegistry();
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext ctx = new DefaultReactContext("", "vOpener", openerQueue, registry);

		String windowId = registry.openWindow(ctx, new WindowOptions());
		assertNotNull(registry.getWindow(windowId));

		registry.windowClosed(windowId);
		assertNull(registry.getWindow(windowId));
	}

	public void testGetNonexistentWindow() {
		ReactWindowRegistry registry = new ReactWindowRegistry();

		assertNull(registry.getWindow("vDoesNotExist"));
	}

	public void testOpenWindowWithProvider() {
		ReactWindowRegistry registry = new ReactWindowRegistry();
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

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactWindowRegistry.class,
			TypeIndex.Module.INSTANCE);
	}
}
