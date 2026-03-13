/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.window;

import junit.framework.TestCase;

import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.react.window.WindowOptions;

public class TestWindowEntry extends TestCase {

	public void testCreation() {
		WindowOptions options = new WindowOptions().setWidth(1024).setTitle("Test");
		WindowEntry entry = new WindowEntry("vNewWindow", "vOpener", null, options, null);

		assertEquals("vNewWindow", entry.getWindowId());
		assertEquals("vOpener", entry.getOpenerWindowId());
		assertNull(entry.getRootControl());
		assertEquals(1024, entry.getOptions().getWidth());
		assertNull(entry.getCloseCallback());
		assertFalse(entry.isConnected());
	}

	public void testMarkConnected() {
		WindowEntry entry = new WindowEntry("vW1", "vOpener", null, new WindowOptions(), null);
		assertFalse(entry.isConnected());

		entry.markConnected();
		assertTrue(entry.isConnected());
	}
}
