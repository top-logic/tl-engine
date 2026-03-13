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
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.react.window.WindowOptions;

public class TestReactWindowRegistry extends TestCase {

	public void testOpenWindowCreatesEntry() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactWindowRegistry registry = new ReactWindowRegistry(queue);

		ReactContext openerCtx = new DefaultReactContext("", "vOpener", queue);
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
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactWindowRegistry registry = new ReactWindowRegistry(queue);
		ReactContext ctx = new DefaultReactContext("", "vOpener", queue);

		String id1 = registry.openWindow(ctx, new WindowOptions());
		String id2 = registry.openWindow(ctx, new WindowOptions());

		assertNotSame(id1, id2);
		assertFalse(id1.equals(id2));
	}

	public void testWindowClosed() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactWindowRegistry registry = new ReactWindowRegistry(queue);
		ReactContext ctx = new DefaultReactContext("", "vOpener", queue);

		String windowId = registry.openWindow(ctx, new WindowOptions());
		assertNotNull(registry.getWindow(windowId));

		registry.windowClosed(windowId);
		assertNull(registry.getWindow(windowId));
	}

	public void testGetNonexistentWindow() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactWindowRegistry registry = new ReactWindowRegistry(queue);

		assertNull(registry.getWindow("vDoesNotExist"));
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactWindowRegistry.class,
			TypeIndex.Module.INSTANCE);
	}
}
