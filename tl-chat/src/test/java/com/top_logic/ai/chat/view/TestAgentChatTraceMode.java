/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import junit.framework.TestCase;

import com.top_logic.ai.chat.view.AgentChatElement.TraceMode;

/**
 * Checks the administrator-facing trace-mode names used by {@code <agent-chat/>}.
 */
public class TestAgentChatTraceMode extends TestCase {

	/** XML and React both consume the lowercase external names. */
	public void testExternalNames() {
		assertEquals("none", TraceMode.NONE.getExternalName());
		assertEquals("overview", TraceMode.OVERVIEW.getExternalName());
		assertEquals("detailed", TraceMode.DETAILED.getExternalName());
	}
}
