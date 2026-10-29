/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import junit.framework.TestCase;

import com.top_logic.layout.view.command.SetDensityCommand;

/**
 * Tests for {@link SetDensityCommand}: what it sends to the browser.
 */
public class TestSetDensityCommand extends TestCase {

	/**
	 * The chosen density reaches the client through the documented global object, at once.
	 */
	public void testClientCode() {
		assertEquals("window.tlTheme.density('compact');", SetDensityCommand.clientCode("compact"));
		assertEquals("window.tlTheme.density('normal');", SetDensityCommand.clientCode("normal"));
	}
}
