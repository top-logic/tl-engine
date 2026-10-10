/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.time.Instant;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Test for the subagent {@link AgentEvent} record family.
 */
public class TestSubagentEvents extends TestCase {

	public void testSubagentLifecycleEventsCarrySubagentId() {
		AgentEvent started =
			new AgentEvent.SubagentStarted("run1", 2, Instant.now(), "sa-1", "TL_ModelExplorer", "find X", "call-1", false);
		assertTrue(started instanceof AgentEvent);
		assertEquals("sa-1", ((AgentEvent.SubagentStarted) started).subagentId());

		SubagentResult result = new SubagentResult("r", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		AgentEvent.SubagentCompleted completed =
			new AgentEvent.SubagentCompleted("run1", 2, Instant.now(), "sa-1", result, "call-1", "child-1");
		assertSame(result, completed.result());
	}

	public void testDelegatedEventCarriesSubagentId() {
		AgentEvent.TurnStarted child = new AgentEvent.TurnStarted("child", 1, Instant.now());
		AgentEvent.DelegatedEvent delegated =
			new AgentEvent.DelegatedEvent("run1", 2, Instant.now(), "sa-1", "toolcall-9", child);
		assertEquals("sa-1", delegated.subagentId());
		assertEquals("toolcall-9", delegated.delegationToolCallId());
		assertSame(child, delegated.childEvent());
	}
}
