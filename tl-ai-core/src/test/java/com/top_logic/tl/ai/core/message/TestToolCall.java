/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import junit.framework.TestCase;

/**
 * Tests for {@link ToolCall}.
 */
public class TestToolCall extends TestCase {

	/** Null id is rejected. */
	@SuppressWarnings("unused")
	public void testNullIdRejected() {
		try {
			new ToolCall(null, "read_file", "{}");
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/** Null name is rejected. */
	@SuppressWarnings("unused")
	public void testNullNameRejected() {
		try {
			new ToolCall("tc_1", null, "{}");
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/** Null arguments is rejected. */
	@SuppressWarnings("unused")
	public void testNullArgumentsRejected() {
		try {
			new ToolCall("tc_1", "read_file", null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

}
