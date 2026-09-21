/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.react.window.WindowOptions;

/**
 * Tests for {@link WindowEntry}.
 */
public class TestWindowEntry extends TestCase {

	public void testCreation() {
		WindowOptions options = new WindowOptions().setWidth(1024).setTitle("Test");
		WindowEntry entry = new WindowEntry("vNewWindow", "vOpener", options, null);

		assertEquals("vNewWindow", entry.getWindowId());
		assertEquals("vOpener", entry.getOpenerWindowId());
		assertNull(entry.getRootControl());
		assertEquals(1024, entry.getOptions().getWidth());
		assertNull(entry.getCloseCallback());
		assertFalse(entry.isConnected());
	}

	public void testMarkConnected() {
		WindowEntry entry = new WindowEntry("vW1", "vOpener", new WindowOptions(), null);
		assertFalse(entry.isConnected());

		entry.markConnected();
		assertTrue(entry.isConnected());
	}

	/**
	 * The tree a window holds is rendered again until a rebuild is demanded, and the tree built in
	 * answer is rendered again as well.
	 */
	public void testRequestRebuild() {
		WindowEntry entry = new WindowEntry("vW1", "vOpener", new WindowOptions(), null);
		assertFalse("A window displays the tree it holds.", entry.isRebuildRequested());

		entry.setRootControl(control(entry));
		assertFalse("Displaying a tree does not ask for it to be replaced.",
			entry.isRebuildRequested());

		entry.requestRebuild();
		assertTrue("The tree must not be rendered again.", entry.isRebuildRequested());

		ReactControl rebuilt = control(entry);
		entry.setRootControl(rebuilt);
		assertSame(rebuilt, entry.getRootControl());
		assertFalse("The tree built in answer is the one to display.", entry.isRebuildRequested());
	}

	/**
	 * A window without a tree takes the demand as well: it is the reload that decides what to do with
	 * whatever the window holds.
	 */
	public void testRequestRebuildWithoutTree() {
		WindowEntry entry = new WindowEntry("vW1", "vOpener", new WindowOptions(), null);

		entry.requestRebuild();

		assertNull(entry.getRootControl());
		assertTrue(entry.isRebuildRequested());
	}

	/** A control on the given window's queue, standing in for the tree the window displays. */
	private static ReactControl control(WindowEntry entry) {
		ReactContext context = new DefaultReactContext("", entry.getWindowId(), entry.getQueue(),
			new ReactWindowRegistry("testSession"));
		return new ReactControl(context, null, "Demo");
	}
}
