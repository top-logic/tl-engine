/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.classic;

import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

/**
 * Tests how {@link ReactCommandLockFilter} reads the window a React command addresses.
 */
public class TestReactCommandLockFilter extends TestCase {

	/**
	 * The window name stands at the top level of the body the React bridge posts.
	 */
	public void testWindowNameOfCommand() {
		assertEquals("v0123abcd", ReactCommandLockFilter.windowName(bytes(
			"{\"controlId\":\"v12\",\"command\":\"publishDraft\",\"windowName\":\"v0123abcd\","
				+ "\"arguments\":{\"windowName\":\"not-this-one\"}}")));
	}

	/**
	 * A body without a window, or one that is not JSON, names no classic window: the command
	 * passes the filter without a lock and the React servlet answers it as before.
	 */
	public void testNoWindowName() {
		assertNull(ReactCommandLockFilter.windowName(bytes("{\"controlId\":\"v12\",\"command\":\"x\"}")));
		assertNull(ReactCommandLockFilter.windowName(bytes("{\"windowName\":42}")));
		assertNull(ReactCommandLockFilter.windowName(bytes("[\"windowName\"]")));
		assertNull(ReactCommandLockFilter.windowName(bytes("not json")));
		assertNull(ReactCommandLockFilter.windowName(new byte[0]));
	}

	private static byte[] bytes(String body) {
		return body.getBytes(StandardCharsets.UTF_8);
	}

}
