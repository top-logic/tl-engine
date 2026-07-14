/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Verifies that subagent {@link AgentEvent} variants map to the correct subagent_* SSE event names.
 */
public class TestSubagentSSE extends TestCase {

	/** */
	public void testSubagentStartedMapsToSse() {
		List<String> emitted = new ArrayList<>();
		ChatSSEHandler handler = ChatSSEHandler.recordingForTest(emitted::add);

		handler.onEventForTest(new AgentEvent.SubagentStarted(
			"run1", 1, Instant.now(), "sa-1", "TL_ModelExplorer", "find X"));
		handler.flushForTest();
		assertTrue("Expected subagent_started event", emitted.contains("subagent_started"));

		handler.onEventForTest(new AgentEvent.SubagentCompleted(
			"run1", 2, Instant.now(), "sa-1",
			new SubagentResult("done", TokenUsage.EMPTY, 12L, 1, StopReason.COMPLETE)));
		handler.flushForTest();
		assertTrue("Expected subagent_completed event", emitted.contains("subagent_completed"));
	}

	/** */
	public void testSubagentFailedMapsToSse() {
		List<String> emitted = new ArrayList<>();
		ChatSSEHandler handler = ChatSSEHandler.recordingForTest(emitted::add);

		handler.onEventForTest(new AgentEvent.SubagentFailed(
			"run1", 1, Instant.now(), "sa-2", "timeout"));
		assertTrue("Expected subagent_failed event", emitted.contains("subagent_failed"));
	}

	/** */
	public void testSubagentCancelledMapsToSse() {
		List<String> emitted = new ArrayList<>();
		ChatSSEHandler handler = ChatSSEHandler.recordingForTest(emitted::add);

		handler.onEventForTest(new AgentEvent.SubagentCancelled(
			"run1", 1, Instant.now(), "sa-3"));
		assertTrue("Expected subagent_cancelled event", emitted.contains("subagent_cancelled"));
	}
}
