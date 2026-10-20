/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;

import junit.framework.TestCase;

/**
 * Tests for the per-session {@link SubagentJobRegistry} accessor on {@link ChatSessionManager}.
 */
public class TestSessionSubagentRegistry extends TestCase {

	/** With no active session the accessor returns null (null-safe path). */
	public void testNoActiveSessionReturnsNull() {
		ChatSessionManager mgr = new ChatSessionManager();
		SubagentJobRegistry result = mgr.getActiveSubagentRegistry();
		assertNull("No active session — registry must be null", result);
	}
}
