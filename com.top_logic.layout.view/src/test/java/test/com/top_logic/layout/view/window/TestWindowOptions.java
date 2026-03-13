/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.window;

import junit.framework.TestCase;

import com.top_logic.layout.react.window.WindowOptions;

public class TestWindowOptions extends TestCase {

	public void testDefaults() {
		WindowOptions options = new WindowOptions();
		assertEquals(800, options.getWidth());
		assertEquals(600, options.getHeight());
		assertEquals("", options.getTitle());
		assertTrue(options.isResizable());
	}

	public void testBuilder() {
		WindowOptions options = new WindowOptions()
			.setWidth(1024)
			.setHeight(768)
			.setTitle("Help")
			.setResizable(false);

		assertEquals(1024, options.getWidth());
		assertEquals(768, options.getHeight());
		assertEquals("Help", options.getTitle());
		assertFalse(options.isResizable());
	}
}
