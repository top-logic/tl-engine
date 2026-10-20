/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a {@link SSEUpdateQueue#shutdown() shut down} queue no longer holds the window it
 * served.
 */
public class TestSSEUpdateQueueShutdown extends TestCase {

	/**
	 * The tree, the dialog manager and the route manager of the window are let go.
	 */
	public void testShutdownDropsWindow() throws Exception {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));
		ReactControl root = new ReactControl(context, null, "TLPanel");
		root.write(new TagWriter());
		queue.setRootControl(root);
		queue.setDialogManager(dialogManager());
		queue.setRouteManager(new RouteManager());
		assertSame(root, queue.getRootControl());

		queue.shutdown();

		assertNull("The root control must be dropped.", queue.getRootControl());
		assertNull("The dialog manager must be dropped.", queue.getDialogManager());
		assertNull("The route manager must be dropped.", queue.getRouteManager());
		assertFalse("No control must be kept.", queue.hasControls());
	}

	private static DialogManager dialogManager() {
		return (DialogManager) Proxy.newProxyInstance(DialogManager.class.getClassLoader(),
			new Class<?>[] { DialogManager.class }, (proxy, method, args) -> {
				throw new UnsupportedOperationException(method.getName());
			});
	}

}
